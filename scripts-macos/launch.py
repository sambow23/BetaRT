#!/usr/bin/env python3
"""Prism wrapper, installed beside libmcrtx_jni.dylib and runtime/."""
import os
from pathlib import Path
import signal
import subprocess
import sys
import time

root = Path(__file__).resolve().parent
env = dict(os.environ,
    MCRTX_JNI_PATH=str(root / 'libmcrtx_jni.dylib'),
    MCRTX_REMIX_PATH=str(root / 'runtime/lib/libremix.0.dylib'),
    DYLD_LIBRARY_PATH=str(root / 'runtime/lib') + os.pathsep + os.environ.get('DYLD_LIBRARY_PATH', ''),
    # Apple Metal HUD can dereference a stale layer during device-loss
    # recovery and turn the useful Vulkan error into a JVM SIGSEGV.
    MTL_HUD_ENABLED='0',
    SDL_VIDEODRIVER='cocoa')
# Metal runtimes need nothing more; Vulkan runtimes bundle MoltenVK and its ICD.
icd = root / 'runtime/etc/vulkan/icd.d/MoltenVK_icd.json'
if icd.is_file():
    env.update(
        VK_DRIVER_FILES=str(icd),
        MVK_CONFIG_ENABLE_EXPERIMENTAL_RAY_TRACING='1',
        # The renderer uses one queue. Avoid MTLEvent-backed binary semaphores;
        # on Apple Silicon they can lose the acquire command buffer as an invalid
        # Metal resource before the first frame is submitted.
        MVK_CONFIG_VK_SEMAPHORE_SUPPORT_STYLE='0',
        # With two GPU frames in flight, current OpenRemix loses the device on this
        # driver a moment after path tracing starts (Metal "Invalid Resource").
        REMIX_FRAMES_IN_FLIGHT=os.environ.get('REMIX_FRAMES_IN_FLIGHT', '1'))
# Java runs off the first thread so the native renderer can own Cocoa's run loop.
args = [arg for arg in sys.argv[1:] if arg != '-XstartOnFirstThread']
if not args:
    raise SystemExit('Expected the Prism Java command')
with (root / ('game-' + time.strftime('%Y%m%d-%H%M%S') + '.log')).open('wb') as log:
    child = subprocess.Popen(args, env=env, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    def forward(signum, frame):
        child.send_signal(signum)
    signal.signal(signal.SIGTERM, forward)
    signal.signal(signal.SIGINT, forward)
    for line in child.stdout:
        log.write(line)
        log.flush()
        sys.stdout.buffer.write(line)
        sys.stdout.buffer.flush()
    raise SystemExit(child.wait())
