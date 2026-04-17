[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [string]$InstanceRoot = "C:\Users\cr\AppData\Roaming\PrismLauncher\instances\b1.7.3",
    [string]$MinecraftLibraryJar = "C:\Users\cr\AppData\Roaming\PrismLauncher\libraries\com\mojang\minecraft\b1.7.3\minecraft-b1.7.3-client.jar",
    [string]$BundleRoot,
    [string]$Configuration = "Release",
    [switch]$Build,
    [switch]$Restore,
    [switch]$OverwriteAssets
)

$ErrorActionPreference = "Stop"

function Get-FileHashString {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return $null
    }

    return (Get-FileHash -Algorithm SHA256 -Path $Path).Hash
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
if (-not $BundleRoot) {
    $BundleRoot = Join-Path $repoRoot "out\patched-client"
}

$patchedJar = Join-Path $BundleRoot "minecraft-b1.7.3-client-mcrtx.jar"
$patchedDll = Join-Path $BundleRoot "mcrtx_jni.dll"
$bundleAssetsDir = Join-Path $BundleRoot "mcrtx_assets"
$libraryJarDirectory = Split-Path $MinecraftLibraryJar -Parent
$deployedDllPath = Join-Path $libraryJarDirectory "mcrtx_jni.dll"
$sharedAssetsDir = Join-Path $libraryJarDirectory "mcrtx_assets"
$legacyInstanceDllPath = Join-Path (Join-Path $InstanceRoot "natives") "mcrtx_jni.dll"
$instanceLibrariesDir = Join-Path $InstanceRoot "libraries"
$instanceMinecraftDir = Join-Path $InstanceRoot "minecraft"
$instanceLibraryDllPath = Join-Path $instanceLibrariesDir "mcrtx_jni.dll"
$instanceAssetsDir = Join-Path $instanceLibrariesDir "mcrtx_assets"
$instanceMinecraftDllPath = Join-Path $instanceMinecraftDir "mcrtx_jni.dll"
$instanceConfigPath = Join-Path $InstanceRoot "instance.cfg"
$deployStateDir = Join-Path $repoRoot "out\deploy-state"
$backupJar = Join-Path $deployStateDir "minecraft-b1.7.3-client.original.jar"
$deploymentInfo = Join-Path $deployStateDir "last-deploy.json"
$deployScript = Join-Path $repoRoot "scripts\build-patched-client.ps1"
$selfScriptPath = Join-Path $repoRoot "scripts\deploy-test-build.ps1"
$customJarTargets = @()
$didCopyJar = $false
$didCopyDll = $false
$didWriteMarker = $false

function Set-InstanceConfigValue {
    param(
        [string]$Path,
        [string]$Key,
        [string]$Value
    )

    $replacement = "$Key=$Value"
    $contentLines = [System.Collections.Generic.List[string]]::new()
    foreach ($contentLine in Get-Content -Path $Path) {
        $contentLines.Add($contentLine)
    }
    $escapedKey = [regex]::Escape($Key)
    $pattern = "^$escapedKey=.*$"
    $matchingIndices = @()

    for ($lineIndex = 0; $lineIndex -lt $contentLines.Count; $lineIndex += 1) {
        if ($contentLines[$lineIndex] -match $pattern) {
            $matchingIndices += $lineIndex
        }
    }

    if ($matchingIndices.Count -gt 0) {
        $contentLines[$matchingIndices[0]] = $replacement
        for ($matchIndex = $matchingIndices.Count - 1; $matchIndex -gt 0; $matchIndex -= 1) {
            $contentLines.RemoveAt($matchingIndices[$matchIndex])
        }
    } else {
        $contentLines.Add($replacement)
    }

    Set-Content -Path $Path -Value $contentLines -Encoding ASCII
}

