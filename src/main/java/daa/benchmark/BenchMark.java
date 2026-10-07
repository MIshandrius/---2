package daa.benchmark;

import daa.metrics.Metrics;
import daa.structures.DynamicArray;
import daa.structures.IList;
import daa.structures.MinHeap;
import daa.structures.MyLinkedList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Random;

/**
 * Runs workloads W1-W4 (and bonus B) and writes results/results.csv.
 * Reproducible: every case builds its data from new Random(42).
 * Only the workload itself is timed (filling the structure is not).
 * Every case: WARMUP_RUNS discarded runs, then REAL_RUNS runs, the median time is saved.
 */
public class BenchMark {
    static final int SEED = 42;
    static final int WARMUP_RUNS = 5;
    static final int REAL_RUNS = 5;
    static final int GETS = 10000;
    static final int QUERIES = 1000;
    static final int OPERATIONS = 1000;

    static long bh = 0;      // "black hole": we store results here so the JIT cannot delete the work

    static class Result{
        double timeMs;
        long steps;
        long moves;
        long comparisons;
    }

    static Result makeResult(double timeMs, Metrics m){
        Result r = new Result();
        r.timeMs = timeMs;
        r.steps = m.steps;
        r.moves = m.moves;
        r.comparisons = m.comparisons;
        return r;
    }

    // ------------------------------------------------------------ bonus B

    static MinHeap buildOnce(String method, int[] data){
        MinHeap heap = new MinHeap();
        if(method.equals("buildHeap")){
            heap.buildHeap(data);
        }
        else{
            for (int i = 0; i < data.length; i++){
                heap.insert(data[i]);
            }
        }

        bh = heap.size();
        return heap;
    }

    static void runBonus() throws IOException{
        int[] sizes = {100, 1000, 10000, 100000};
        String[] inputs = {"random" ,"descending"};
        String[] methods = {"n_inserts", "buildHeap"};

        PrintWriter csv = new PrintWriter(new FileWriter("results/bonus_buildheap.csv"));
        csv.println("input,method,n,time_ms,comparisons,moves");

        for(int a = 0; a < inputs.length; a++){
            for(int s = 0; s < sizes.length; s++){
                int n = sizes[s];
                Random rnd = new Random(SEED);
                int[] data = new int[n];
                for(int i = 0; i < n;i++){
                    if(inputs[a].equals("random")){
                        data[i] = rnd.nextInt();
                    }
                    else{
                        data[i] = n - i;        // n, n-1, ..., 1: worst case for insert
                    }
                }

                for(int m = 0; m < methods.length; m++){
                    for(int i = 0; i < WARMUP_RUNS; i++){
                        buildOnce(methods[m], data);
                    }
                    double[] times = new double[REAL_RUNS];
                    MinHeap last = null;
                    for(int i = 0; i < REAL_RUNS;i++){
                        long start = System.nanoTime();
                        last = buildOnce(methods[m],data);
                        long end = System.nanoTime();
                        times[i] = (end- start) / 1000000.0;
                    }
                    double t = median(times);
                    System.out.println("BONUS " + inputs[a] + " " + methods[m] + " n=" + n
                            + "  time=" + String.format(Locale.US, "%.4f", t)
                            + "ms  comps=" + last.metrics().comparisons);
                    csv.println(inputs[a] + "," + methods[m] + "," + n + ","
                            + String.format(Locale.US, "%.4f", t) + ","
                            + last.metrics().comparisons + "," + last.metrics().moves);
                }
            }
        }
        csv.close();
    }

    // ------------------------------------------------------------ data

    /** Only EVEN numbers, so every odd number is guaranteed to be absent (W2). */
    static int[] makeData(Random rnd, int n){
        int[] data = new int[n];
        for (int i = 0; i < n; i++){
            data[i] = rnd.nextInt(1000000) * 2;
        }
        return data;
    }

    // ------------------------------------------------------------ workloads

