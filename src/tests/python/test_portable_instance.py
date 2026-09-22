import importlib.util
import json
from pathlib import Path
import tempfile
import unittest
from unittest import mock
import zipfile

spec = importlib.util.spec_from_file_location('portable', Path(__file__).resolve().parents[3] / 'scripts-linux/create-portable-instance.py')
portable = importlib.util.module_from_spec(spec)
spec.loader.exec_module(portable)


class PortableInstance(unittest.TestCase):
    def fixtures(self, root):
        template, native = root / 'original host', root / 'native'
        (template / 'patches').mkdir(parents=True)
        (template / 'libraries/mcrtx_assets').mkdir(parents=True)
        (template / 'libraries/lighting-notices').mkdir()
        (template / 'minecraft/saves').mkdir(parents=True)
        (template / 'minecraft/saves/private.dat').write_text('private world')
        (template / 'instance.cfg').write_text('WrapperCommand=/home/old/wrapper.py\nJavaPath=/home/old/java')
        (template / 'minecraft/mcrtx-runtime.env').write_text('MCRTX_REMIX_PATH=/home/old/libremix.so')
        (template / 'mmc-pack.json').write_text('{}')
        for name in ('net.minecraft', 'org.lwjgl3'):
            (template / 'patches' / (name + '.json')).write_text('{}')
        (template / 'patches/customjar.json').write_text(json.dumps({'mainJar': {'name': 'custom:customjar:1'}}))
        for name in ('customjar-1.jar', 'LICENSE-pbrt.txt', 'NOTICE-pbrt.txt', 'mcrtx_assets/map.png'):
            (template / 'libraries' / name).write_bytes(b'fixture')
        (template / 'libraries/libnvidia-ngx-dlss.so.OLD').write_bytes(b'obsolete')
        native.mkdir()
        for name in portable.REQUIRED_LIBRARIES:
            (native / name).write_bytes(b'ELF fixture')
        (native / 'libremix.so.0').symlink_to('libremix.so')
        return template, native

    @mock.patch.object(portable, 'validate_elf')
    def test_relocated_archive_has_libraries_without_host_state(self, audit):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            template, native = self.fixtures(root)
            output, archive = root / 'new host/instance', root / 'export.zip'
            portable.create(template, native, output, archive)
            with zipfile.ZipFile(archive) as package:
                self.assertIsNone(package.testzip())
                package.extractall(root / 'another machine/instance')
                names = set(package.namelist())
                self.assertIn('instance.cfg', names)
                self.assertIn('libraries/customjar-1.jar', names)
                self.assertNotIn('minecraft/saves/private.dat', names)
                self.assertNotIn('libraries/libnvidia-ngx-dlss.so.OLD', names)
                for name in ('instance.cfg', 'minecraft/mcrtx-runtime.env'):
                    self.assertNotIn(b'/home/old/', package.read(name))
            relocated = root / 'another machine/instance'
            self.assertEqual((relocated / 'libraries/libremix.so.0').read_bytes(), b'ELF fixture')
            self.assertFalse(any(p.is_symlink() for p in output.rglob('*')))
            self.assertIn('minecraft/saves', (output / '.packignore').read_text())
            self.assertTrue((template / 'minecraft/saves/private.dat').is_file())
            audit.assert_called()
            with self.assertRaises(ValueError):
                portable.create(template, native, output, archive)

    @mock.patch.object(portable, 'validate_elf')
    def test_external_library_link_is_rejected(self, audit):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            template, native = self.fixtures(root)
            external = root / 'host-private.txt'
            external.write_text('host only')
            (native / 'external').symlink_to(external)
            with self.assertRaisesRegex(ValueError, 'outside the package'):
                portable.create(template, native, root / 'output', root / 'export.zip')
            self.assertFalse((root / 'output').exists())
            self.assertFalse((root / 'export.zip').exists())

    @mock.patch.object(portable.subprocess, 'check_output')
    def test_new_glibc_is_rejected(self, command):
        command.side_effect = ['ELF64 Advanced Micro Devices X86-64', 'GLIBC_2.28 GLIBC_2.43']
        with self.assertRaisesRegex(ValueError, 'newer than 2.28'):
            portable.validate_elf(Path('libremix.so'))

    @mock.patch.object(portable.subprocess, 'check_output')
    def test_build_directory_rpath_is_rejected(self, command):
        command.side_effect = ['ELF64 Advanced Micro Devices X86-64', 'GLIBC_2.28',
                               '(RUNPATH) Library runpath: [/home/builder/lib:$ORIGIN]']
        with self.assertRaisesRegex(ValueError, 'Nonportable library search path'):
            portable.validate_elf(Path('libremix.so'))


if __name__ == '__main__':
    unittest.main()
