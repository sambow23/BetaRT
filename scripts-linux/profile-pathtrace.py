#!/usr/bin/env python3
"""Paired raster/refit/rebuild measurements in the isolated profile instance.

Uses DirectWorldTest's fixed in-memory layout and scripted motion; never loads a save or
joins a server. Run after stage-pathtrace-profile.py and compiling the test class.
"""
import argparse
import json
import os
from pathlib import Path
import statistics
import subprocess
import time
import csv
import signal

def interrupted(signum, frame):
    raise SystemExit(128 + signum)

signal.signal(signal.SIGTERM, interrupted)

parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--instance',type=Path,required=True)
parser.add_argument('--classes',type=Path,required=True)
parser.add_argument('--output',type=Path,required=True)
parser.add_argument('--runs',type=int,default=3)
parser.add_argument('--warmup-ms',type=int,default=45000)
parser.add_argument('--measure-ms',type=int,default=60000)
args=parser.parse_args()
if args.runs < 1 or args.warmup_ms < 0 or args.measure_ms < 1:
    parser.error('Use positive run/measurement counts and a nonnegative warmup')
instance=args.instance.resolve()
if instance.name!='BetaRT-Raster-Profile':parser.error('Only the isolated profile instance is accepted')
args.output.mkdir(parents=True,exist_ok=False)
base=os.environ.copy()
for key in ('REMIX_VULKAN_VALIDATION','VK_LAYER_VALIDATE_SYNC','LD_PRELOAD','MCRTX_PERF_TRACE','MCRTX_PT_ACCUMULATE'):
    base.pop(key,None)
base.update(MCRTX_JNI_PATH=str(instance/'libraries/libmcrtx_jni.so'),MCRTX_REMIX_PATH=str(instance/'libraries/libremix.so.0'),
            MCRTX_VSYNC='0',MCRTX_PT_SAMPLES='1',MCRTX_PT_BOUNCES='2',MCRTX_PT_ACCUMULATE='0',MANGOHUD='0',DISABLE_MANGOHUD='1')
base['LD_LIBRARY_PATH']=str(instance/'libraries')
results=[]
def percentile(values,p):
    values=sorted(values);return values[min(len(values)-1,int((len(values)-1)*p))]
for repeat in range(args.runs):
    modes=['raster','refit','rebuild']
    modes=modes[repeat%3:]+modes[:repeat%3]
    for mode in modes:
        run=(args.output/(mode+'-'+str(repeat+1))).resolve();run.mkdir()
        env=base.copy();env.update(MCRTX_RENDER_BACKEND='raster' if mode=='raster' else 'pathtrace',
            MCRTX_PT_FORCE_REBUILD='1' if mode=='rebuild' else '0',MCRTX_PT_PROFILE=str(run/'frames.csv'))
        command=['java','-Xverify:all','-Djava.awt.headless=true','-Dmcrtx.test.directGpu=true','-Dmcrtx.test.benchmark=true',
                 '-Dmcrtx.test.width=1920','-Dmcrtx.test.height=1080','-Dmcrtx.test.warmupMs='+str(args.warmup_ms),
                 '-Dmcrtx.test.measureMs='+str(args.measure_ms),'-cp',str(args.classes.resolve())+':'+str(instance/'libraries/customjar-1.jar'),
                 'DirectWorldTest',str(run)]
        samples=[]
        print('Starting '+run.name,flush=True)
        with (run/'game.log').open('w') as log:
            process=subprocess.Popen(command,cwd=instance/'minecraft',env=env,stdout=log,stderr=subprocess.STDOUT)
            deadline=time.monotonic()+(args.warmup_ms+args.measure_ms)/1000+120
            try:
                while process.poll() is None:
                    if time.monotonic()>deadline:
                        raise RuntimeError('Benchmark timeout: '+run.name)
                    try:
                        status=Path('/proc/'+str(process.pid)+'/status').read_text()
                        rss=int(next(line.split()[1] for line in status.splitlines() if line.startswith('VmRSS:')))*1024
                        gpu=subprocess.run(['nvidia-smi','--query-gpu=utilization.gpu,memory.used,clocks.sm,power.draw',
                                            '--format=csv,noheader,nounits'],capture_output=True,text=True).stdout.strip()
                        samples.append({'time_ms':round(time.time()*1000),'rss_bytes':rss,'gpu':gpu})
                    except (FileNotFoundError,StopIteration):pass
                    time.sleep(1)
            finally:
                # An interrupted comparison must not leave its private Java
                # process holding the profiling libraries open during rollback.
                if process.poll() is None:
                    process.terminate()
                    try: process.wait(timeout=15)
                    except subprocess.TimeoutExpired:
                        process.kill();process.wait()
        if process.returncode:raise RuntimeError('Benchmark failed: '+str(run/'game.log'))
        window=json.loads((run/'measurement.json').read_text())
        with (run/'frames.csv').open() as file:
            rows=[row for row in csv.DictReader(file) if window['start_ms']<=int(row['time_ms'])<window['end_ms']]
        if not rows:raise RuntimeError('Missing measured presentations')
        intervals=[float(row['interval_ms']) for row in rows]
        duration=(window['end_ms']-window['start_ms'])/1000
        if mode!='raster' and max(int(row['blas_builds']) for row in rows)==0:raise RuntimeError('Tracing unavailable during benchmark')
        result={'mode':mode,'run':repeat+1,'duration_s':duration,'fresh_fps':window['fresh']/duration,
                'published_fps':window['published']/duration,'presented_fps':window['presented']/duration,
                'p50_ms':percentile(intervals,.5),'p95_ms':percentile(intervals,.95),'p99_ms':percentile(intervals,.99),
                'gpu_build_ms':statistics.mean(float(row['gpu_build_ms']) for row in rows),
                'gpu_trace_ms':statistics.mean(float(row['gpu_trace_ms']) for row in rows),
                'uploads_bytes':sum(int(row['upload_bytes']) for row in rows),
                'peak_acceleration_bytes':max(int(row['acceleration_bytes']) for row in rows),
                'peak_rss_bytes':max(sample['rss_bytes'] for sample in samples),
                'blas_builds':int(rows[-1]['blas_builds'])-int(rows[0]['blas_builds']),
                'blas_refits':int(rows[-1]['blas_refits'])-int(rows[0]['blas_refits']),
                'static_tlas_builds':int(rows[-1]['static_builds'])-int(rows[0]['static_builds']),
                'dynamic_tlas_builds':int(rows[-1]['dynamic_builds'])-int(rows[0]['dynamic_builds']),
                'dynamic_tlas_refits':int(rows[-1]['dynamic_refits'])-int(rows[0]['dynamic_refits'])}
        results.append(result);(run/'load.json').write_text(json.dumps(samples));(run/'result.json').write_text(json.dumps(result,indent=2))
        (args.output/'results.json').write_text(json.dumps(results,indent=2));print(json.dumps(result),flush=True)
