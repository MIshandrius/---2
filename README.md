# DAA Assignment 2 - In-Memory Workload Engine

`DynamicArray`, `MyLinkedList` and `MinHeap` written from scratch (primitive `int`, no `java.util` collections),
with operation counters, a reproducible benchmark (W1-W4 and bonus B), JUnit 5 tests and a report.

Requirements: JDK 17 or newer, Maven 3.8 or newer. Python 3 with `pandas` and `matplotlib` only for the plots.

## 1. Run the tests
```bash
mvn test
```
41 tests: edge cases (empty, one element, duplicates, first/last index, invalid index), comparison with
`ArrayList` / `PriorityQueue` on random data, heap property after every operation, sorted output, exact counter values.

## 2. Run the benchmark (one command, seed 42)
```bash
mvn -q compile exec:java
```
Writes `results/results.csv` (workloads W1-W4) and `results/bonus_buildheap.csv` (bonus B). Takes about 1-2 minutes.
The counters (steps / moves / comparisons) are identical on every run; the times differ a little between runs and machines.

## 3. Make the plots
```bash
python3 scripts/plot.py
```
Writes one PNG per workload and one for bonus B to `results/plots/`.

## What the counters mean
| counter | DynamicArray / MinHeap | MyLinkedList |
|---|---|---|
| steps | one read of an array cell | one visit of a node (`get(i)` = i + 1 visits) |
| moves | one element shifted or copied (array growth counts); in the heap one swap = 1 move | one link updated (`next`, `head`, `tail`) |
| comparisons | one comparison of two elements | one comparison of two elements |

## Benchmark rules
* Data comes from `new Random(42)`, so the run is reproducible.
* Only the workload is timed; filling the structure is not (W1-W3). W4 times insert + extract together.
* Before measuring, the JVM is warmed up; every case then runs 5 discarded warm-up runs and 5 measured runs, the median is saved.
* W2: all stored values are even and the absent queries are odd, so exactly 500 of 1 000 queries are hits (the benchmark stops with an error otherwise).

## Layout
```
src/main/java/daa/structures   IList, DynamicArray, MyLinkedList, MinHeap
src/main/java/daa/metrics      Metrics (steps, moves, comparisons)
src/main/java/daa/benchmark    BenchMark (W1-W4, warm-up, median, CSV, bonus B)
src/test/java/daa/structures   JUnit 5 tests
results/results.csv            all workloads
results/bonus_buildheap.csv    bonus B
results/plots/                 PNG charts
scripts/plot.py                chart generator
REPORT.md                      complexity table, loop-invariant proofs, plots, discussion
```
