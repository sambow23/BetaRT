"""Summarize mcrtx profiling artifacts.

Reads the aggregated `mcrtx-perf.log` (always) and the optional
`mcrtx-perf-trace.jsonl` if present, and prints the tables that are most
useful day-to-day: hottest sites by total time, top stutter sites by max,
and p50/p95/p99 distributions from the per-call trace.

Usage:
    python scripts/analyze-perf.py [--dir <dir>] [--top N] [--trace]

If `--dir` is omitted, looks next to `mcrtx_jni.dll` under
`out/patched-client/` relative to the repo root.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

try:
    import pandas as pd
except ImportError:
    sys.exit("pandas is required: pip install pandas")

_KV = re.compile(r"(\w+)=([^\s]+)")


def load_aggregate(path: Path) -> pd.DataFrame:
    rows = []
    with path.open("r", encoding="utf-8", errors="replace") as handle:
        for line in handle:
            if "[mcrtx-perf]" not in line:
                continue
            rows.append({k: v for k, v in _KV.findall(line)})
    if not rows:
        return pd.DataFrame()
    df = pd.DataFrame(rows)
    for column in ("t", "frames", "calls", "avgUs", "maxUs", "samples", "avgCount", "maxCount"):
        if column in df:
            df[column] = pd.to_numeric(df[column], errors="coerce")
    if "calls" in df and "avgUs" in df:
        df["totalUs"] = df["calls"].fillna(0) * df["avgUs"].fillna(0)
    else:
        df["totalUs"] = 0.0
    return df


def load_trace(path: Path) -> pd.DataFrame:
    rows = []
    with path.open("r", encoding="utf-8", errors="replace") as handle:
        for line in handle:
            line = line.strip()
            if not line:
                continue
            try:
                rows.append(json.loads(line))
            except json.JSONDecodeError:
                continue
    return pd.DataFrame(rows)


def print_aggregate_summary(df: pd.DataFrame, top: int) -> None:
    if df.empty:
        print("Aggregate log is empty (no flushes recorded).")
        return

    duration_df = df[df.get("calls").notna()] if "calls" in df else df
    count_df = df[df.get("avgCount").notna()] if "avgCount" in df else pd.DataFrame()

    if not duration_df.empty:
        windows = duration_df.groupby(["side", "site"]).agg(
            flushes=("calls", "size"),
            calls=("calls", "sum"),
            total_ms=("totalUs", lambda s: s.sum() / 1000.0),
            avg_us=("avgUs", "mean"),
            max_us=("maxUs", "max"),
        )

        print(f"\n=== Hottest sites by total time (top {top}) ===")
        hot = windows.sort_values("total_ms", ascending=False).head(top)
        print(hot.to_string(float_format=lambda v: f"{v:10.2f}"))

        print(f"\n=== Top stutter sites by max single-call (top {top}) ===")
        stutter = windows.sort_values("max_us", ascending=False).head(top)
        print(stutter.to_string(float_format=lambda v: f"{v:10.2f}"))

        # Pick only leaf sites for "time share" so sub-sites aren't counted
        # both on their own and via their parent. A site X is a leaf if no
        # other site starts with "X.".
        all_sites = set(duration_df["site"].unique())
        leaf_mask = duration_df["site"].apply(
            lambda s: not any(other != s and other.startswith(s + ".") for other in all_sites)
        )
        leaf_df = duration_df[leaf_mask]

        print("\n=== Time share by side (leaf sites only, parents with children excluded) ===")
        share = leaf_df.groupby("side")["totalUs"].sum().sort_values(ascending=False)
        total = share.sum() or 1.0
        for side, total_us in share.items():
            print(f"  {side:<8} {total_us / 1000.0:12.2f} ms  ({100.0 * total_us / total:5.1f}%)")

        # Surface parents with children so user knows what got excluded and
        # can drill in if a parent's total differs markedly from its children.
        non_leaf = duration_df[~leaf_mask]
        if not non_leaf.empty:
            parent_totals = non_leaf.groupby("site")["totalUs"].sum() / 1000.0
            child_totals = {}
            for parent in parent_totals.index:
                prefix = parent + "."
                mask = duration_df["site"].str.startswith(prefix)
                child_totals[parent] = duration_df[mask]["totalUs"].sum() / 1000.0
            print("\n=== Parent/child overlap (wallclock should match when children cover the parent) ===")
            for parent, parent_ms in parent_totals.sort_values(ascending=False).items():
                child_ms = child_totals.get(parent, 0.0)
                delta = parent_ms - child_ms
                print(f"  {parent:<40} parent={parent_ms:10.2f} ms  children={child_ms:10.2f} ms  delta={delta:+9.2f} ms")

    if not count_df.empty:
        counts = count_df.groupby(["side", "site"]).agg(
            flushes=("samples", "size"),
            samples=("samples", "sum"),
            avg=("avgCount", "mean"),
            peak=("maxCount", "max"),
        )
        print(f"\n=== Count metrics (avg/peak per flush) ===")
        print(counts.sort_values("avg", ascending=False).to_string(float_format=lambda v: f"{v:12.2f}"))


def print_trace_summary(df: pd.DataFrame, top: int) -> None:
    if df.empty:
        print("Trace file is empty.")
        return

    if "us" not in df or "site" not in df:
        print("Trace is missing expected fields (us, site).")
        return

    print(f"\n=== Per-call distribution (top {top} sites by count) ===")
    counts = df.groupby(["side", "site"]).size().rename("count")
    top_sites = counts.sort_values(ascending=False).head(top).index
    dist = (
        df.set_index(["side", "site"]).loc[top_sites]
        .groupby(level=[0, 1])["us"]
        .quantile([0.5, 0.95, 0.99])
        .unstack()
    )
    dist.columns = ["p50_us", "p95_us", "p99_us"]
    dist["count"] = counts.loc[top_sites]
    dist["max_us"] = df.groupby(["side", "site"])["us"].max().loc[top_sites]
    print(dist.sort_values("p99_us", ascending=False).to_string(float_format=lambda v: f"{v:10.2f}"))


def main() -> int:
    repo_root = Path(__file__).resolve().parent.parent
    default_dir = repo_root / "out" / "patched-client"

    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dir", type=Path, default=default_dir,
                        help="directory containing the perf artifacts")
    parser.add_argument("--log", type=Path, help="override aggregate log path")
    parser.add_argument("--trace", type=Path, nargs="?", const=True,
                        help="include trace analysis; optional explicit path")
    parser.add_argument("--top", type=int, default=20, help="rows to show per table")
    args = parser.parse_args()

    log_path = args.log or (args.dir / "mcrtx-perf.log")
    if not log_path.is_file():
        sys.exit(f"Aggregate log not found: {log_path}")

    print(f"Aggregate log: {log_path}")
    aggregate = load_aggregate(log_path)
    print_aggregate_summary(aggregate, args.top)

    if args.trace:
        trace_path = args.trace if isinstance(args.trace, Path) else (args.dir / "mcrtx-perf-trace.jsonl")
        if not trace_path.is_file():
            print(f"\nTrace not found at {trace_path}; skipping.")
        else:
            print(f"\nTrace: {trace_path}")
            print_trace_summary(load_trace(trace_path), args.top)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
