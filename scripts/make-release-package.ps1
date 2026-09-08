<#
.SYNOPSIS
Packages the current build as a zip that installs into an existing BetaRT
1.7.3 instance.

.DESCRIPTION
Produces an update package, not a full install: the patched client jar, the
native bridge and the texture assets, plus an installer and a readme. It
deliberately does not carry the RTX Remix runtime, which is 350 MB of DLLs that
anyone running BetaRT already has and that never changes between our builds.

Run build-patched-client.ps1 first, or pass -Build to have it run for you. The
native DLL must already be built (cmake --build build --config Release --target
mcrtx_jni); this script will not build C++.

.EXAMPLE
.\scripts\make-release-package.ps1 -Build

.EXAMPLE
.\scripts\make-release-package.ps1 -Label "lod-preview-2"
#>
param(
    [string]$Configuration = "Release",
    # Where the built bundle is; the default is where build-patched-client puts it.
    [string]$BundleRoot,
    # Where the zip lands.
    [string]$OutputRoot,
    # A name for this build, used in the file name. Defaults to the commit hash.
    [string]$Label,
    # Run build-patched-client.ps1 before packaging.
    [switch]$Build
)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)

if (-not $BundleRoot) { $BundleRoot = Join-Path $repoRoot "out\patched-client" }
if (-not $OutputRoot) { $OutputRoot = Join-Path $repoRoot "out\package" }

if ($Build) {
    Write-Host "Building patched client..."
    & (Join-Path $repoRoot "scripts\build-patched-client.ps1") -Configuration $Configuration | Out-Null
    if (-not $?) { throw "build-patched-client.ps1 failed" }
}

$bundleJar = Join-Path $BundleRoot "minecraft-b1.7.3-client-mcrtx.jar"
$bundleDll = Join-Path $BundleRoot "mcrtx_jni.dll"
$bundleAssets = Join-Path $BundleRoot "mcrtx_assets"

foreach ($required in @($bundleJar, $bundleDll, $bundleAssets)) {
    if (-not (Test-Path $required)) {
        throw "Missing '$required'. Run build-patched-client.ps1 (or pass -Build) first."
    }
}

# Identify the build from git so a package can always be traced back to the
# source it came from. A dirty tree is called out rather than silently shipped,
# because a package nobody can reproduce is worse than no package.
$commit = "unknown"
$dirty = $false
Push-Location $repoRoot
try {
    $commit = (& git rev-parse --short HEAD)
    $status = (& git status --porcelain)
    if ($status) { $dirty = $true }
} catch {
    Write-Warning "Not a git checkout; the package will not record a commit."
} finally {
    Pop-Location
}

if (-not $Label) {
    $Label = $commit
}
if ($dirty) {
    Write-Warning "Working tree has uncommitted changes. Packaging anyway; label will be marked dirty."
    $Label = "$Label-dirty"
}

$stamp = Get-Date -Format "yyyyMMdd"
$packageName = "BetaRT-distant-terrain-$stamp-$Label"
$stagingRoot = Join-Path $OutputRoot $packageName
$payloadRoot = Join-Path $stagingRoot "payload"

if (Test-Path $stagingRoot) { Remove-Item $stagingRoot -Recurse -Force }
New-Item -ItemType Directory -Path $payloadRoot -Force | Out-Null

Write-Host "Staging payload..."
# Named client.jar rather than by its build name: the installer copies it over
# whatever customjar-N.jar the target instance happens to use, so its name here
# describes what it is rather than where it came from.
Copy-Item $bundleJar (Join-Path $payloadRoot "client.jar") -Force
Copy-Item $bundleDll (Join-Path $payloadRoot "mcrtx_jni.dll") -Force
Copy-Item $bundleAssets (Join-Path $payloadRoot "mcrtx_assets") -Recurse -Force

Copy-Item (Join-Path $repoRoot "scripts\package\install.ps1") (Join-Path $stagingRoot "install.ps1") -Force

$readme = @"
# BetaRT -- distant terrain build

Build ``$Label``, packaged $stamp.

This **updates a BetaRT 1.7.3 instance you already have working**. It is not a
full install: it does not include the RTX Remix runtime, and it will refuse to
install into an instance that has not got one.

## Installing

1. Extract this zip somewhere, keeping its folder structure.
2. **Close the game and PrismLauncher.** Either one holds the files this
   replaces open, and the installer will refuse to start while they do.
3. Right-click ``install.ps1`` and pick **Run with PowerShell**.

   Or, from a PowerShell prompt in the extracted folder:

   ``````
   .\install.ps1
   ``````

   It finds your BetaRT instances and asks which one, if there is more than one.
   To skip the question: ``.\install.ps1 -InstanceName "BetaRT-v0.1.2"``

4. Launch the instance from PrismLauncher.

Windows blocks scripts that came out of a downloaded zip. Right-clicking and
picking **Run with PowerShell** gets past that; from a prompt, run:
``````
powershell -ExecutionPolicy Bypass -File .\install.ps1
``````

To see what it would do without changing anything:
``````
.\install.ps1 -DryRun
``````

## Where it installs to, and why

An instance keeps more than one copy of the native bridge and the texture
assets, and which copy the game loads depends on how the instance was built --
an instance with jar mods launches from ``minecraft\bin\minecraft.jar`` and
loads the bridge sitting next to *that*. So the installer replaces **every copy
it finds** rather than guessing: leaving a stale one behind is how you end up
running a new client jar against an old bridge, which looks like the mod
silently doing nothing.

