"""Explicit world-frame windows shared by the profiling entry points."""

def world_frames(rows, window):
    start, end = int(window['start_ms']), int(window['end_ms'])
    if end <= start or int(window['fresh']) <= 0:
        raise ValueError('Missing fresh world-frame measurement window')
    measured = [row for row in rows if start <= int(row['time_ms']) < end and int(row['fresh'])]
    if not measured:
        raise ValueError('No fresh world frames inside the measurement window')
    return measured


def require_idle_profile(instance=None, own_pid=None):
    """Read-only guard. Never stop someone else's client to obtain exclusivity."""
    from pathlib import Path
    for process in Path('/proc').glob('[0-9]*'):
        try:
            if own_pid is not None and int(process.name) == own_pid:
                continue
            if (process / 'comm').read_text().strip() != 'java':
                continue
            cwd = (process / 'cwd').resolve()
            command = (process / 'cmdline').read_bytes()
            if (instance and cwd == instance.resolve() / 'minecraft') or b'PrismLauncher/instances/' in command or b'-Dmcrtx.test.directGpu=true' in command:
                raise RuntimeError('A graphics client is running; close it before exclusive profiling')
        except (PermissionError, FileNotFoundError, ProcessLookupError):
            continue
