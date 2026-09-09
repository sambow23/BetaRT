"""Prism uses separate native artifacts; Maven may instead use classifiers."""
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest

SCRIPT = Path(__file__).resolve().parents[3] / 'scripts-linux/prism-audio-component.py'


class PrismAudioTest(unittest.TestCase):
    def test_both_native_naming_schemes(self):
        for classifier in (False, True):
            with self.subTest(classifier=classifier), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                names = ['org.lwjgl:lwjgl:3.3.3', 'org.lwjgl:lwjgl-openal:3.3.3']
                native = ['org.lwjgl:' + module + (':3.3.3:natives-linux' if classifier else '-natives-linux:3.3.3')
                          for module in ('lwjgl', 'lwjgl-openal')]
                graphics = ['org.lwjgl:lwjgl-opengl:3.3.3', 'org.lwjgl:lwjgl-glfw-natives-linux:3.3.3']
                metadata = {'uid': 'org.lwjgl3', 'libraries': [{'name': name} for name in names + native + graphics]}
                (root / 'input.json').write_text(json.dumps(metadata))
                subprocess.run([sys.executable, SCRIPT, root / 'input.json', root / 'output.json'], check=True, capture_output=True)
                output = json.loads((root / 'output.json').read_text())
                self.assertEqual({library['name'] for library in output['libraries']}, set(names + native))

    def test_missing_natives_are_rejected(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / 'input.json').write_text(json.dumps({'uid': 'org.lwjgl3', 'libraries': [
                {'name': 'org.lwjgl:lwjgl:3.3.3'}, {'name': 'org.lwjgl:lwjgl-openal:3.3.3'}]}))
            result = subprocess.run([sys.executable, SCRIPT, root / 'input.json', root / 'output.json'], capture_output=True)
            self.assertNotEqual(result.returncode, 0)
            self.assertFalse((root / 'output.json').exists())


if __name__ == '__main__':
    unittest.main()
