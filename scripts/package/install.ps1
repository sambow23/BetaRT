<#
.SYNOPSIS
Installs a BetaRT distant-terrain build into an existing BetaRT 1.7.3 instance.

.DESCRIPTION
This updates an instance you already have working. It replaces three things --
the patched client jar, the native bridge, and the texture assets -- and touches
nothing else. It does not install the RTX Remix runtime, configure PrismLauncher,
or migrate saves, because an instance that already runs BetaRT has all of that.

Everything it overwrites is backed up first, under a timestamped folder inside
the instance. Run with -Restore to put the most recent backup back.

Nothing is overwritten until every new file has been copied in beside the old
one and checked, so an interrupted run leaves the instance as it was rather than
half updated.

.EXAMPLE
.\install.ps1
Finds your BetaRT instances and installs into the one you pick.

.EXAMPLE
.\install.ps1 -InstanceName "BetaRT-v0.1.2"

.EXAMPLE
.\install.ps1 -DryRun
Says what it would do and changes nothing.

.EXAMPLE
.\install.ps1 -Restore
#>
param(
    # Instance folder name under PrismLauncher\instances.
    [string]$InstanceName,
    # Full path to an instance folder, for non-standard layouts.
    [string]$InstanceRoot,
    # PrismLauncher data directory, if not the default under %APPDATA%.
    [string]$PrismRoot,
    # Undo the most recent install in the chosen instance.
    [switch]$Restore,
    # Report what would happen and write nothing.
    [switch]$DryRun,
    # How many timestamped backups to keep. 0 keeps all of them.
    [int]$KeepBackups = 3,
    # Never wait for a keypress. For running this from another script.
    [switch]$NoPause
)

$ErrorActionPreference = "Stop"
$script:noReaderPresent = $false

# ---- talking to whoever is running this --------------------------------

function Write-Step {
    param([string]$Text)
    Write-Host "  $Text"
}

# Read-Host blocks forever when there is a console but nobody at it, which is
# how an unattended run of this script hangs rather than failing. Every wait for
# a person goes through here, so the worst case is a short wait and a clear
# message instead of a process that never returns.
function Read-LineWithTimeout {
    param([int]$Seconds)

    # Once one wait has gone unanswered there is nobody there, and every later
    # wait is just another timeout on the way out.
    $script:noReaderPresent = $true
    if (-not [Environment]::UserInteractive) { return $null }
    try {
        if ([Console]::IsInputRedirected) { return $null }
    } catch {
        return $null
    }

    $deadline = (Get-Date).AddSeconds($Seconds)
    $typed = ""
    while ((Get-Date) -lt $deadline) {
        $available = $false
        try {
            $available = [Console]::KeyAvailable
        } catch {
            return $null
        }
        if (-not $available) {
            Start-Sleep -Milliseconds 100
            continue
        }

        $key = [Console]::ReadKey($true)
        $script:noReaderPresent = $false
        if ($key.Key -eq [ConsoleKey]::Enter) {
            Write-Host ""
            return $typed
        }
        if ($key.Key -eq [ConsoleKey]::Backspace) {
            if ($typed.Length -gt 0) {
                $typed = $typed.Substring(0, $typed.Length - 1)
                Write-Host "`b `b" -NoNewline
            }
            continue
        }
        $typed += $key.KeyChar
        Write-Host $key.KeyChar -NoNewline
    }
    Write-Host ""
    return $null
}

function Wait-ForReader {
    if ($NoPause -or $script:noReaderPresent) { return }
    Write-Host "Press Enter to close."
    [void](Read-LineWithTimeout -Seconds 30)
}

# Every refusal comes through here. A plain sentence beats a PowerShell stack
# trace for someone who right-clicked the file, and that window closes the
# moment the script ends, so a failure waits to be read.
function Stop-Install {
    param([string]$Message)

    Write-Host ""
    Write-Host $Message -ForegroundColor Red
    Write-Host ""
    Wait-ForReader
    exit 1
}

