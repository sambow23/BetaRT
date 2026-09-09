#!/usr/bin/env python3
"""Summarize JVMTI/Vulkan cadence captures inside each run's measurement window."""
import argparse
import csv
import json
import statistics
from pathlib import Path


def distribution(values):
    values = sorted(values)
    if not values:
        return {}
    def percentile(p):
        at = (len(values) - 1) * p
        lo = int(at)
        return values[lo] + (values[min(lo + 1, len(values) - 1)] - values[lo]) * (at - lo)
    return {'mean': statistics.fmean(values), 'p50': percentile(.5),
            'p95': percentile(.95), 'p99': percentile(.99), 'max': values[-1]}


def summarize(path):
    window = json.loads((path / 'window.json').read_text())
    start, end = window['start_ms'], window['end_ms']
    duration = (end - start) / 1000
    result = {'run': path.name, 'duration_s': duration, 'warmup_s': window['warmup_s']}
    for name in ('java', 'vulkan'):
        with (path / (name + '.csv')).open() as file:
            rows = [{k: float(v) for k, v in row.items()} for row in csv.DictReader(file)]
        before = next((row for row in reversed(rows) if row['monotonic_ms'] < start), None)
        rows = [row for row in rows if start <= row['monotonic_ms'] < end]
        if not rows:
            raise ValueError('No measurement frames: ' + str(path / name))
        output = {'frames': len(rows), 'fps': len(rows) / duration}
        for key in rows[0]:
            if key not in ('frame', 'monotonic_ms', 'fresh_presented', 'total_presented', 'replaced'):
                output[key] = distribution([row[key] for row in rows])
        if name == 'java' and rows[-1]['total_presented']:
            base = before or rows[0]
            for key in ('fresh_presented', 'total_presented', 'replaced'):
                output[key] = int(rows[-1][key] - base[key])
                output[key + '_per_s'] = output[key] / duration
            output['redraws'] = output['total_presented'] - output['fresh_presented']
            output['jni_resource_mib_per_s'] = sum(row['resource_bytes'] for row in rows) / duration / 2**20
            output['jni_frame_mib_per_s'] = sum(row['frame_bytes'] for row in rows) / duration / 2**20
        if name == 'vulkan':
            output['copy_mib_per_s'] = sum(row['copy_bytes'] for row in rows) / duration / 2**20
        result[name] = output
    samples = json.loads((path / 'load.json').read_text())
    samples = [row for row in samples if start <= row['monotonic_ms'] <= end]
    result['load'] = {'cpu_percent': distribution([
        (b['java_cpu_ticks'] - a['java_cpu_ticks']) / window['clock_ticks'] /
        ((b['monotonic_ms'] - a['monotonic_ms']) / 1000) * 100
        for a, b in zip(samples, samples[1:])]),
        'rss_mib': distribution([row['rss_pages'] * 4096 / 2**20 for row in samples])}
    for index, name in enumerate(('gpu_percent', 'gpu_memory_mib', 'gpu_clock_mhz', 'gpu_memory_clock_mhz', 'gpu_power_w')):
        result['load'][name] = distribution([float(row['gpu'].split(',')[index]) for row in samples])
    return result


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('runs', nargs='+', type=Path)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    results = [summarize(path) for path in args.runs]
    args.output.write_text(json.dumps(results, indent=2) + '\n')
    for result in results:
        j, v = result['java'], result['vulkan']
        print(f"{result['run']}: Java {j['fps']:.2f} FPS / Vulkan {v['fps']:.2f} FPS; "
              f"p99 {j['frame_ms']['p99']:.3f} / {v['frame_ms']['p99']:.3f} ms")