function Convert-ToPrismPath {
    param([string]$Path)

    return $Path.Replace("\", "/")
}

function Ensure-PrismPreLaunchSync {
    param(
        [string]$InstanceConfig,
        [string]$ScriptPath,
        [string]$ConfigurationName
    )

    $prismScriptPath = Convert-ToPrismPath -Path $ScriptPath
    $command = 'powershell -NoProfile -ExecutionPolicy Bypass -File ' + $prismScriptPath + ' -Configuration ' + $ConfigurationName
    Set-InstanceConfigValue -Path $InstanceConfig -Key "OverrideCommands" -Value "true"
    Set-InstanceConfigValue -Path $InstanceConfig -Key "PreLaunchCommand" -Value $command
    return $command
}

function Remove-PrismPreLaunchSync {
    param([string]$InstanceConfig)

    Set-InstanceConfigValue -Path $InstanceConfig -Key "PreLaunchCommand" -Value ""
}

foreach ($requiredPath in @($InstanceRoot, (Split-Path $MinecraftLibraryJar -Parent), $deployScript, $instanceConfigPath, $selfScriptPath)) {
    if (-not (Test-Path $requiredPath)) {
        throw "Required path not found: $requiredPath"
    }
}

if (Test-Path $instanceLibrariesDir) {
    $customJarTargets = @(Get-ChildItem -Path $instanceLibrariesDir -Filter "customjar-*.jar" -File | ForEach-Object { $_.FullName })
}

if ($Build) {
    $buildDirectory = Join-Path $repoRoot "build"
    if (-not (Test-Path $buildDirectory)) {
        throw "Native build directory not found at $buildDirectory. Run cmake -S . -B build -G 'Visual Studio 17 2022' -A x64 first."
    }

    cmake --build $buildDirectory --config $Configuration --target mcrtx_jni
    if ($LASTEXITCODE -ne 0) {
        throw "cmake native build failed with exit code $LASTEXITCODE"
    }

    & $deployScript -Configuration $Configuration -OutputRoot $BundleRoot
    if ($LASTEXITCODE -ne 0) {
        throw "build-patched-client.ps1 failed with exit code $LASTEXITCODE"
    }
}

if ($Restore) {
    if (-not (Test-Path $backupJar)) {
        throw "Cannot restore because no backup jar exists at $backupJar"
    }

    if ($PSCmdlet.ShouldProcess($MinecraftLibraryJar, "Restore original Minecraft Beta 1.7.3 jar")) {
        Copy-Item $backupJar $MinecraftLibraryJar -Force
    }

    if (Test-Path $deployedDllPath) {
        if ($PSCmdlet.ShouldProcess($deployedDllPath, "Remove deployed mcrtx JNI DLL")) {
            Remove-Item $deployedDllPath -Force
        }
    }

    if (Test-Path $legacyInstanceDllPath) {
        if ($PSCmdlet.ShouldProcess($legacyInstanceDllPath, "Remove legacy deployed mcrtx JNI DLL")) {
            Remove-Item $legacyInstanceDllPath -Force
        }
    }

    if (Test-Path $deploymentInfo) {
        if ($PSCmdlet.ShouldProcess($deploymentInfo, "Remove deployment marker")) {
            Remove-Item $deploymentInfo -Force
        }
    }

    foreach ($customJarTarget in $customJarTargets) {
        if ($PSCmdlet.ShouldProcess($customJarTarget, "Remove Prism custom jar override")) {
            Remove-Item $customJarTarget -Force
        }
    }

    foreach ($pathToRemove in @($instanceLibraryDllPath, $instanceMinecraftDllPath)) {
        if (Test-Path $pathToRemove) {
            if ($PSCmdlet.ShouldProcess($pathToRemove, "Remove instance-local mcrtx JNI DLL")) {
                Remove-Item $pathToRemove -Force
            }
        }
    }

    foreach ($assetsPath in @($sharedAssetsDir, $instanceAssetsDir)) {
        if (Test-Path $assetsPath) {
            if ($PSCmdlet.ShouldProcess($assetsPath, "Remove deployed mcrtx atlas assets")) {
                Remove-Item $assetsPath -Recurse -Force
            }
        }
    }

    if ($PSCmdlet.ShouldProcess($instanceConfigPath, "Remove Prism pre-launch sync command")) {
        Remove-PrismPreLaunchSync -InstanceConfig $instanceConfigPath
    }

    Write-Host "Restored vanilla Beta 1.7.3 jar to $MinecraftLibraryJar"
    Write-Host "Removed deployed DLL from $libraryJarDirectory"
    return
}

foreach ($requiredPath in @($MinecraftLibraryJar, $patchedJar, $patchedDll, $bundleAssetsDir)) {
    if (-not (Test-Path $requiredPath)) {
        throw "Required path not found: $requiredPath"
    }
}

New-Item -ItemType Directory -Force -Path $deployStateDir | Out-Null
New-Item -ItemType Directory -Force -Path $libraryJarDirectory | Out-Null
New-Item -ItemType Directory -Force -Path $instanceLibrariesDir | Out-Null
New-Item -ItemType Directory -Force -Path $instanceMinecraftDir | Out-Null

$targetJarHash = Get-FileHashString -Path $MinecraftLibraryJar
$patchedJarHash = Get-FileHashString -Path $patchedJar

if (-not (Test-Path $backupJar)) {
    if ($targetJarHash -eq $patchedJarHash) {
        Write-Warning "Target jar already matches the patched jar and no vanilla backup exists yet. Restore will not be available until a vanilla backup is placed at $backupJar."
    } else {
        if ($PSCmdlet.ShouldProcess($backupJar, "Create one-time backup of the vanilla client jar")) {
            Copy-Item $MinecraftLibraryJar $backupJar -Force
        }
    }
}

if ($targetJarHash -ne $patchedJarHash) {
    if ($PSCmdlet.ShouldProcess($MinecraftLibraryJar, "Deploy patched Beta 1.7.3 client jar")) {
        Copy-Item $patchedJar $MinecraftLibraryJar -Force
        $didCopyJar = $true
    }
}

foreach ($customJarTarget in $customJarTargets) {
    if ($PSCmdlet.ShouldProcess($customJarTarget, "Deploy patched Beta 1.7.3 custom jar override")) {
        Copy-Item $patchedJar $customJarTarget -Force
        $didCopyJar = $true
    }
}

if ($PSCmdlet.ShouldProcess($deployedDllPath, "Deploy mcrtx JNI DLL next to the shared PrismLauncher Beta 1.7.3 jar")) {
    Copy-Item $patchedDll $deployedDllPath -Force
    $didCopyDll = $true
}

foreach ($dllTarget in @($instanceLibraryDllPath, $instanceMinecraftDllPath)) {
    if ($PSCmdlet.ShouldProcess($dllTarget, "Deploy instance-local mcrtx JNI DLL")) {
        Copy-Item $patchedDll $dllTarget -Force
        $didCopyDll = $true
    }
}

foreach ($assetsTarget in @($sharedAssetsDir, $instanceAssetsDir)) {
    if ($OverwriteAssets -and (Test-Path $assetsTarget)) {
        if ($PSCmdlet.ShouldProcess($assetsTarget, "Clear existing mcrtx atlas assets before redeploy")) {
            Remove-Item $assetsTarget -Recurse -Force
        }
    }

    if ($PSCmdlet.ShouldProcess($assetsTarget, "Deploy mcrtx atlas assets (preserving existing files)")) {
        New-Item -ItemType Directory -Force -Path $assetsTarget | Out-Null
        $sourceItems = Get-ChildItem -Path $bundleAssetsDir -Recurse -File
        foreach ($sourceItem in $sourceItems) {
            $relativePath = $sourceItem.FullName.Substring($bundleAssetsDir.Length).TrimStart('\', '/')
            $destinationPath = Join-Path $assetsTarget $relativePath
            if (-not $OverwriteAssets -and (Test-Path $destinationPath)) {
                continue
            }
            $destinationDir = Split-Path $destinationPath -Parent
            if ($destinationDir -and -not (Test-Path $destinationDir)) {
                New-Item -ItemType Directory -Force -Path $destinationDir | Out-Null
            }
            Copy-Item $sourceItem.FullName $destinationPath -Force
        }
    }
}

if (Test-Path $legacyInstanceDllPath) {
    if ($PSCmdlet.ShouldProcess($legacyInstanceDllPath, "Remove stale instance-native mcrtx JNI DLL")) {
        Remove-Item $legacyInstanceDllPath -Force
    }
}

$deploymentRecord = [ordered]@{
    deployedAt = (Get-Date).ToString("o")
    instanceRoot = $InstanceRoot
    minecraftLibraryJar = $MinecraftLibraryJar
    deployedDll = $deployedDllPath
    instanceLibraryDll = $instanceLibraryDllPath
    instanceMinecraftDll = $instanceMinecraftDllPath
    sharedAssetsDir = $sharedAssetsDir
    instanceAssetsDir = $instanceAssetsDir
    customJarTargets = $customJarTargets
    bundleRoot = $BundleRoot
    configuration = $Configuration
    patchedJar = $patchedJar
    patchedDll = $patchedDll
}

if ($PSCmdlet.ShouldProcess($deploymentInfo, "Write deployment marker")) {
    $deploymentRecord | ConvertTo-Json | Set-Content -Path $deploymentInfo -Encoding ASCII
    $didWriteMarker = $true
}

$preLaunchCommand = ""
if ($PSCmdlet.ShouldProcess($instanceConfigPath, "Configure Prism pre-launch sync command")) {
    $preLaunchCommand = Ensure-PrismPreLaunchSync -InstanceConfig $instanceConfigPath -ScriptPath $selfScriptPath -ConfigurationName $Configuration
}

if ($WhatIfPreference) {
    Write-Host "Dry run complete for Beta 1.7.3 deployment"
    return
}

if ($didCopyJar) {
    Write-Host "Deployed patched Beta 1.7.3 client jar to $MinecraftLibraryJar"
} else {
    Write-Host "Patched Beta 1.7.3 client jar was already in place"
}

if ($customJarTargets.Count -gt 0) {
    Write-Host "Deployed patched custom jar override(s): $($customJarTargets -join '; ')"
}

if ($didCopyDll) {
    Write-Host "Deployed mcrtx_jni.dll to $deployedDllPath"
    Write-Host "Deployed instance-local mcrtx_jni.dll to $instanceLibraryDllPath"
}

Write-Host "Deployed terrain atlas assets to $sharedAssetsDir"
Write-Host "Deployed terrain atlas assets to $instanceAssetsDir"

if ($didWriteMarker) {
    Write-Host "Updated deployment marker at $deploymentInfo"
}

if ($preLaunchCommand) {
    Write-Host "Configured Prism pre-launch sync command in $instanceConfigPath"
}

Write-Host "Launch the b1.7.3 PrismLauncher instance to test the current build"