    /** W1: fill with n values, then 10 000 x get(random index). */
    static Result runW1(IList list, int n){
        Random rnd = new Random(SEED);
        int[] data = makeData(rnd, n);

        int[] indexes = new int[GETS];
        for(int k = 0; k < GETS; k++){
            indexes[k] = rnd.nextInt(n);
        }

        for (int i = 0; i < n; i++){
            list.add(data[i]);
        }
        list.metrics().reset();

        long sum = 0;
        long start = System.nanoTime();
        for(int  k = 0; k < GETS; k++){
            sum = sum + list.get(indexes[k]);
        }

        long end = System.nanoTime();

        bh = sum;
        return makeResult((end -start) / 1000000.0, list.metrics());
    }

    /** W2: 1 000 x contains, half present and half absent. */
    static Result runW2(IList list, int n){
        Random rnd = new Random(SEED);
        int[] data = makeData(rnd, n);

        int[] queries = new int[QUERIES];
        for (int k = 0; k < QUERIES; k++){
            if (k % 2 == 0){
                queries[k] = data[rnd.nextInt(n)];
            }
            else{
                queries[k] = rnd.nextInt(1000000) * 2 + 1;
            }
        }

        for (int i = 0; i < n;i++){
            list.add(data[i]);
        }
        list.metrics().reset();

        int found = 0;
        long start = System.nanoTime();

        for(int k = 0; k < QUERIES; k++){
            if(list.contains(queries[k])){
                found++;
            }
        }

        long end = System.nanoTime();

        bh = found;

        if(found != QUERIES / 2){
            throw new IllegalStateException("W2: expected " + (QUERIES / 2) + " hits, got " + found);
        }

        return makeResult((end - start) / 1000000.0, list.metrics());
    }

    /** W3: 1 000 insertions, then 1 000 removals, at index 0 (head) or n / 2 (middle). */
    static Result runW3(IList list, int n, String variant){
        Random rnd = new Random(SEED);
        int[] data = makeData(rnd, n);

        int[] newValues = new int[OPERATIONS];
        for (int k = 0; k < OPERATIONS; k++){
            newValues[k] = rnd.nextInt(1000000);
        }

        for (int i = 0; i < n;i++){
            list.add(data[i]);
        }
        list.metrics().reset();

        int pos;
        if (variant.equals("head")){
            pos = 0;
        }
        else{
            pos = n / 2;
        }

        long sum = 0;
        long start = System.nanoTime();
        for (int k = 0; k < OPERATIONS; k++){
            list.add(pos,newValues[k]);
        }
        for(int k = 0; k < OPERATIONS; k++){
            sum = sum + list.remove(pos);
        }
        long end = System.nanoTime();

        bh  = sum;
        if(list.size() != n){
            throw new IllegalStateException("W3: wrong final size " + list.size());
        }
        return makeResult((end - start) / 1000000.0, list.metrics());

    }

    /** W4: n x insert, then n x extractMin; the output must be non-decreasing. */
    static Result runW4(int n){
        Random rnd = new Random(SEED);
        int[] data = new int[n];
        for(int i = 0; i < n; i++){
            data[i] = rnd.nextInt();
        }

        MinHeap heap = new MinHeap();

        long sum = 0;
        long start = System.nanoTime();
        for(int i = 0; i < n; i++){
            heap.insert(data[i]);
        }
        int prev  = Integer.MIN_VALUE;
        for(int i = 0; i < n; i++){
            int value = heap.extractMin();
            if(value < prev){
                throw new IllegalStateException("W4: output is not sorted at position " + i);
            }
            prev = value;
            sum = sum + value;
        }

        long end = System.nanoTime();

        bh = sum;
        return makeResult((end - start) / 1000000.0, heap.metrics());
    }

    // ------------------------------------------------------------ running and measuring

    static Result runOnce(String workload, String variant, String structure, int n){
        if(workload.equals("W4")){
            return runW4(n);
        }

        IList list;
        if(structure.equals("DynamicArray")){
            list = new DynamicArray();
        }
        else{
            list = new MyLinkedList();
        }

        if(workload.equals("W1")){
            return runW1(list, n);
        } else if (workload.equals("W2")) {
            return runW2(list, n);
        } else{
            return runW3(list, n, variant);
        }
    }

