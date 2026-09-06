#!/usr/bin/env python3
"""Compare terrain builds in private Prism instances on the graphical GPU host."""
import argparse
import importlib.util
import json
import os
from pathlib import Path
import re
import shutil
import sys
import time


def load_profiler(path):
    spec = importlib.util.spec_from_file_location("terrain_profiler", path)
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


def terrain_counts(log):
    matches = re.findall(r"Terrain: ([^\n]+)", log)
    return [{key: int(value) for key, value in re.findall(r"(\w+)=(\d+)", line)} for line in matches]


def snapshots_idle(log):
    matches = re.findall(r"Terrain snapshots: resident=(\d+) pending=(\d+)", log)
    return bool(matches) and int(matches[-1][0]) > 0 and int(matches[-1][1]) == 0


def main():
    if len(sys.argv) > 2 and sys.argv[1] == "--java-wrapper":
        p = load_profiler(os.environ["BETART_PROFILE_MODULE"])
        private = Path(sys.argv[2])
        descriptor = os.open(str(private / "game-output.log"), os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
        os.dup2(descriptor, 1)
        os.dup2(descriptor, 2)
        os.close(descriptor)
        os.environ.update(MCRTX_PERF="1", MCRTX_PERF_INTERVAL="120", MCRTX_PERF_LOG=str(private / "cpu-perf.log"))
        java = sys.argv[3:]
        java.insert(1, "-Dmcrtx.profile.celestialAngle=0.125")
        p.java_wrapper(str(private), java)
        return
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--profiler", type=Path, required=True)
    parser.add_argument("--instance", type=Path, required=True)
    parser.add_argument("--worker", type=Path, required=True)
    parser.add_argument("--synchronous", type=Path)
    parser.add_argument("--client", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--mode", choices=("worker", "synchronous", "legacy", "all"), default="worker")
    parser.add_argument("--rounds", type=int, default=1)
    parser.add_argument("--manual", action="store_true")
    parser.add_argument("--warmup", type=float, default=45)
    args = parser.parse_args()
    if args.rounds < 1 or args.warmup < 0 or (args.manual and (args.mode == "all" or args.rounds != 1)):
        parser.error("Invalid rounds, warmup or manual mode")
    if args.mode in ("all", "synchronous") and not args.synchronous:
        parser.error("A preserved synchronous build is required")
    args.output = args.output.resolve()
    args.output.mkdir(parents=True, exist_ok=False)
    p = load_profiler(args.profiler)
    os.environ["BETART_PROFILE_MODULE"] = str(args.profiler.resolve())
    p.__file__ = str(Path(__file__).resolve())
    # Match the source instance's Medium / Ultra Performance configuration while
    # fixing the integrator inputs across all three builds, including legacy.
    baseline = p.baseline_options("focused")
    baseline.update({"rtx.pathMinBounces": 1, "rtx.pathMaxBounces": 2, "rtx.risLightSampleCount": 8,
                     "rtx.integrateIndirectMode": 1, "rtx.qualityDLSS": 0})
    p.baseline_options = lambda suite: baseline.copy()
    p.cases = lambda suite: [p.Case(ris=8, bounces=2, gi=1)]
    mode = args.mode
    def configure_private(game, suite):
        overrides = {"MCRTX_UNDERGROUND_CULLING_ENABLED": "0", "MCRTX_RT_QUALITY": "Medium",
                     "MCRTX_UPSCALER_TYPE": "DLSS", "MCRTX_DLSS_PRESET": "UltraPerf"}
        if mode != "legacy":
            for source, name in ((args.worker if mode == "worker" else args.synchronous, "libmcrtx_jni.so"),
                                 (args.client, "customjar-1.jar")):
                target = game.parent / "libraries" / name
                if not target.resolve().is_relative_to(game.parent):
                    raise RuntimeError("Private library escapes instance")
                shutil.copy2(source, target)
        path = game / "mcrtx-runtime.env"
        lines = [line for line in path.read_text().splitlines() if line.partition("=")[0].strip() not in overrides]
        p.atomic_text(path, "\n".join(lines + [f"{key}={value}" for key, value in overrides.items()]) + "\n")
        return overrides
    p.configure_runtime = configure_private
    files = ("instance.cfg", "minecraft/rtx.conf", "minecraft/user.conf", "minecraft/options.txt",
             "minecraft/mcrtx-runtime.env", "libraries/libmcrtx_jni.so", "libraries/customjar-1.jar")
    source = args.instance.resolve()
    before = {name: p.sha256(source / name) for name in files}
    results = []
    pose = None
    session_args = argparse.Namespace(instance=source, runtime=None, suite="focused", architecture="Turing",
        ngfx="ngfx", prism="prismlauncher", initial_warmup=args.warmup, warmup=5, stable=5,
        measure=15, min_frames=120, max_measure=60, timeout=300)
    for round_index in range(args.rounds):
        modes = ["synchronous", "worker", "legacy"] if args.mode == "all" else [args.mode]
        if round_index % 2:
            modes.reverse()
        for mode in modes:
            label = f"round-{round_index + 1}-{mode}"
            session = p.Session(session_args, args.output / label)
            started = time.monotonic()
            started_unix = time.time()
            try:
                session.launch()
                session.settle(initial=True)
                if mode != "legacy":
                    deadline = time.monotonic() + 300
                    while True:
                        session.check()
                        log = (session.private / "game-output.log").read_text(errors="replace")
                        counts = terrain_counts(log)
                        if snapshots_idle(log) and counts and counts[-1]["resident"] > 0 and counts[-1]["published"] == counts[-1]["resident"] and counts[-1]["pending"] == 0 and counts[-1]["dispatched"] == 0:
                            break
                        if time.monotonic() > deadline:
                            raise RuntimeError(f"Terrain did not settle: {counts[-1:]}")
                        time.sleep(.5)
                    session.settle()
                cold_seconds = time.monotonic() - started
                if args.manual:
                    stationary = session.window(session.initial_case, label + "-stationary")
                    indirect = stationary["metrics"]["Integrate Indirect Raytracing"]["median"]
                    if stationary["invalid_frames"] or indirect is None or indirect < .01:
                        raise RuntimeError("Indirect lighting is inactive before movement")
                    counts = terrain_counts((session.private / "game-output.log").read_text(errors="replace"))
                    if mode != "legacy" and (len(counts) < 3 or len({(c["builds"], c["meshCreates"], c["resident"]) for c in counts[-3:]}) != 1):
                        raise RuntimeError("Stationary terrain did not remain idle before movement")
                    stationary.update(mode=mode, phase="stationary", cold_settle_seconds=cold_seconds, terrain=counts[-3:])
                    results.append(stationary)
                    p.atomic_text(session.output / "stationary.json", json.dumps(stationary, indent=2))
                    print(f"READY for movement in {mode}. Private instance: {session.private}. Close game to finish.", flush=True)
                    session.send(session.initial_case, measure=True)
                    manual_started = time.time()
                    while session.owns_java():
                        session.check(require_connection=False)
                        time.sleep(.5)
                    rows = [row for row in p.frame_rows(session.output / "frames.csv")
                            if row["measure"] == "1" and row["ready"] == "1" and row["InjectRTX"]]
                    results.append({"mode": mode, "manual": True, "started_unix": manual_started,
                                    "cold_settle_seconds": cold_seconds, "frames": len(rows),
                                    "metrics": p.summarize(rows), "path_requires_user_review": True})
                    continue
                if pose is not None and not p.same_pose(pose, session.pose):
                    raise RuntimeError("Camera changed between comparison runs")
                pose = session.pose
                result = session.window(session.initial_case, label)
                indirect = result["metrics"]["Integrate Indirect Raytracing"]["median"]
                if result["invalid_frames"] or indirect is None or indirect < .01:
                    raise RuntimeError("Invalid timing or inactive indirect lighting; reject measurement")
                counts = terrain_counts((session.private / "game-output.log").read_text(errors="replace"))
                if mode != "legacy":
                    if len(counts) < 3 or len({(c["builds"], c["meshCreates"], c["resident"]) for c in counts[-3:]}) != 1:
                        raise RuntimeError("Stationary terrain did not remain idle")
                result.update(mode=mode, cold_settle_seconds=cold_seconds, terrain=counts[-3:])
                results.append(result)
            finally:
                if session.owns_java():
                    shutil.copy2(Path(f"/proc/{session.pid}/maps"), session.output / "native-maps.txt")
                session.close()
                for name in ("game-output.log", "cpu-perf.log"):
                    if session.private and (session.private / name).exists():
                        shutil.copy2(session.private / name, session.output / name)
                if before != {name: p.sha256(source / name) for name in files}:
                    raise RuntimeError("Source instance changed during verification")
                if session.private and hasattr(session, "game"):
                    for crash in session.game.glob("hs_err_pid*.log"):
                        if crash.stat().st_mtime >= started_unix:
                            shutil.copy2(crash, session.output / crash.name)
                p.atomic_text(args.output / "results.json", json.dumps({"source_unchanged": True,
                    "source_hashes": before, "worker_sha256": p.sha256(args.worker),
                    "synchronous_sha256": p.sha256(args.synchronous) if args.synchronous else None,
                    "client_sha256": p.sha256(args.client), "results": results,
                    "visual_geometry_and_movement_checks_required": True}, indent=2))


if __name__ == "__main__":
    main()