# ---- paths -------------------------------------------------------------

function Get-FullPath {
    param([string]$Path)

    if ([System.IO.Path]::IsPathRooted($Path)) {
        return [System.IO.Path]::GetFullPath($Path)
    }
    return [System.IO.Path]::GetFullPath((Join-Path (Get-Location).Path $Path))
}

function Test-ReparsePoint {
    param([string]$Path)

    $item = Get-Item -LiteralPath $Path -Force -ErrorAction SilentlyContinue
    if ($null -eq $item) { return $false }
    return (($item.Attributes -band [System.IO.FileAttributes]::ReparsePoint) -ne 0)
}

function Test-InsideInstance {
    param([string]$Path)

    $prefix = $script:instanceFull.TrimEnd('\') + '\'
    return (Get-FullPath $Path).StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)
}

# The only recursive delete in this script. Callers only ever pass a directory
# this run created or renamed moments ago, and these three guards are what stop
# a junction, a mistyped -InstanceRoot or an empty variable from turning that
# into a delete somewhere else on the disk.
function Remove-Tree {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        Stop-Install "Refusing to delete an empty path. Nothing was deleted; please report this."
    }
    if (-not (Test-InsideInstance $Path)) {
        Stop-Install "Refusing to delete '$Path': it is outside the instance folder."
    }
    if (Test-ReparsePoint $Path) {
        Stop-Install "Refusing to delete '$Path': it is a junction or a symbolic link, and deleting it could reach outside the instance. Replace it with a real folder, or move what it points at into the instance."
    }
    Remove-Item -LiteralPath $Path -Recurse -Force
}

function Get-TreeSize {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) { return [long]0 }
    $item = Get-Item -LiteralPath $Path -Force
    if (-not $item.PSIsContainer) { return [long]$item.Length }
    $measured = Get-ChildItem -LiteralPath $Path -Recurse -File -Force | Measure-Object -Property Length -Sum
    if ($null -eq $measured.Sum) { return [long]0 }
    return [long]$measured.Sum
}

function Format-Megabytes {
    param([long]$Bytes)
    return "{0:N0} MB" -f ($Bytes / 1MB)
}

# A running game holds mcrtx_jni.dll open and PrismLauncher holds the jar. Both
# fail part way through a copy, which is how an instance ends up with a new jar
# and an old bridge -- the pairing that looks like the mod silently doing
# nothing. One open per file finds that out before anything has been written.
function Test-FileAvailable {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) { return $true }
    for ($attempt = 0; $attempt -lt 2; $attempt++) {
        try {
            $stream = [System.IO.File]::Open($Path, "Open", "Read", "None")
            $stream.Close()
            return $true
        } catch {
            # An indexer or a virus scanner can hold a file for a moment, so one
            # retry keeps a passing install from failing on a coincidence.
            Start-Sleep -Milliseconds 400
        }
    }
    return $false
}

