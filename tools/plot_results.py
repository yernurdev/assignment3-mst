

import argparse
import json
import os
from typing import List, Dict
import matplotlib.pyplot as plt


def read_results(path: str) -> List[Dict]:
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    rows = []
    for it in data.get("results", []):
        rows.append({
            "graph_id": it["graph_id"],
            "V": it["input_stats"]["vertices"],
            "E": it["input_stats"]["edges"],
            "cost": it["prim"]["total_cost"],
            "prim_ms": it["prim"]["execution_time_ms"],
            "prim_ops": it["prim"]["operations_count"],
            "kr_ms": it["kruskal"]["execution_time_ms"],
            "kr_ops": it["kruskal"]["operations_count"],
        })

    rows.sort(key=lambda r: r["graph_id"])
    return rows

def make_line_chart(x, y, title, xlabel, ylabel, outpath):
    plt.figure()
    plt.plot(x, y, marker="o")  # НЕ указываем цвета / стили
    plt.title(title)
    plt.xlabel(xlabel)
    plt.ylabel(ylabel)
    os.makedirs(os.path.dirname(outpath), exist_ok=True)
    plt.grid(True, linestyle="--", linewidth=0.5)
    plt.savefig(outpath, bbox_inches="tight")
    plt.close()

def generate_for_size(rows: List[Dict], size_label: str, out_dir: str):
    # Время
    make_line_chart(
        [r["graph_id"] for r in rows],
        [r["prim_ms"] for r in rows],
        f"{size_label.capitalize()} graphs — Prim (time)",
        "Graph ID", "Execution time (ms)",
        os.path.join(out_dir, f"{size_label}_graph_prim.png"),
    )
    make_line_chart(
        [r["graph_id"] for r in rows],
        [r["kr_ms"] for r in rows],
        f"{size_label.capitalize()} graphs — Kruskal (time)",
        "Graph ID", "Execution time (ms)",
        os.path.join(out_dir, f"{size_label}_graph_kruskal.png"),
    )
    # Операции
    make_line_chart(
        [r["graph_id"] for r in rows],
        [r["prim_ops"] for r in rows],
        f"{size_label.capitalize()} graphs — Prim (ops)",
        "Graph ID", "Operations (count)",
        os.path.join(out_dir, f"{size_label}_graph_prim_ops.png"),
    )
    make_line_chart(
        [r["graph_id"] for r in rows],
        [r["kr_ops"] for r in rows],
        f"{size_label.capitalize()} graphs — Kruskal (ops)",
        "Graph ID", "Operations (count)",
        os.path.join(out_dir, f"{size_label}_graph_kruskal_ops.png"),
    )

def main():
    parser = argparse.ArgumentParser(description="Build MST charts from JSON outputs (no CSV needed).")
    parser.add_argument("--out-json-small",  default="output/output_small_graphs.json")
    parser.add_argument("--out-json-medium", default="output/output_medium_graphs.json")
    parser.add_argument("--out-json-large",  default="output/output_large_graphs.json")
    parser.add_argument("--out-dir",         default="docs/diagrams")
    args = parser.parse_args()

    small  = read_results(args.out_json_small)   if os.path.exists(args.out_json_small)  else []
    medium = read_results(args.out_json_medium)  if os.path.exists(args.out_json_medium) else []
    large  = read_results(args.out_json_large)   if os.path.exists(args.out_json_large)  else []

    if small:  generate_for_size(small,  "small",  args.out_dir)
    if medium: generate_for_size(medium, "medium", args.out_dir)
    if large:  generate_for_size(large,  "large",  args.out_dir)

    print("Charts saved to:", os.path.abspath(args.out_dir))

if __name__ == "__main__":
    main()
