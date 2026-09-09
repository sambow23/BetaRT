import importlib.util
from pathlib import Path
import tempfile
import unittest
from unittest import mock
spec = importlib.util.spec_from_file_location('stage', Path(__file__).resolve().parents[3] / 'scripts-linux/stage-pathtrace-profile.py')
stage = importlib.util.module_from_spec(spec)
spec.loader.exec_module(stage)
class Staging(unittest.TestCase):
    def test_round_trip_and_changed_artifact_refusal(self):
        with tempfile.TemporaryDirectory() as directory:
            root=Path(directory);instance=root/'BetaRT-Raster-Profile';payload=root/'payload';backup=root/'backup'
            (instance/'libraries').mkdir(parents=True);payload.mkdir()
            for name in stage.FILES:
                (instance/'libraries'/name).write_bytes(b'old');(payload/name).write_bytes(b'new')
            stage.run('stage',instance,payload,backup)
            target=instance/'libraries'/stage.FILES[0];target.write_bytes(b'changed')
            with self.assertRaises(ValueError):stage.run('rollback',instance,payload,backup)
            target.write_bytes(b'new');stage.run('rollback',instance,payload,backup)
            for name in stage.FILES:self.assertEqual((instance/'libraries'/name).read_bytes(),b'old')
            with self.assertRaises(ValueError):stage.run('stage',root/'b1.7.3',payload,root/'normal-backup')
    def test_failed_publication_restores_the_pair(self):
        with tempfile.TemporaryDirectory() as directory:
            root=Path(directory);instance=root/'BetaRT-Raster-Profile';payload=root/'payload'
            (instance/'libraries').mkdir(parents=True);payload.mkdir()
            for name in stage.FILES:
                (instance/'libraries'/name).write_bytes(b'old');(payload/name).write_bytes(b'new')
            replace=stage.os.replace
            def fail_second(source,target):
                if source.name==stage.FILES[1]+'.pathtrace-tmp':raise OSError('Simulated publication failure')
                replace(source,target)
            with mock.patch.object(stage.os,'replace',side_effect=fail_second):
                with self.assertRaises(OSError):stage.run('stage',instance,payload,root/'backup')
            for name in stage.FILES:self.assertEqual((instance/'libraries'/name).read_bytes(),b'old')
            self.assertFalse(list((instance/'libraries').glob('*.pathtrace-*')))
if __name__ == '__main__': unittest.main()
