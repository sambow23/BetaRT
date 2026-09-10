#!/usr/bin/env python3
"""Compare a paired v5 client/runtime against a RAM-only terrain snapshot.

ROOT contains runtime.so, libmcrtx_jni.so, customjar-1.jar, classes/, and the
previously captured terrain-snapshot.bin. This never opens a saved world.
Compile src/tests/java/direct/DirectSnapshotTest.java into classes/ first.
"""
import argparse
import csv
import json
import os
from pathlib import Path
import statistics
import subprocess
import time
from profile_windows import world_frames, require_idle_profile

p = argparse.ArgumentParser(description=__doc__)
p.add_argument("root", type=Path)
p.add_argument("label")
p.add_argument("--java", required=True)
p.add_argument("--libraries", type=Path, required=True)
p.add_argument("--revision", choices=["before", "after"], default="after",
               help="Deliberate lighting-feature removal; not an emissive-quality comparison")
p.add_argument("--indirect", choices=["full", "checkerboard"], default="full")
p.add_argument("--scene", choices=["localhost", "daylight", "moving"], default="localhost")
p.add_argument("--warmup", type=int, default=45000)
p.add_argument("--measure", type=int, default=60000)
p.add_argument("--quality", action="store_true")
a = p.parse_args()
root = a.root.resolve()
out = root / a.label
require_idle_profile()
out.mkdir()
env = os.environ.copy()
for name in tuple(env):
    if name.startswith("MCRTX_PT_") or name in ("LD_PRELOAD", "REMIX_VULKAN_VALIDATION", "VK_LAYER_VALIDATE_SYNC", "VK_LAYER_PATH", "REMIX_LIGHTING_DIAGNOSTICS"):
        env.pop(name)
env.update(SDL_VIDEODRIVER="wayland", WAYLAND_DISPLAY="wayland-0", XDG_RUNTIME_DIR="/run/user/1000",
           VK_DRIVER_FILES="/usr/share/vulkan/icd.d/nvidia_icd.json", LD_LIBRARY_PATH=str(a.libraries.resolve()),
           MCRTX_JNI_PATH=str(root/"libmcrtx_jni.so"), MCRTX_REMIX_PATH=str(root/"runtime.so"),
           MCRTX_RENDER_BACKEND="pathtrace", MCRTX_PT_DENOISER="reblur-sh", MCRTX_PT_TAA="1",
           MCRTX_PT_RTXDI="auto", MCRTX_PT_SHARC="auto", MCRTX_PT_SAMPLES="1", MCRTX_PT_BOUNCES="2",
           MCRTX_PT_ACCUMULATE="0", MCRTX_PT_FORCE_REBUILD="0", MCRTX_VSYNC="0", MANGOHUD="0",
           DISABLE_MANGOHUD="1", MCRTX_PT_PROFILE=str(out/"frames.csv"), MCRTX_PT_SKY="1",
           MCRTX_PT_EMISSIVE_SAMPLER="rtxdi", MCRTX_PT_EMISSIVE_DOWNSCALE="1",
           MCRTX_PT_INDIRECT_SAMPLING=a.indirect, MCRTX_PT_LIGHT_CANDIDATES="8")
cmd = [a.java, "-Xverify:all", "-Djava.awt.headless=true", "-Dmcrtx.test.directGpu=true", "-Dmcrtx.test.benchmark=true",
       "-Dmcrtx.test.width=1920", "-Dmcrtx.test.height=1080", f"-Dmcrtx.test.scene={a.scene}",
       f"-Dmcrtx.test.warmupMs={a.warmup}", f"-Dmcrtx.test.measureMs={a.measure}",
       f"-Dmcrtx.test.quality={str(a.quality).lower()}", "-cp", str(root/("quality-classes" if a.quality else "classes"))+":"+str(root/"customjar-1.jar"),
       "DirectSnapshotTest", str(out), str(root/"terrain-snapshot.bin")]