It also refuses, without changing anything, if:

- the instance has no RTX Remix runtime (this updates BetaRT, it cannot install it)
- the instance launches from PrismLauncher's shared jar rather than its own copy
- any file it would replace is open in another program
- a folder it would replace is a junction or a symbolic link
- there is not enough free disk space, or the instance sits too deep in the
  folder tree for Windows to handle the paths

Nothing is overwritten until every new file has been copied in beside the old
one and checked against the package, so an interrupted run leaves the instance
as it was rather than half updated.

## Reverting

Everything it replaces is backed up inside the instance under
``betart-lod-backups\<timestamp>``. To put the most recent one back:

``````
.\install.ps1 -Restore
``````

That also removes anything the install added that was not there before, so a
restore leaves the instance as it started.

A backup is roughly the size of the texture assets -- about 90 MB per run, per
copy -- so the installer keeps the newest three and deletes older ones, and
prints what the backups are using when it finishes. ``-KeepBackups 0`` keeps all
of them.

## What changed

Distant terrain was rebuilt. The visible parts:

- Terrain out past the loaded chunks is a smooth surface with real lighting,
  instead of flat-shaded stair steps.
- Trees read as foliage over ground rather than as pillars or plateaus of
  leaves.
- Distant water reflects the sky.
- Terrain fills in from the world's own save files, so you see distance you have
  not walked to in this session.
- Building a piece of distant terrain no longer stutters the frame.

There is a **Distant Terrain** page in the BetaRT settings with distance,
handover, coverage and detail-budget controls.

### LOD Distance now reaches 4096 blocks

The distance slider has a fifth notch. It was 192 / 512 / 1024 / 2048; it is now
192 / 512 / 1024 / 2048 / 4096, and each notch adds one ring of coarser, wider
cells around the ones inside it. **The default has not moved** -- it is still
1024, three rings -- so nothing changes unless you drag the slider.

What each notch costs, over ground that is fully generated:

| Setting | Rings | Regions | Triangles | Label |
|---|---|---|---|---|
| 192 | 1 | 36 | ~230,000 | Lowest |
| 512 | 2 | 91 | ~570,000 | Low |
| 1024 | 3 | 139 | ~690,000 | Medium |
| 2048 | 4 | 187 | ~800,000 | High |
| 4096 | 5 | 235 | ~920,000 | Extreme |

The far rings are cheaper than they look. A region is always 32x32 cells however
much ground it covers, and coarse ground is smooth, so an outermost-ring region
meshes to about 2,300 triangles where an innermost-ring one runs to 6,000. The
whole fifth ring is about 110,000 triangles and 15 MB. It needs no new texture
assets, so the download and the install are the same size as before.

**The honest catch: 4096 blocks needs 8 km of generated world under it.** Distant
terrain can only draw ground that exists. It comes from chunks you have loaded
this session and, in single-player, from your save's own region files -- so the
outer rings show terrain you have walked to and nothing where you have not. A
world with a few hundred chunks explored will look nearly identical at 4096 and
at 1024, with a little more terrain in the directions you have travelled. On a
server there is no save to read, so the outer rings fill only from what your
client has been sent and what previous sessions cached.

Turn it up if you have a well-explored single-player world and want to see the
far side of it. Otherwise 1024 or 2048 is the setting that shows you something.

## Known rough edges

This is a development build. Worth knowing before you install:

- **The terrain cache format changed.** Old ``mcrtx-lod-cache`` data is ignored
  and rebuilt; the folder can be deleted. Nothing else in your world is touched.
- Distant terrain may take a few seconds to fill after loading a world, and
  fills nearest-first.
- Distant terrain can still show through real terrain in places, and there can
  be seams where two detail levels meet over a tall cliff.
- Multiplayer gets only what your own client has loaded; the save-file reader is
  single-player only.
- The Nether has no distant terrain by design (a heightfield there is just the
  bedrock ceiling).

If something looks wrong, ``minecraft\mcrtx.log`` in the instance has a
``lod status:`` line that says what the field is doing. Sending that back is the
single most useful thing when reporting a problem.

## If a restore is not enough

Reinstalling a BetaRT release over the instance works too -- this package only
replaces the client jar, the native bridge and the texture assets, and touches
nothing else in the instance.
"@

Set-Content -Path (Join-Path $stagingRoot "README.md") -Value $readme -Encoding utf8

$zipPath = Join-Path $OutputRoot "$packageName.zip"
if (Test-Path $zipPath) { Remove-Item $zipPath -Force }

Write-Host "Compressing (the texture atlases are large; this takes a moment)..."
Compress-Archive -Path $stagingRoot -DestinationPath $zipPath -CompressionLevel Optimal

$zipSize = [math]::Round((Get-Item $zipPath).Length / 1MB, 1)
$rawSize = [math]::Round(((Get-ChildItem $stagingRoot -Recurse -File | Measure-Object -Property Length -Sum).Sum) / 1MB, 1)

Write-Host ""
Write-Host "Package: $zipPath"
Write-Host "  $zipSize MB zipped, $rawSize MB extracted"
Write-Host "  build $Label"
Write-Host ""
Write-Host "Staging folder left at $stagingRoot for inspection."
