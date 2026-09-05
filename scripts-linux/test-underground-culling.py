#!/usr/bin/env python3
"""Run OFF/ON comparisons in private Prism copies on the graphical GPU host."""

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
    spec = importlib.util.spec_from_file_location("betart_profiler", path)
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


def main():
    if len(sys.argv) > 2 and sys.argv[1] == "--java-wrapper":
        module = load_profiler(os.environ["BETART_PROFILE_MODULE"])
        descriptor = os.open(str(Path(sys.argv[2]) / "game-output.log"), os.O_WRONLY | os.O_CREAT | os.O_TRUNC, 0o600)
        os.dup2(descriptor, 1)
        os.dup2(descriptor, 2)
        os.close(descriptor)
        java = sys.argv[3:]
        os.environ["MCRTX_PERF"] = "1"
        os.environ["MCRTX_PERF_INTERVAL"] = "120"
        os.environ["MCRTX_PERF_LOG"] = str(Path(sys.argv[2]) / "cpu-perf.log")
        java.insert(1, "-Dmcrtx.profile.celestialAngle=" + os.environ["BETART_PROFILE_CELESTIAL_ANGLE"])
        module.java_wrapper(sys.argv[2], java)
        return

    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--profiler", type=Path, required=True, help="dxvk-remix-gmod/scripts-linux/profile-betart.py")
    parser.add_argument("--instance", type=Path, required=True)
    parser.add_argument("--jni", type=Path, required=True)
    parser.add_argument("--client", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--mode", choices=("both", "off", "on"), default="both")
    parser.add_argument("--rounds", type=int, default=1)
    parser.add_argument("--warmup", type=float, default=45)
    parser.add_argument("--celestial-angle", type=float, default=0.125, help="Fixed client sky angle in [0,1), default daytime")
    parser.add_argument("--manual", action="store_true", help="Keep one private game open for movement/entrance checks, without benchmarking")
    args = parser.parse_args()
    if args.rounds <= 0 or args.warmup < 0 or not 0 <= args.celestial_angle < 1:
        parser.error("rounds must be positive, warmup nonnegative, and celestial-angle in [0,1)")
    if args.manual and (args.mode == "both" or args.rounds != 1):
        parser.error("manual verification needs --mode on or --mode off and one round")
    args.output = args.output.resolve()
    args.output.mkdir(parents=True, exist_ok=False)
    os.environ["BETART_PROFILE_MODULE"] = str(args.profiler.resolve())
    os.environ["BETART_PROFILE_CELESTIAL_ANGLE"] = str(args.celestial_angle)
    p = load_profiler(args.profiler)
    p.__file__ = str(Path(__file__).resolve())
    configure = p.configure_runtime
    enabled = False

    def configure_private(game, suite):
        values = configure(game, suite)
        for source, relative in ((args.jni, "libmcrtx_jni.so"), (args.client, "customjar-1.jar")):
            target = game.parent / "libraries" / relative
            if not target.resolve().is_relative_to(game.parent):
                raise RuntimeError("Library symlink escapes private instance")
            shutil.copy2(source, target)
        path = game / "mcrtx-runtime.env"
        overrides = {"MCRTX_UNDERGROUND_CULLING_ENABLED": "1" if enabled else "0"}
        lines = [line for line in path.read_text().splitlines() if line.partition("=")[0].strip() not in overrides]
        p.atomic_text(path, "\n".join(lines + [f"{key}={value}" for key, value in overrides.items()]) + "\n")
        return values | overrides

    p.configure_runtime = configure_private
    source = args.instance.resolve()
    source_files = ["instance.cfg", "minecraft/rtx.conf", "minecraft/user.conf", "minecraft/options.txt",
                    "minecraft/mcrtx-runtime.env", "libraries/libmcrtx_jni.so", "libraries/customjar-1.jar"]
    before = {name: p.sha256(source / name) for name in source_files}
    results = []
    pose = None
    session_args = argparse.Namespace(instance=source, runtime=None, suite="ultra-shadow", architecture="Turing",
        ngfx="ngfx", prism="prismlauncher", initial_warmup=args.warmup, warmup=5, stable=5,
        measure=10, min_frames=100, max_measure=45, timeout=300)
    for round_index in range(args.rounds):
        modes = [False, True] if args.mode == "both" else [args.mode == "on"]
        if round_index % 2:
            modes.reverse()
        for enabled in modes:
            label = f"round-{round_index + 1}-{'on' if enabled else 'off'}"
            session = p.Session(session_args, args.output / label)
            try:
                session.launch()
                if args.manual:
                    print(f"Private manual session: {session.private}. Move freely; close the game to finish.", flush=True)
                    while True:
                        try:
                            session.check(require_connection=False)
                        except RuntimeError:
                            if session.pid and not Path(f"/proc/{session.pid}").exists():
                                break
                            raise
                        time.sleep(1)
                    continue
                session.settle(initial=True)
                if enabled:
                    deadline = time.monotonic() + 300
                    while True:
                        session.check()
                        log = (session.private / "game-output.log").read_text(errors="replace")
                        columns = re.findall(r"Underground: .*pendingColumns=(\d+)", log)
                        groups = re.findall(r"Underground submissions: .*pendingGroups=(\d+)", log)
                        worker = re.findall(r"Underground: .*pendingWorker=(\d+).*active=(true|false)", log)
                        if columns and groups and worker and columns[-1] == groups[-1] == "0" and worker[-1] == ("0", "true"):
                            break
                        if time.monotonic() >= deadline:
                            raise RuntimeError(f"Culling did not settle: columns={columns[-1:]} groups={groups[-1:]}")
                        time.sleep(1)
                    session.settle()
                if pose is not None and not p.same_pose(pose, session.pose):
                    raise RuntimeError("Camera changed between runs")
                pose = session.pose
                result = session.window(session.initial_case, label)
                if result["invalid_frames"]:
                    raise RuntimeError("Invalid timestamp frames")
                indirect = result["metrics"]["Integrate Indirect Raytracing"]["median"]
                if indirect is None or indirect < 0.01:
                    raise RuntimeError("Indirect pass did not record meaningful work; do not compare these timings")
                log = (session.private / "game-output.log").read_text(errors="replace")
                sky = re.findall(r"Profiling celestial angle fixed at ([0-9.eE+-]+)", log)
                if not sky or abs(float(sky[-1]) - args.celestial_angle) > 0.000001:
                    raise RuntimeError("Client did not acknowledge the fixed profiling sky")
                maps = Path(f"/proc/{session.pid}/maps").read_text()
                if str(session.game.parent / "libraries" / "libmcrtx_jni.so") not in maps:
                    raise RuntimeError("New JNI library was not loaded")
                records = re.findall(r"\[mcrtx\] (Underground(?: submissions)?: .*)", log)
                counters = re.findall(r"Underground: .*queries=(\d+).*topologyUploads=(\d+) maskUploads=(\d+) active=(true|false)", log)
                if enabled and (len(counters) < 3 or len(set(counters[-3:])) != 1 or counters[-1][-1] != "true"):
                    raise RuntimeError("Region traversal/uploads did not stay idle during the stationary measurement")
                cpu = (session.private / "cpu-perf.log").read_text()
                sites = ("hook.undergroundVisibility", "presentLocked.prepareSnapshot", "RemixRenderer::rebuildFireMesh")
                timings = {}
                for site in sites:
                    samples = re.findall(r"site=" + re.escape(site) + r" calls=(\d+) avgUs=([0-9.]+) maxUs=([0-9.]+)", cpu)[-3:]
                    if samples:
                        timings[site] = {"avg_us": sum(int(n)*float(avg) for n, avg, _ in samples) / sum(int(n) for n, _, _ in samples),
                                         "max_us": max(float(peak) for _, _, peak in samples)}
                if enabled and "hook.undergroundVisibility" not in timings:
                    raise RuntimeError("Game-thread culling CPU scope was not recorded")
                result.update({"enabled": enabled, "round": round_index + 1, "culling_status": records[-6:],
                               "cpu_recent_intervals": timings})
                results.append(result)
            finally:
                if session.private and (session.private / "game-output.log").exists():
                    shutil.copy2(session.private / "game-output.log", session.output / "game-output.log")
                session.close()
                if session.private and (session.private / "cpu-perf.log").exists():
                    shutil.copy2(session.private / "cpu-perf.log", session.output / "cpu-perf.log")
                after = {name: p.sha256(source / name) for name in source_files}
                if after != before:
                    raise RuntimeError("Source instance changed during verification")
                p.atomic_text(args.output / "results.json", json.dumps({
                    "source_unchanged": True, "source_hashes": before, "jni_sha256": p.sha256(args.jni),
                    "client_sha256": p.sha256(args.client), "results": results,
                    "celestial_angle": args.celestial_angle,
                    "manual_cave_and_motion_checks_required": True}, indent=2))
    print(f"Completed private comparison: {args.output}", flush=True)


if __name__ == "__main__":
    main()