samples = []
with (out/"game.log").open("w") as log:
    process = subprocess.Popen(cmd, cwd=root, env=env, stdout=log, stderr=subprocess.STDOUT)
    deadline = time.monotonic()+max(420, (a.warmup+a.measure)/1000+240)
    try:
        while process.poll() is None:
            if time.monotonic() > deadline:
                raise RuntimeError("Benchmark timeout")
            require_idle_profile(own_pid=process.pid)
            gpu = subprocess.check_output(["nvidia-smi", "--query-gpu=clocks.sm,clocks.mem,power.draw,temperature.gpu,utilization.gpu,memory.used", "--format=csv,noheader,nounits"], text=True).strip()
            apps = list(csv.reader(subprocess.check_output(["nvidia-smi", "--query-compute-apps=pid,process_name,used_gpu_memory", "--format=csv,noheader,nounits"], text=True).splitlines()))
            own = [float(row[2]) for row in apps if int(row[0]) == process.pid]
            if any(int(row[0]) != process.pid and row[1].strip().endswith("/java") for row in apps):
                raise RuntimeError("Another Java GPU client started; discard this run")
            samples.append(dict(time_ms=int(time.time()*1000), gpu=gpu, process_gpu_mib=max(own, default=0)))
            time.sleep(1)
    finally:
        if process.poll() is None:
            process.terminate()
            try:
                process.wait(timeout=15)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait()
    if process.returncode:
        raise RuntimeError("Benchmark failed: "+a.label)
(out/"load.json").write_text(json.dumps(samples, indent=2))
with (out/"frames.csv").open() as f:
    rows = list(csv.DictReader(f))
window = json.loads((out/"measurement.json").read_text())
presentations = [r for r in rows if window["start_ms"] <= int(r["time_ms"]) < window["end_ms"]]
rows = world_frames(rows, window)
if a.quality:
    assert len(list(out.glob("quality-*.png"))) == 32
for r in rows:
    assert (int(r["denoiser"]), int(r["taa"]), int(r["samples"]), int(r["sky"])) == (1, 1, 1, 1), r
    assert int(r["indirect"]) == int(a.indirect == "checkerboard"), r
    if a.revision == "after":
        assert int(r["sampler"]) == 0xffffffff, r
        for key in ("emitters", "rtxdi", "downscale", "lighting_width", "lighting_height",
                    "primary_shading_locations", "transparent_records", "lightbvh_builds", "lightbvh_refits",
                    "gpu_rtxdi_ms", "gpu_direct_nrd_ms", "gpu_candidates_ms", "gpu_temporal_reuse_ms",
                    "gpu_spatial_reuse_ms", "gpu_visibility_ms", "gpu_reconstruction_ms", "gpu_lightbvh_ms"):
            if key in r:
                assert float(r[key]) == 0, (key, r)
    else:
        assert int(r["sampler"]) == 1 and int(r["downscale"]) == 1, r
result = {k: str(v) if isinstance(v, Path) else v for k, v in vars(a).items()}
result["comparison"] = "deliberate lighting-feature removal"
result["measured_fresh_rows"] = len(rows)
result["interval_population"] = "all presentations within fresh world-frame window"
if not a.quality:
    duration = (window["end_ms"]-window["start_ms"])/1000
    result.update(duration_s=duration, fresh_fps=window["fresh"]/duration,
                  presented_fps=window["presented"]/duration, fresh_ms=duration*1000/window["fresh"])
    intervals = sorted(float(r["interval_ms"]) for r in presentations)
    result["mean_interval_ms"] = statistics.mean(intervals)
    for q in (.5, .95, .99):
        result[f"p{int(q*100)}_ms"] = intervals[int((len(intervals)-1)*q)]
for key in rows[0]:
    if key.startswith("gpu_") or key.startswith("unavailable_") or key in ("cache_queries", "cache_hits", "cache_occupied", "transparent_records"):
        result[key] = statistics.mean(float(r[key]) for r in rows)
for key in ("emitters", "denoise_bytes", "lighting_bytes", "acceleration_bytes", "primary_shading_locations"):
    result[key+"_minmax"] = [min(int(r[key]) for r in rows), max(int(r[key]) for r in rows)]
for key in ("blas_builds", "blas_refits", "static_builds", "dynamic_builds", "dynamic_refits", "history_resets", "cache_resets", "lightbvh_builds", "lightbvh_refits"):
    result[key+"_delta"] = int(rows[-1][key])-int(rows[0][key])
result["peak_process_gpu_mib"] = max(s["process_gpu_mib"] for s in samples)
(out/"result.json").write_text(json.dumps(result, indent=2))
print(json.dumps(result), flush=True)
