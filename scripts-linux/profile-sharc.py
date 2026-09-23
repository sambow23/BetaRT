#!/usr/bin/env python3
"""Profile SharcProfileTest against an immutable, RAM-only server terrain snapshot.

ROOT contains classes/SharcProfileTest.class, libsharc_profile.so and
terrain-snapshot.bin. Each run
gets a new directory. No save, server, or installed instance is modified.
Compile the fixture against the same client JAR supplied through --libraries.
"""
import argparse
import csv
import hashlib
import json
import math
import os
from pathlib import Path
import signal
import statistics
import struct
import subprocess
import time

from profile_windows import require_idle_profile, world_frames


def number(value):
    try:
        result = float(value)
        return result if math.isfinite(result) else None
    except (ValueError, TypeError):
        return None


def summarize(out):
    window = json.loads((out / 'measurement.json').read_text())
    with (out / 'frames.csv').open() as file:
        all_rows = list(csv.DictReader(file))
    rows = world_frames(all_rows, window)
    presentations = [r for r in all_rows if window['start_ms'] <= int(r['time_ms']) < window['end_ms']]
    duration = (window['end_ms'] - window['start_ms']) / 1000
    gpu_frames = window['gpu_after'][0] - window['gpu_before'][0]
    if abs(gpu_frames - window['fresh']) > 4:
        raise RuntimeError(f'World GPU frames ({gpu_frames}) differ from submitted frames ({window["fresh"]}); stale timings')
    result = dict(duration_s=duration, fresh_fps=window['fresh'] / duration,
                  presented_fps=window['presented'] / duration, fresh_ms=duration * 1000 / window['fresh'],
                  fresh_rows=len(rows), gpu_frames=gpu_frames, reconstruction=window['gpu_after'][2:8], mean_ms={}, median_ms={})
    intervals = sorted(float(r['interval_ms']) for r in presentations)
    for p in (.5, .95, .99):
        result[f'p{int(p*100)}_ms'] = intervals[int((len(intervals)-1)*p)]
    for key in rows[0]:
        if key.startswith('gpu_'):
            # Disabled caches retain allocations and their last diagnostics.
            # A stale update/resolve timestamp is not work in the off phase.
            values = [0.0 if key.startswith('gpu_sharc_') and r.get('sharc') == '0' else number(r[key]) for r in rows]
            values = [v for v in values if v is not None]
            if values:
                result['mean_ms'][key] = statistics.mean(values)
                result['median_ms'][key] = statistics.median(values)
    for key in ('cache_occupied', 'cache_resets', 'history_resets', 'sharc', 'denoiser', 'taa', 'local_lights',
                'local_active_lights', 'samples', 'lighting_bytes', 'static_builds', 'dynamic_builds'):
        values = [number(r.get(key)) for r in rows]
        values = [v for v in values if v is not None]
        if values:
            result[key] = dict(first=values[0], last=values[-1], min=min(values), max=max(values), mean=statistics.mean(values))
    attempts = sum(float(r.get('sharc_sampled_queries') or 0) for r in rows if r.get('sharc') == '1')
    hits = sum(float(r.get('sharc_sampled_hits') or 0) for r in rows if r.get('sharc') == '1')
    result['sampled_queries_per_frame'] = attempts / len(rows)
    result['sampled_hits_per_frame'] = hits / len(rows)
    result['sampled_hit_rate'] = hits / attempts if attempts else 0
    result['steady_scene'] = all(result[k]['min'] == result[k]['max'] for k in
                                ('static_builds', 'local_lights', 'cache_resets', 'history_resets'))
    if not result['steady_scene']:
        (out / 'rejected-result.json').write_text(json.dumps(result, indent=2) + '\n')
        raise RuntimeError('Terrain, lighting or cache changed during measurement; extend warmup')
    # Trace already includes SHARC update/resolve. Never add them to this sum.
    keys = ('gpu_trace_ms', 'gpu_confidence_ms', 'gpu_nrd_ms', 'gpu_taa_ms')
    result['lighting_and_denoise_ms'] = sum(result['mean_ms'].get(k, 0) for k in keys)
    if (out / 'lighting-timings.csv').exists():
        with (out / 'lighting-timings.csv').open() as file:
            lighting = [r for r in csv.DictReader(file)
                        if window['start_ms'] <= int(r['time_ms']) < window['end_ms']]
        if not lighting:
            raise RuntimeError('No total GPU timings in measurement window')
        result['lighting_timing_rows'] = len(lighting)
        result['gpu_total_ms'] = statistics.mean(float(r['gpu_total_ms']) for r in lighting)
        result['gpu_prepare_ms'] = statistics.mean(float(r['gpu_prepare_ms']) for r in lighting)
        result['lighting_and_denoise_ms'] = result['gpu_total_ms'] + sum(
            result['mean_ms'].get(k, 0) for k in keys[1:])
    if (out / 'stages.csv').exists():
        with (out / 'stages.csv').open() as file:
            stages = [r for r in csv.DictReader(file) if window['start_ms'] <= int(r['time_ms']) < window['end_ms']]
        if not stages:
            raise RuntimeError('No world GPU renders in measurement window; presentation timestamps are stale')
        result['stage_rows'] = len(stages)
        result['stages_ms'] = {k: statistics.mean(float(r[k]) for r in stages) for k in stages[0] if k != 'time_ms'}
    (out / 'result.json').write_text(json.dumps(result, indent=2) + '\n')
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('root', type=Path)
    parser.add_argument('label')
    parser.add_argument('--libraries', type=Path, required=True)
    parser.add_argument('--java', type=Path, required=True)
    parser.add_argument('--renderer', type=Path, required=True, help='Build supporting REMIX_PROFILE_EVERY_FRAME')
    parser.add_argument('--sharc', choices=('0', '1'), required=True)
    parser.add_argument('--bounces', type=int, default=1)
    parser.add_argument('--roulette', choices=('0', '1'), default='0')
    parser.add_argument('--roulette-min', type=int, choices=range(1, 5), default=1)
    parser.add_argument('--camera', type=Path, help='JSON array [eyeX,eyeY,eyeZ,yaw,pitch] overriding the snapshot camera')
    parser.add_argument('--capacity', type=int, default=22)
    parser.add_argument('--downscale', type=int, default=5)
    parser.add_argument('--warmup', type=int, default=10000)
    parser.add_argument('--measure', type=int, default=20000)
    parser.add_argument('--stages', action='store_true')
    parser.add_argument('--lighting-timings', action='store_true', help='Private total GPU timing using existing queries')
    parser.add_argument('--restir-diagnostics', action='store_true', help='Diagnostic-only RTXDI counters and pass timings')
    parser.add_argument('--state', action='store_true', help='Per-frame pipeline/buffer state; also enables stage diagnostics')
    parser.add_argument('--device-local-scene', action='store_true', help='Experimental scene tables in device-local memory')
    parser.add_argument('--validation-layers', type=Path, help='Validation-only run: directory containing the Vulkan layer manifest')
    parser.add_argument('--validation-libraries', type=Path, help='Validation layer shared-library directory')
    parser.add_argument('--rays', action='store_true')
    parser.add_argument('--capture', action='store_true', help='PNG after the timing window')
    parser.add_argument('--motion', action='store_true', help='Diagnostic motion; not a paired timing comparison')
    parser.add_argument('--split-indirect', action='store_true', help='Existing experimental separate continuation dispatch')
    parser.add_argument('--inline-indirect', action='store_true', help='Force inline continuation in either cache mode')
    parser.add_argument('--pipeline-stats', type=Path, help='Diagnostic Vulkan pipeline-statistics preload library')
    parser.add_argument('--phases', type=Path, help='CSV: name,sharc,bounces,capacity,downscale[,dispatch[,placement[,roulette[,rouletteMin[,torchSampler[,rtxdiProposal]]]]]]; dispatch 0=inline,1=split,a=auto; placement 0=host,1=device')
    parser.add_argument('--upscaler', choices=('dlss', 'off'), default='dlss')
    args = parser.parse_args()
    if args.split_indirect and args.inline_indirect:
        parser.error('Choose split or inline indirect, not both')
    if not 0 <= args.bounces <= 4 or not 16 <= args.capacity <= 22 or not 1 <= args.downscale <= 16:
        parser.error('Invalid bounce count, cache capacity or update downscale')
    if args.warmup < 10000 or args.measure <= 0:
        parser.error('Use at least 10 seconds of warmup and a positive measurement duration')
    phases = [row for row in csv.reader(args.phases.read_text().splitlines()) if row] if args.phases else []
    if any(not 5 <= len(row) <= 11 or (len(row) >= 6 and row[5] not in ('0', '1', 'a')) or
           (len(row) >= 7 and row[6] not in ('0', '1')) or
           (len(row) >= 8 and row[7] not in ('0', '1')) or
           (len(row) >= 9 and row[8] not in ('1', '2', '3', '4')) or
           (len(row) >= 10 and row[9] not in ('lightbvh', 'rtxdi')) or
           (len(row) >= 11 and row[10] not in ('lightbvh', 'regir')) for row in phases):
        parser.error('Invalid phase CSV')
    camera = json.loads(args.camera.read_text()) if args.camera else None
    if camera is not None and (not isinstance(camera, list) or len(camera) != 5 or
                               any(type(v) not in (int, float) or not math.isfinite(v) for v in camera)):
        parser.error('Camera must contain five finite numbers')
    require_idle_profile()
    root, libraries = args.root.resolve(), args.libraries.resolve()
    out = root / args.label
    out.mkdir(exist_ok=False)
    renderer = args.renderer.resolve() if args.renderer else libraries / 'libremix.so.0.1003.0'
    env = {k: v for k, v in os.environ.items() if not k.startswith(('MCRTX_', 'REMIX_', 'VK_', 'MANGOHUD')) and k != 'LD_PRELOAD'}
    env.update(MCRTX_RENDER_BACKEND='pathtrace', MCRTX_VSYNC='0', MCRTX_LOAD_PBR='1',
               MCRTX_ASSETS=str(libraries / 'mcrtx_assets'), MCRTX_GAMEPLAY_FOV='90', MCRTX_VIEWMODEL_FOV='70',
               MCRTX_OPAQUE_WATER_ICE='1', MCRTX_PT_SEPARATE_LOCAL_LIGHTING='3',
               MCRTX_PT_UPSCALER=args.upscaler, MCRTX_PT_DLSS_QUALITY='quality', MCRTX_PT_DLSS_RR='0',
               MCRTX_PT_LOCAL_DIRECT_CLAMP='0', MCRTX_PT_INDIRECT_CLAMP='0', MCRTX_PT_LOCAL_FIREFLY_FILTER='1',
               MCRTX_PT_DENOISER='reblur-sh', MCRTX_PT_TAA='1', MCRTX_PT_SAMPLES='1',
               MCRTX_PT_BOUNCES=str(args.bounces), MCRTX_PT_ACCUMULATE='0',
               MCRTX_PT_RUSSIAN_ROULETTE=args.roulette, MCRTX_PT_ROULETTE_MIN_BOUNCES=str(args.roulette_min),
               MCRTX_PT_SHARC=args.sharc, MCRTX_PT_SHARC_CAPACITY_LOG2=str(args.capacity),
               MCRTX_PT_SHARC_DOWNSCALE=str(args.downscale), MCRTX_PT_SHARC_SCALE='50',
               MCRTX_JNI_PATH=str(libraries / 'libmcrtx_jni.so'), MCRTX_REMIX_PATH=str(renderer),
               MCRTX_PT_PROFILE=str(out / 'frames.csv'), LD_LIBRARY_PATH=str(libraries),
               REMIX_PROFILE_EVERY_FRAME='1',
               SDL_VIDEODRIVER='x11', VK_DRIVER_FILES='/usr/share/vulkan/icd.d/nvidia_icd.json',
               MANGOHUD='0', DISABLE_MANGOHUD='1')
    if args.stages or args.state:
        env['REMIX_STACK_PROFILE'] = str(out / 'stages.csv')
    if args.lighting_timings:
        env['REMIX_LIGHTING_TIMINGS'] = str(out / 'lighting-timings.csv')
    if args.restir_diagnostics:
        env['REMIX_RESTIR_DIAGNOSTICS'] = str(out / 'restir.csv')
    if args.state:
        env['REMIX_TRACE_STATE'] = str(out / 'state.csv')
    if args.device_local_scene:
        env['REMIX_DEVICE_LOCAL_SCENE'] = '1'
    if args.validation_layers:
        env.update(REMIX_VULKAN_VALIDATION='1', VK_LAYER_VALIDATE_SYNC='1',
                   VK_LAYER_PATH=str(args.validation_layers.resolve()))
        if args.validation_libraries:
            env['LD_LIBRARY_PATH'] += ':' + str(args.validation_libraries.resolve())
    if any(len(row) >= 6 for row in phases):
        env['REMIX_INDIRECT_MODE_FILE'] = str(out / 'indirect-mode.txt')
    if args.rays:
        env['REMIX_RAY_PROFILE'] = str(out / 'rays.csv')
    if args.split_indirect:
        env['REMIX_SPLIT_INDIRECT'] = '1'
    if args.inline_indirect:
        env['REMIX_SPLIT_INDIRECT'] = '0'
    if args.pipeline_stats:
        env['LD_PRELOAD'] = str(args.pipeline_stats.resolve())
        env['REMIX_PIPELINE_STATS_LOG'] = str(out / 'pipelines.tsv')
    # No inherited launch file or user options; these match the current game's
    # view distance, PBR pack and ordinary graphics settings.
    (out / 'options.txt').write_text('music:0.0\nsound:0.0\nviewDistance:2\nfancyGraphics:true\nao:false\nskin:Default\nfpsLimit:0\n')
    command = [str(args.java), '-Xms512m', '-Xmx2048m', '-Djava.awt.headless=true',
               '-Dmcrtx.test.directGpu=true', f'-Dmcrtx.test.warmupMs={args.warmup}',
               '-Dmcrtx.test.profileHelper=' + str(root / 'libsharc_profile.so'),
               f'-Dmcrtx.test.measureMs={args.measure}', f'-Dmcrtx.test.capture={str(args.capture).lower()}',
               f'-Dmcrtx.test.motion={str(args.motion).lower()}', '-cp',
               str(root / 'classes') + ':' + str(libraries / 'customjar-1.jar'),
               'SharcProfileTest', str(out), str(root / 'terrain-snapshot.bin')]
    if args.phases:
        command.insert(1, '-Dmcrtx.test.phases=' + str(args.phases.resolve()))
    if camera is not None:
        command.insert(1, '-Dmcrtx.test.camera=' + ','.join(map(str, camera)))
    artifacts = [renderer, libraries / 'libmcrtx_jni.so', libraries / 'customjar-1.jar', root / 'terrain-snapshot.bin',
                 root / 'classes/SharcProfileTest.class', root / 'libsharc_profile.so']
    if args.pipeline_stats:
        artifacts.append(args.pipeline_stats.resolve())
    if args.camera:
        artifacts.append(args.camera.resolve())
    (out / 'launch.json').write_text(json.dumps(dict(command=command, settings=vars(args),
        env={k: v for k, v in env.items() if k.startswith(('MCRTX_', 'REMIX_', 'VK_', 'SDL_')) or k == 'LD_PRELOAD'},
        sha256={str(p): hashlib.sha256(p.read_bytes()).hexdigest() for p in artifacts}), indent=2, default=str) + '\n')
    print('Starting ' + args.label, flush=True)
    samples = []
    with (out / 'client.log').open('w') as log:
        process = subprocess.Popen(command, cwd=out, env=env, stdout=log, stderr=subprocess.STDOUT, start_new_session=True)
        deadline = time.monotonic() + max(1, len(phases)) * (max(180000, args.warmup) + args.measure)/1000 + 300
        try:
            while process.poll() is None:
                if time.monotonic() > deadline:
                    raise TimeoutError('Profiling client timed out')
                require_idle_profile(own_pid=process.pid)
                status = subprocess.run(['nvidia-smi', '--query-gpu=utilization.gpu,utilization.memory,clocks.sm,clocks.mem,power.draw,temperature.gpu,memory.used',
                                         '--format=csv,noheader,nounits'], capture_output=True, text=True, check=True)
                samples.append(dict(time_ms=round(time.time()*1000), gpu=status.stdout.strip()))
                time.sleep(1)
        finally:
            if process.poll() is None:
                os.killpg(process.pid, signal.SIGTERM)
                try:
                    process.wait(timeout=10)
                except subprocess.TimeoutExpired:
                    os.killpg(process.pid, signal.SIGKILL); process.wait()
            (out / 'load.json').write_text(json.dumps(samples, indent=2) + '\n')
    if process.returncode:
        raise RuntimeError(f'Profiling failed ({process.returncode}): {out / "client.log"}')
    log_text = (out / 'client.log').read_text()
    if any(error in log_text for error in ('Validation Error', 'SYNC-HAZARD', 'VUID-',
                                         'Separate indirect pass unavailable', 'Separate indirect consumer unavailable',
                                         'Adaptive termination unavailable', 'using LightBVH')):
        raise RuntimeError(f'Validation error or dispatch fallback: {out / "client.log"}')
    for phase in phases or [None]:
        destination = out / phase[0] if phase else out
        expected = int(phase[1]) if phase else int(args.sharc)
        if phase:
            for name in ('frames.csv', 'stages.csv', 'state.csv', 'rays.csv', 'load.json', 'lighting-timings.csv'):
                if (out / name).exists():
                    (destination / name).symlink_to('../' + name)
            launch = json.loads((out / 'launch.json').read_text())
            launch['phase'] = dict(zip(('name', 'sharc', 'bounces', 'capacity', 'downscale', 'dispatch', 'placement', 'roulette', 'rouletteMin', 'torchSampler', 'rtxdiProposal'), phase))
            (destination / 'launch.json').write_text(json.dumps(launch, indent=2) + '\n')
        result = summarize(destination)
        assert result['sharc']['min'] == result['sharc']['max'] == expected
        assert result['denoiser']['min'] == result['samples']['min'] == 1
        assert result['taa']['min'] == int(args.upscaler == 'off')
        assert result['reconstruction'][0] == (2 if args.upscaler == 'dlss' else 0)
        assert result['reconstruction'][-1] == 0
        if phase and len(phase) >= 6 and (destination / 'state.csv').exists():
            window = json.loads((destination / 'measurement.json').read_text())
            with (destination / 'state.csv').open() as file:
                state = [row for row in csv.DictReader(file)
                         if window['start_ms'] <= int(row['time_ms']) < window['end_ms']]
            assert state
            if phase[5] != 'a':
                assert all(int(row['split']) == int(phase[5]) for row in state)
            if len(phase) >= 7:
                assert all(int(row['scene_device_local']) == int(phase[6]) for row in state)
            if len(phase) >= 8:
                minimum = int(phase[8]) if len(phase) >= 9 else args.roulette_min
                active = int(phase[7]) and minimum < int(phase[2])
                for row in state:
                    # Private renderer frame: verify the effective controls, not
                    # just the requested launch variables. No SDK/JNI extension.
                    frame = bytes.fromhex(row['frame_hex'])
                    assert len(frame) == 544, 'Update diagnostic private-frame decoder'
                    assert struct.unpack_from('<II', frame, 528) == (int(active), minimum)
        print(json.dumps(dict(phase=phase[0] if phase else args.label, result=result)), flush=True)


if __name__ == '__main__':
    main()
