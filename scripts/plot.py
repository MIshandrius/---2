#!/usr/bin/env python3
"""Reads results/results.csv and results/bonus_buildheap.csv, writes PNG charts to results/plots/.

W1, W2, W4: one figure each.  W3: one figure with both variants (head, middle).
Left chart  = median time vs n (both structures on the same chart).
Right chart = steps / moves / comparisons vs n.
Log-log axes because n grows 10x per point.

Usage: python3 scripts/plot.py        (needs pandas and matplotlib)
"""
import os
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "results", "plots")
os.makedirs(OUT, exist_ok=True)

df = pd.read_csv(os.path.join(ROOT, "results", "results.csv"))
COLORS = {"DynamicArray": "tab:blue", "MyLinkedList": "tab:red", "MinHeap": "tab:green"}
METRIC_STYLE = {"steps": "-", "moves": "--", "comparisons": ":"}
TITLES = {
    "W1": "W1 - Random access (10 000 x get(random index))",
    "W2": "W2 - Search (1 000 x contains, half present / half absent)",
    "W4": "W4 - MinHeap: n x insert + n x extractMin",
}


def time_panel(ax, sub, title):
    for s, g in sub.groupby("structure"):
        g = g.sort_values("n")
        ax.plot(g["n"], g["time_ms"], marker="o", color=COLORS[s], label=s)
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel("n (number of elements)")
    ax.set_ylabel("median time (ms, log scale)")
    ax.set_title(title + " - time", fontsize=9)
    ax.grid(True, which="both", alpha=0.3)
    ax.legend()


def ops_panel(ax, sub, title):
    for s, g in sub.groupby("structure"):
        g = g.sort_values("n")
        for m, ls in METRIC_STYLE.items():
            if g[m].max() == 0:
                continue  # an all-zero series cannot be drawn on a log axis
            ax.plot(g["n"], g[m], marker="o", ms=4, linestyle=ls, color=COLORS[s], label=f"{s} {m}")
    ax.set_xscale("log")
    ax.set_yscale("log")
    ax.set_xlabel("n (number of elements)")
    ax.set_ylabel("operations (count, log scale)")
    ax.set_title(title + " - operation counts", fontsize=9)
    ax.grid(True, which="both", alpha=0.3)
    ax.legend(fontsize=7, title="counters that are always 0 are not drawn", title_fontsize=7)


for w in ["W1", "W2", "W4"]:
    sub = df[df.workload == w]
    fig, axes = plt.subplots(1, 2, figsize=(12, 4.5))
    time_panel(axes[0], sub, TITLES[w])
    ops_panel(axes[1], sub, TITLES[w])
    fig.tight_layout()
    fig.savefig(os.path.join(OUT, f"{w}.png"), dpi=130)
    plt.close(fig)

fig, axes = plt.subplots(2, 2, figsize=(12, 8.5))
for row, variant in enumerate(["head", "middle"]):
    sub = df[(df.workload == "W3") & (df.variant == variant)]
    t = "W3 (" + variant + ") - 1 000 insertions + 1 000 removals at " + ("index 0" if variant == "head" else "index n/2")
    time_panel(axes[row][0], sub, t)
    ops_panel(axes[row][1], sub, t)
fig.tight_layout()
fig.savefig(os.path.join(OUT, "W3.png"), dpi=130)
plt.close(fig)

bonus_path = os.path.join(ROOT, "results", "bonus_buildheap.csv")
if os.path.exists(bonus_path):
    b = pd.read_csv(bonus_path)
    fig, axes = plt.subplots(2, 2, figsize=(12, 8.5))
    for row, inp in enumerate(["random", "descending"]):
        sub = b[b.input == inp]
        for m, g in sub.groupby("method"):
            g = g.sort_values("n")
            axes[row][0].plot(g["n"], g["time_ms"], marker="o", label=m)
            axes[row][1].plot(g["n"], g["comparisons"], marker="o", label=m)
        for ax, yl, ttl in [(axes[row][0], "median time (ms, log scale)", "time"),
                            (axes[row][1], "comparisons (count, log scale)", "comparisons")]:
            ax.set_xscale("log")
            ax.set_yscale("log")
            ax.set_xlabel("n (number of elements)")
            ax.set_ylabel(yl)
            ax.set_title(f"Bonus B ({inp} input): n x insert vs buildHeap - {ttl}", fontsize=9)
            ax.grid(True, which="both", alpha=0.3)
            ax.legend()
    fig.tight_layout()
    fig.savefig(os.path.join(OUT, "bonus_buildheap.png"), dpi=130)
    plt.close(fig)
print("plots written to", OUT)