function Get-Sha256 {
    param([string]$Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash
}

# ---- find the package --------------------------------------------------

$packageRoot = $PSScriptRoot
if (-not $packageRoot) {
    if ($MyInvocation.MyCommand.Path) {
        $packageRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
    }
}
if (-not $packageRoot) {
    Stop-Install @"
This script cannot tell where it was run from, which means it cannot find its
payload folder. Run it as a file rather than pasting or piping it:
  powershell -ExecutionPolicy Bypass -File .\install.ps1
"@
}
$payloadRoot = Join-Path $packageRoot "payload"

# ---- locate the instance -------------------------------------------------

if (-not $InstanceRoot) {
    if (-not $PrismRoot) {
        $PrismRoot = Join-Path $env:APPDATA "PrismLauncher"
    }
    $instancesDir = Join-Path $PrismRoot "instances"
    if (-not (Test-Path $instancesDir)) {
        Stop-Install @"
No PrismLauncher instances folder at '$instancesDir'.

If PrismLauncher is installed somewhere else -- a portable copy, another drive,
or an AppData folder redirected into OneDrive -- point this at it:
  .\install.ps1 -PrismRoot "D:\PrismLauncher"
or name the instance folder directly:
  .\install.ps1 -InstanceRoot "D:\PrismLauncher\instances\BetaRT-v0.1.2"
"@
    }

    if ($InstanceName) {
        # A name is a folder name, not a path. Anything else is either a typo or
        # an attempt to walk out of the instances folder.
        if ($InstanceName -match '[\\/]' -or $InstanceName -eq ".." -or $InstanceName -eq ".") {
            Stop-Install "-InstanceName takes the name of a folder under '$instancesDir', not a path. Use -InstanceRoot for a full path."
        }
        $InstanceRoot = Join-Path $instancesDir $InstanceName
    } else {
        # An instance that runs BetaRT has the Remix runtime sitting next to the
        # game, which is the one marker that separates it from a plain Beta
        # instance. Anything without it would fail at launch rather than at
        # install, which is a far worse place to find out.
        $candidates = @()
        foreach ($dir in (Get-ChildItem -Path $instancesDir -Directory)) {
            if (Test-Path (Join-Path $dir.FullName "minecraft\d3d9.dll")) {
                $candidates += $dir
            }
        }

        if ($candidates.Count -eq 0) {
            Stop-Install "Found no BetaRT instance under '$instancesDir'. Pass -InstanceName explicitly."
        }
        if ($candidates.Count -eq 1) {
            $InstanceRoot = $candidates[0].FullName
        } else {
            Write-Host "Several BetaRT instances found:"
            for ($i = 0; $i -lt $candidates.Count; $i++) {
                Write-Host "  [$i] $($candidates[$i].Name)"
            }
            Write-Host ""
            Write-Host "Install into which? (number) " -NoNewline
            $choice = Read-LineWithTimeout -Seconds 60

            if ($null -eq $choice) {
                $names = ($candidates | ForEach-Object { '  .\install.ps1 -InstanceName "' + $_.Name + '"' }) -join "`n"
                Stop-Install @"
More than one BetaRT instance is installed and nobody answered which to use, so
nothing was changed. Name the one you want:

$names
"@
            }

            $index = 0
            if (-not [int]::TryParse($choice, [ref]$index) -or $index -lt 0 -or $index -ge $candidates.Count) {
                Stop-Install "Not a listed choice: '$choice'"
            }
            $InstanceRoot = $candidates[$index].FullName
        }
    }
}

if (-not (Test-Path $InstanceRoot)) {
    Stop-Install "No such instance folder: '$InstanceRoot'"
}

# Everything downstream compares paths against this, so it has to be absolute
# and it has to be settled before the first write.
$script:instanceFull = (Get-FullPath $InstanceRoot).TrimEnd('\')
$librariesDir = Join-Path $script:instanceFull "libraries"
$minecraftDir = Join-Path $script:instanceFull "minecraft"

if (-not (Test-Path $minecraftDir)) {
    Stop-Install "'$script:instanceFull' does not look like a PrismLauncher instance (no minecraft folder)."
}
if (-not (Test-Path (Join-Path $minecraftDir "d3d9.dll"))) {
    Stop-Install @"
'$script:instanceFull' has no RTX Remix runtime (minecraft\d3d9.dll is missing), so it
is not a working BetaRT instance. This package updates an existing BetaRT
install; it cannot create one. Install a BetaRT release first, confirm it runs,
then run this again.
"@
}

Write-Host ""
Write-Host "Instance: $script:instanceFull"

$backupsRoot = Join-Path $script:instanceFull "betart-lod-backups"
$manifestName = "backup-manifest.txt"
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"

# ---- restore -------------------------------------------------------------

if ($Restore) {
    if (-not (Test-Path $backupsRoot)) {
        Stop-Install "Nothing to restore: no backups in '$backupsRoot'."
    }
    $latest = Get-ChildItem -Path $backupsRoot -Directory | Sort-Object Name -Descending | Select-Object -First 1
    if ($null -eq $latest) {
        Stop-Install "Nothing to restore: '$backupsRoot' is empty."
    }

    Write-Host "Restoring from $($latest.Name)"
    $manifestPath = Join-Path $latest.FullName $manifestName

    if (Test-Path $manifestPath) {
        # The manifest records where each file came from and whether it existed
        # at all, so restoring can put back what was replaced *and* remove what
        # the install added. Without that second half, restoring an instance
        # that had no mcrtx_assets leaves the new one in place next to the old
        # jar, which is the mismatch this whole script exists to avoid.
        foreach ($line in (Get-Content -LiteralPath $manifestPath)) {
            if ($line.StartsWith("#") -or $line.Trim().Length -eq 0) { continue }
            $fields = $line -split "`t", 2
            if ($fields.Count -ne 2) { continue }
            $state = $fields[0]
            $relative = $fields[1]

            $target = Join-Path $script:instanceFull $relative
            if (-not (Test-InsideInstance $target)) {
                Stop-Install "Backup manifest names '$relative', which is outside the instance. Refusing to restore it."
            }

            if ($state -eq "created") {
                if (Test-Path -LiteralPath $target) {
                    $item = Get-Item -LiteralPath $target -Force
                    if ($item.PSIsContainer) { Remove-Tree $target } else { Remove-Item -LiteralPath $target -Force }
                    Write-Step "removed $relative (it did not exist before)"
                }
                continue
            }

            $source = Join-Path $latest.FullName $relative
            if (-not (Test-Path -LiteralPath $source)) {
                Stop-Install "Backup is incomplete: '$relative' is listed but missing from '$($latest.FullName)'."
            }

            $sourceItem = Get-Item -LiteralPath $source -Force
            if ($sourceItem.PSIsContainer) {
                $staged = "$target.betart-restore-$stamp"
                if (Test-Path -LiteralPath $staged) { Remove-Tree $staged }
                Copy-Item -LiteralPath $source $staged -Recurse -Force
                if (Test-Path -LiteralPath $target) { Remove-Tree $target }
                Move-Item -LiteralPath $staged $target
            } else {
                $parent = Split-Path -Parent $target
                if (-not (Test-Path -LiteralPath $parent)) { New-Item -ItemType Directory -Path $parent -Force | Out-Null }
                Copy-Item -LiteralPath $source $target -Force
            }
            Write-Step "restored $relative"
        }
    } else {
        # Backups written by earlier versions of this installer are flat, with a
        # ".1" suffix marking the second copy of the bridge. They are still the
        # only way back for anyone who installed before this change.
        foreach ($item in (Get-ChildItem -Path $latest.FullName)) {
            if ($item.Name -eq "mcrtx_jni.dll.1") {
                Copy-Item -LiteralPath $item.FullName (Join-Path $minecraftDir "mcrtx_jni.dll") -Force
                Write-Step "restored mcrtx_jni.dll (minecraft)"
                continue
            }

            $target = Join-Path $librariesDir $item.Name
            if ($item.PSIsContainer) {
                if (Test-Path -LiteralPath $target) { Remove-Tree $target }
                Copy-Item -LiteralPath $item.FullName $target -Recurse -Force
            } else {
                Copy-Item -LiteralPath $item.FullName $target -Force
            }
            Write-Step "restored $($item.Name)"
        }
    }

    Write-Host ""
    Write-Host "Restored. Launch the instance to confirm."
    Write-Host ""
    Wait-ForReader
    return
}

# ---- validate the payload ------------------------------------------------

$payloadJar = Join-Path $payloadRoot "client.jar"
$payloadDll = Join-Path $payloadRoot "mcrtx_jni.dll"
$payloadAssets = Join-Path $payloadRoot "mcrtx_assets"

foreach ($required in @($payloadJar, $payloadDll, $payloadAssets)) {
    if (-not (Test-Path $required)) {
        Stop-Install "Package is incomplete: '$required' is missing. Re-extract the zip, keeping its folder structure."
    }
}
$payloadAssetFiles = @(Get-ChildItem -LiteralPath $payloadAssets -Recurse -File -Force)
if ($payloadAssetFiles.Count -eq 0) {
    Stop-Install "Package is incomplete: '$payloadAssets' has no files in it. Re-extract the zip."
}

# ---- work out what to replace --------------------------------------------

# PrismLauncher numbers custom jars per instance, so the name is discovered
# rather than assumed. Replacing every one of them is deliberate: an instance
# with two would otherwise launch from whichever was left behind.
$customJars = @()
if (Test-Path $librariesDir) {
    $customJars = @(Get-ChildItem -Path $librariesDir -Filter "customjar-*.jar" -File)
}
if ($customJars.Count -eq 0) {
    Stop-Install @"
No customjar-*.jar in '$librariesDir'. This instance launches from the shared
PrismLauncher library jar rather than its own copy, which this installer does not
touch -- overwriting the shared jar would change every other instance using it.
Install into an instance created from a BetaRT release zip instead.
"@
}

# The bridge and the assets are both found by searching, and the search does not
# start in libraries. The bridge is looked for next to the jar the game actually
# launched from, then in the working directory; the assets are looked for next
# to the loaded bridge, then in the working directory, and only then in
# libraries. An instance with jar mods launches from minecraft\bin\minecraft.jar,
# so on those instances minecraft\bin\mcrtx_jni.dll and minecraft\mcrtx_assets
# are what load -- updating libraries alone would leave the game running an old
# bridge against a new jar and never say so. So: every copy that already exists
# gets replaced, and libraries is always written because that is where an
# instance without jar mods loads from.
$dllRelatives = @("libraries\mcrtx_jni.dll", "minecraft\mcrtx_jni.dll", "minecraft\bin\mcrtx_jni.dll", "natives\mcrtx_jni.dll")
$assetRelatives = @("libraries\mcrtx_assets", "minecraft\mcrtx_assets", "minecraft\bin\mcrtx_assets")

$plan = @()
foreach ($jar in $customJars) {
    $plan += [pscustomobject]@{ Relative = "libraries\" + $jar.Name; Source = $payloadJar; IsDirectory = $false }
}
foreach ($relative in $dllRelatives) {
    $target = Join-Path $script:instanceFull $relative
    if ($relative -eq "libraries\mcrtx_jni.dll" -or (Test-Path -LiteralPath $target)) {
        $plan += [pscustomobject]@{ Relative = $relative; Source = $payloadDll; IsDirectory = $false }
    }
}
foreach ($relative in $assetRelatives) {
    $target = Join-Path $script:instanceFull $relative
    if ($relative -eq "libraries\mcrtx_assets" -or (Test-Path -LiteralPath $target)) {
        $plan += [pscustomobject]@{ Relative = $relative; Source = $payloadAssets; IsDirectory = $true }
    }
}

foreach ($entry in $plan) {
    $entry | Add-Member -NotePropertyName Target -NotePropertyValue (Join-Path $script:instanceFull $entry.Relative)
    $entry | Add-Member -NotePropertyName Existed -NotePropertyValue (Test-Path -LiteralPath $entry.Target)
}

# ---- refuse before writing anything --------------------------------------

$backupDir = Join-Path $backupsRoot $stamp
$stagingSuffix = ".betart-new-$stamp"
$replacedSuffix = ".betart-old-$stamp"

foreach ($entry in $plan) {
    if (-not (Test-InsideInstance $entry.Target)) {
        Stop-Install "Refusing to write '$($entry.Target)': it is outside the instance folder."
    }
    if ($entry.Existed -and (Test-ReparsePoint $entry.Target)) {
        Stop-Install @"
'$($entry.Relative)' is a junction or a symbolic link rather than a real file or
folder. Replacing it would either follow it out of the instance or quietly throw
away where it points. Nothing has been changed. Replace it with a real copy, or
install into an instance that has one.
"@
    }
}

# Leftovers mean a previous run was killed part way through. The ".betart-old-"
# folders hold that run's original files, so they are the user's, not ours to
# clear away.
$leftovers = @()
foreach ($entry in $plan) {
    $parent = Split-Path -Parent $entry.Target
    if (-not (Test-Path -LiteralPath $parent)) { continue }
    foreach ($stray in (Get-ChildItem -LiteralPath $parent -Force -Filter "*.betart-*" -ErrorAction SilentlyContinue)) {
        $leftovers += $stray.FullName
    }
}
if ($leftovers.Count -gt 0) {
    $list = ($leftovers | Sort-Object -Unique) -join "`n  "
    Stop-Install @"
An earlier run of this installer was interrupted and left these behind:

  $list

A ".betart-old-" folder holds the files that run replaced; a ".betart-new-" one
holds a copy that was never finished. Rename the "old" one back over the real
name if the instance is broken, delete them if it is not, then run this again.
"@
}

# PowerShell 5.1 cannot open a path longer than 260 characters unless Windows is
# set to allow it, and it discovers that half way through a copy. The longest
# path this run would create is knowable up front, so check it up front.
$longPathsEnabled = $false
try {
    $policy = Get-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" -Name "LongPathsEnabled" -ErrorAction SilentlyContinue
    if ($null -ne $policy -and $policy.LongPathsEnabled -eq 1) { $longPathsEnabled = $true }
} catch {
    $longPathsEnabled = $false
}
if (-not $longPathsEnabled) {
    $deepestAsset = 0
    foreach ($file in $payloadAssetFiles) {
        $relativeLength = $file.FullName.Length - $payloadAssets.Length
        if ($relativeLength -gt $deepestAsset) { $deepestAsset = $relativeLength }
    }
    $longest = 0
    foreach ($entry in $plan) {
        $extra = 0
        if ($entry.IsDirectory) { $extra = $deepestAsset }
        # A comma binds tighter than a plus in PowerShell, so these are built one
        # per line rather than as one array literal.
        $lengths = @()
        $lengths += ($entry.Target + $stagingSuffix).Length + $extra
        $lengths += ($entry.Target + $replacedSuffix).Length + $extra
        $lengths += (Join-Path $backupDir $entry.Relative).Length + $extra
        foreach ($candidate in $lengths) {
            if ($candidate -gt $longest) { $longest = $candidate }
        }
    }
    if ($longest -gt 259) {
        Stop-Install @"
This instance sits too deep in the folder tree: installing would need paths of
$longest characters, and Windows stops this script at 260. Nothing has been
changed. Move the instance somewhere shorter (or turn on Win32 long paths) and
run this again.
"@
    }
}

# Both halves of a locked file matter: a backup that cannot be read is a backup
# that is not there, and a target that cannot be written stops the install after
# earlier files have already been replaced.
$busy = @()
foreach ($entry in $plan) {
    if (-not $entry.Existed) { continue }
    if ($entry.IsDirectory) {
        foreach ($file in (Get-ChildItem -LiteralPath $entry.Target -Recurse -File -Force)) {
            if (-not (Test-FileAvailable $file.FullName)) { $busy += $file.FullName }
        }
    } else {
        if (-not (Test-FileAvailable $entry.Target)) { $busy += $entry.Target }
    }
}
if ($busy.Count -gt 0) {
    $list = ($busy | Select-Object -First 8) -join "`n  "
    Stop-Install @"
Another program is holding these files open:

  $list

That is almost always the game still running, or PrismLauncher with the instance
open. Close both and run this again. Nothing has been changed.
"@
}

$backupBytes = [long]0
foreach ($entry in $plan) {
    if ($entry.Existed) { $backupBytes += (Get-TreeSize $entry.Target) }
}
$installBytes = [long]0
foreach ($entry in $plan) {
    $installBytes += (Get-TreeSize $entry.Source)
}
# The new copy is built beside the old one before either is removed, so at the
# high-water mark the disk holds the backup, the old files and the new ones.
$requiredBytes = $backupBytes + $installBytes + 64MB

$freeBytes = [long](-1)
try {
    $drive = New-Object System.IO.DriveInfo([System.IO.Path]::GetPathRoot($script:instanceFull))
    $freeBytes = $drive.AvailableFreeSpace
} catch {
    $freeBytes = -1
}
if ($freeBytes -ge 0 -and $freeBytes -lt $requiredBytes) {
    Stop-Install @"
Not enough free disk space. This needs about $(Format-Megabytes $requiredBytes)
(the backup, the new files, and room to work) and the drive has
$(Format-Megabytes $freeBytes) free. Nothing has been changed.
"@
}

Write-Host ""
Write-Host "Will replace:"
foreach ($entry in $plan) {
    $note = ""
    if (-not $entry.Existed) { $note = "  (new)" }
    Write-Step "$($entry.Relative)$note"
}
Write-Host ""
Write-Host "Backup:   betart-lod-backups\$stamp  ($(Format-Megabytes $backupBytes))"
if ($freeBytes -ge 0) {
    Write-Host "Disk:     needs about $(Format-Megabytes $requiredBytes), $(Format-Megabytes $freeBytes) free"
}

if ($DryRun) {
    Write-Host ""
    Write-Host "Dry run: nothing was changed."
    Write-Host ""
    Wait-ForReader
    return
}

# ---- back up, stage, then swap -------------------------------------------

$staged = @()
$replaced = @()
$swapped = @()

try {
    Write-Host ""
    Write-Host "Backing up to betart-lod-backups\$stamp"
    New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

    $manifest = @("# BetaRT install $stamp -- what was replaced, and where it came from.", "# 'created' means the install added it and restoring removes it again.")
    foreach ($entry in $plan) {
        if (-not $entry.Existed) {
            $manifest += ("created`t" + $entry.Relative)
            continue
        }
        $destination = Join-Path $backupDir $entry.Relative
        $parent = Split-Path -Parent $destination
        if (-not (Test-Path -LiteralPath $parent)) { New-Item -ItemType Directory -Path $parent -Force | Out-Null }
        if ($entry.IsDirectory) {
            Copy-Item -LiteralPath $entry.Target $destination -Recurse -Force
        } else {
            Copy-Item -LiteralPath $entry.Target $destination -Force
        }
        $manifest += ("existed`t" + $entry.Relative)
        Write-Step "saved $($entry.Relative)"
    }
    Set-Content -LiteralPath (Join-Path $backupDir $manifestName) -Value $manifest -Encoding UTF8

    # Copy everything in beside its target first. This is the slow part, and it
    # is the part most likely to fail -- so nothing the instance is using has
    # been touched by the time it finishes.
    Write-Host ""
    Write-Host "Copying in the new files"
    foreach ($entry in $plan) {
        $stagePath = $entry.Target + $stagingSuffix
        $parent = Split-Path -Parent $stagePath
        if (-not (Test-Path -LiteralPath $parent)) { New-Item -ItemType Directory -Path $parent -Force | Out-Null }
        if ($entry.IsDirectory) {
            Copy-Item -LiteralPath $entry.Source $stagePath -Recurse -Force
        } else {
            Copy-Item -LiteralPath $entry.Source $stagePath -Force
        }
        $staged += $stagePath
    }

    # Check the copies before committing to them. A short write or a full disk
    # shows up here, where the instance is still untouched.
    foreach ($entry in $plan) {
        $stagePath = $entry.Target + $stagingSuffix
        if ($entry.IsDirectory) {
            foreach ($file in $payloadAssetFiles) {
                $relative = $file.FullName.Substring($payloadAssets.Length).TrimStart('\')
                $copy = Join-Path $stagePath $relative
                if (-not (Test-Path -LiteralPath $copy)) { throw "Copy of '$($entry.Relative)' is missing '$relative'." }
                if ((Get-Sha256 $copy) -ne (Get-Sha256 $file.FullName)) { throw "Copy of '$($entry.Relative)\$relative' does not match the package." }
            }
        } else {
            if ((Get-Sha256 $stagePath) -ne (Get-Sha256 $entry.Source)) { throw "Copy of '$($entry.Relative)' does not match the package." }
        }
    }

    # Renames only from here on: fast, and each one either happened or did not.
    Write-Host ""
    Write-Host "Installing"
    foreach ($entry in $plan) {
        $stagePath = $entry.Target + $stagingSuffix
        if ($entry.Existed) {
            $asidePath = $entry.Target + $replacedSuffix
            Move-Item -LiteralPath $entry.Target $asidePath
            $replaced += $asidePath
        }
        Move-Item -LiteralPath $stagePath $entry.Target
        $swapped += $entry.Relative
        Write-Step $entry.Relative
    }
} catch {
    Write-Host ""
    Write-Host "INSTALL FAILED: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host ""

    # Anything staged but not swapped is this script's own scratch and safe to
    # drop. Anything already swapped is a real change, and unpicking it here
    # risks making things worse than the backup already sitting on disk.
    foreach ($path in $staged) {
        if (Test-Path -LiteralPath $path) {
            try { Remove-Tree $path } catch { }
        }
    }

    if ($swapped.Count -eq 0) {
        Write-Host "Nothing was replaced -- the instance is exactly as it was." -ForegroundColor Yellow
        if (Test-Path $backupDir) { Write-Host "The backup at betart-lod-backups\$stamp can be deleted." }
    } else {
        Write-Host "These were already replaced before the failure:" -ForegroundColor Yellow
        foreach ($name in $swapped) { Write-Step $name }
        Write-Host ""
        Write-Host "Put them back before launching the game, or you will be running a mix" -ForegroundColor Yellow
        Write-Host "of old and new files:" -ForegroundColor Yellow
        Write-Host "  .\install.ps1 -Restore"
    }
    Write-Host ""
    Wait-ForReader
    exit 1
}

# ---- tidy up -------------------------------------------------------------

foreach ($path in $replaced) {
    Remove-Tree $path
}

# Every run copies the whole asset set twice -- once into the instance and once
# into the backup -- so backups add up to about a gigabyte in ten runs, inside
# the instance, where nobody goes looking.
$pruned = 0
if ($KeepBackups -gt 0) {
    $allBackups = @(Get-ChildItem -Path $backupsRoot -Directory | Where-Object { $_.Name -match '^\d{8}-\d{6}$' } | Sort-Object Name -Descending)
    if ($allBackups.Count -gt $KeepBackups) {
        foreach ($old in ($allBackups | Select-Object -Skip $KeepBackups)) {
            Remove-Tree $old.FullName
            $pruned++
        }
    }
}

$backupsTotal = Get-TreeSize $backupsRoot

Write-Host ""
Write-Host "Done. Launch the instance from PrismLauncher."
Write-Host ""
Write-Host "Backups now use $(Format-Megabytes $backupsTotal) in betart-lod-backups."
if ($pruned -gt 0) {
    Write-Host "Removed $pruned older backup(s); the newest $KeepBackups are kept."
    Write-Host "Pass -KeepBackups 0 to keep every one."
}
Write-Host "To undo:  .\install.ps1 -Restore"
Write-Host ""
Wait-ForReader