    /** Median of the numbers (bubble sort on a copy; only 5 numbers). */
    static double median(double[] values){
        double[] sorted = new double[values.length];
        for(int i = 0; i < values.length; i++){
            sorted[i] = values[i];
        }

        for (int i = 0; i < sorted.length; i++){
            for (int j = 0; j < sorted.length - 1 - i; j++){
                if (sorted[j] > sorted[j + 1]){
                    double temp = sorted[j];
                    sorted[j] = sorted[j + 1];
                    sorted[j + 1] = temp;
                }
            }
        }
        return sorted[sorted.length / 2];
    }

    static Result measure(String workload, String variant, String structure, int n){
        for(int i = 0; i < WARMUP_RUNS ;i++){
            runOnce(workload, variant, structure, n);       // warm-up: result is thrown away
        }

        double[] times = new double[REAL_RUNS];
        Result last = null;
        for(int i = 0;i < REAL_RUNS;i++){
            last = runOnce(workload, variant, structure, n);
            times[i] = last.timeMs;
        }

        last.timeMs = median(times);       // counters are the same in every run
        return last;
    }

    /**
     * JIT warm-up of all code paths BEFORE anything is measured.
     * Without it the first measured cases (n = 100) run in slow interpreted code and look
     * slower than n = 1 000. The results of these runs are thrown away.
     */
    static void warmUpJVM(){
        String[] structures = {"DynamicArray", "MyLinkedList"};
        int[] warmSizes = {1000, 10000};
        for(int w = 0; w < warmSizes.length; w++){
            int rounds;
            if(warmSizes[w] == 1000){
                rounds = 40;
            }
            else{
                rounds = 6;
            }
            for(int round = 0; round < rounds; round++){
                for(int s = 0; s < structures.length;s++){
                    runOnce("W1" , "-", structures[s], warmSizes[w]);
                    runOnce("W2" , "-", structures[s], warmSizes[w]);
                    runOnce("W3" , "head", structures[s], warmSizes[w]);
                    runOnce("W3" , "middle", structures[s], warmSizes[w]);
                }
                runOnce("W4", "-", "MinHeap", warmSizes[w]);
            }
        }
    }

    static void runCase(PrintWriter csv, String workload, String variant, String structure, int n){
        Result r = measure(workload, variant, structure, n);

        System.out.println(workload + " " + variant + " " + structure + " n=" + n + "   time=" + String.format(Locale.US, "%.4f", r.timeMs) + "ms" + "   steps=" + r.steps + "  moves=" + r.moves + "  comps=" + r.comparisons);

        csv.println(workload + "," + variant + "," + structure + "," + n + "," + String.format(Locale.US, "%.4f", r.timeMs) + "," + r.steps + "," + r.moves + "," + r.comparisons);
    }

    public static void main(String[] args) throws IOException {
        int[] sizes = {100, 1000, 10000, 100000};
        String[] listStructures = {"DynamicArray", "MyLinkedList"};

        System.out.println("Warming up");
        warmUpJVM();

        new File("results").mkdirs();
        PrintWriter csv = new PrintWriter(new FileWriter("results/results.csv"));
        csv.println("workload,variant,structure,n,time_ms,steps,moves,comparisons");
        for (int s = 0; s < listStructures.length; s++) {
            for (int i = 0; i < sizes.length; i++) {
                runCase(csv, "W1", "-", listStructures[s], sizes[i]);
            }
        }

        for(int s = 0; s < listStructures.length;s++){
            for(int i = 0; i < sizes.length;i++){
                runCase(csv, "W2", "-", listStructures[s], sizes[i]);

            }
        }

        String[] variants = {"head", "middle"};
        for(int v = 0; v< variants.length;v++){
            for(int s = 0; s < listStructures.length; s++){
                for(int i = 0; i< sizes.length; i++){
                    runCase(csv, "W3", variants[v], listStructures[s],sizes[i]);
                }
            }
        }

        for (int i = 0; i < sizes.length; i++){
            runCase(csv, "W4", "-", "MinHeap", sizes[i]);

        }

        csv.close();
        System.out.println("Results saved to results/results.csv");

        runBonus();
        System.out.println("Done. Bonus results saved to results/bonus_buildheap.csv");
    }
}
