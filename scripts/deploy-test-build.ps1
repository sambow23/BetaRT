[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [string]$InstanceRoot = "C:\Users\cr\AppData\Roaming\PrismLauncher\instances\b1.7.3",
    [string]$MinecraftLibraryJar = "C:\Users\cr\AppData\Roaming\PrismLauncher\libraries\com\mojang\minecraft\b1.7.3\minecraft-b1.7.3-client.jar",
    [string]$BundleRoot,
    [string]$Configuration = "Release",
    [ValidateSet("lwjgl2", "lwjgl3", "glfw")]
    [string]$PlatformBackend = "lwjgl3",
    [ValidateSet("platform", "native")]
    [string]$InputBackend = "native",
    [ValidateSet("single", "overlay", "dual", "detached", "separate", "standalone")]
    [string]$WindowMode = "overlay",
    [switch]$UseNoApplet,
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
$instanceMmcPackPath = Join-Path $InstanceRoot "mmc-pack.json"
$instancePatchesDir = Join-Path $InstanceRoot "patches"
$instanceMinecraftPatchPath = Join-Path $instancePatchesDir "net.minecraft.json"
$instanceRemixConfigPath = Join-Path $instanceMinecraftDir "rtx.conf"
$prismRoot = Split-Path (Split-Path $InstanceRoot -Parent) -Parent
$prismMetaRoot = Join-Path $prismRoot "meta"
$minecraftMetaPath = Join-Path $prismMetaRoot "net.minecraft\b1.7.3.json"
$deployStateDir = Join-Path $repoRoot "out\deploy-state"
$backupJar = Join-Path $deployStateDir "minecraft-b1.7.3-client.original.jar"
$deploymentInfo = Join-Path $deployStateDir "last-deploy.json"
$deployScript = Join-Path $repoRoot "scripts\build-patched-client.ps1"
$selfScriptPath = Join-Path $repoRoot "scripts\deploy-test-build.ps1"
$customJarTargets = @()
$didCopyJar = $false
$didCopyDll = $false
$didWriteMarker = $false
$defaultLwjgl2Version = "2.9.4-nightly-20150209"
$defaultLwjgl3Version = "3.3.3"

function Read-JsonFile {
    param([string]$Path)

    return Get-Content -Path $Path -Raw | ConvertFrom-Json
}

function Write-JsonFile {
    param(
        [string]$Path,
        [Parameter(ValueFromPipeline = $true)]
        [object]$InputObject
    )

    $json = $InputObject | ConvertTo-Json -Depth 32
    Set-Content -Path $Path -Value $json -Encoding ASCII
}

function Get-PlatformComponentSpec {
    param([string]$Backend)

    switch ($Backend.ToLowerInvariant()) {
        "lwjgl3" {
            return [pscustomobject]@{
                BackendId = "lwjgl3"
                ComponentUid = "org.lwjgl3"
                CachedName = "LWJGL 3"
                Version = $defaultLwjgl3Version
            }
        }
        "glfw" {
            return [pscustomobject]@{
                BackendId = "lwjgl3"
                ComponentUid = "org.lwjgl3"
                CachedName = "LWJGL 3"
                Version = $defaultLwjgl3Version
            }
        }
        default {
            return [pscustomobject]@{
                BackendId = "lwjgl2"
                ComponentUid = "org.lwjgl"
                CachedName = "LWJGL 2"
                Version = $defaultLwjgl2Version
            }
        }
    }
}

function Set-PrismMinecraftPlatformPatch {
    param(
        [string]$MinecraftMetaPath,
        [string]$PatchPath,
        [pscustomobject]$PlatformSpec,
        [bool]$UseNoApplet
    )

    $minecraftPatch = Read-JsonFile -Path $MinecraftMetaPath
    $minecraftPatch.requires = @(
        [ordered]@{
            suggests = $PlatformSpec.Version
            uid = $PlatformSpec.ComponentUid
        }
    )

    $traits = [System.Collections.Generic.List[string]]::new()
    if ($minecraftPatch.PSObject.Properties.Name -contains "+traits") {
        foreach ($trait in $minecraftPatch.'+traits') {
            if (-not [string]::IsNullOrWhiteSpace($trait) -and -not $traits.Contains($trait)) {
                $traits.Add($trait)
            }
        }
    }

    if ($UseNoApplet) {
        if (-not $traits.Contains("noapplet")) {
            $traits.Add("noapplet")
        }
    } else {
        for ($traitIndex = $traits.Count - 1; $traitIndex -ge 0; $traitIndex -= 1) {
            if ($traits[$traitIndex] -eq "noapplet") {
                $traits.RemoveAt($traitIndex)
            }
        }
    }

    $minecraftPatch.'+traits' = @($traits)
    New-Item -ItemType Directory -Force -Path (Split-Path $PatchPath -Parent) | Out-Null
    Write-JsonFile -Path $PatchPath -InputObject $minecraftPatch
}

function Sync-PrismPlatformComponent {
    param(
        [string]$PackPath,
        [pscustomobject]$PlatformSpec
    )

    $pack = Read-JsonFile -Path $PackPath
    $components = [System.Collections.Generic.List[object]]::new()
    foreach ($component in $pack.components) {
        if ($component.uid -eq "org.lwjgl" -or $component.uid -eq "org.lwjgl3") {
            continue
        }

        if ($component.uid -eq "net.minecraft") {
            $component.cachedRequires = @(
                [ordered]@{
                    suggests = $PlatformSpec.Version
                    uid = $PlatformSpec.ComponentUid
                }
            )
        }
        $components.Add($component)
    }

    $platformComponent = [pscustomobject]@{
        cachedName = $PlatformSpec.CachedName
        cachedVersion = $PlatformSpec.Version
        cachedVolatile = $true
        dependencyOnly = $true
        uid = $PlatformSpec.ComponentUid
        version = $PlatformSpec.Version
    }

    $orderedComponents = [System.Collections.Generic.List[object]]::new()
    $orderedComponents.Add($platformComponent)
    foreach ($component in $components) {
        $orderedComponents.Add($component)
    }

    $pack.components = $orderedComponents
    Write-JsonFile -Path $PackPath -InputObject $pack
}

function Set-PrismPlatformBackend {
    param(
        [string]$PackPath,
        [string]$MinecraftMetaPath,
        [string]$PatchPath,
        [string]$Backend,
        [bool]$UseNoApplet
    )

    $platformSpec = Get-PlatformComponentSpec -Backend $Backend
    Set-PrismMinecraftPlatformPatch -MinecraftMetaPath $MinecraftMetaPath -PatchPath $PatchPath -PlatformSpec $platformSpec -UseNoApplet $UseNoApplet
    Sync-PrismPlatformComponent -PackPath $PackPath -PlatformSpec $platformSpec
    return $platformSpec
}

function Set-InstanceConfigValue {
    param(
        [string]$Path,
        [string]$Key,
        [string]$Value,
        [string]$Section = "General"
    )

    $replacement = "$Key=$Value"
    $contentLines = [System.Collections.Generic.List[string]]::new()
    foreach ($contentLine in Get-Content -Path $Path) {
        $contentLines.Add($contentLine)
    }

    $escapedKey = [regex]::Escape($Key)
    $pattern = "^$escapedKey=.*$"
    $sectionHeader = "[$Section]"
    $sectionStart = -1
    for ($lineIndex = 0; $lineIndex -lt $contentLines.Count; $lineIndex += 1) {
        if ($contentLines[$lineIndex] -eq $sectionHeader) {
            $sectionStart = $lineIndex
            break
        }
    }

    if ($sectionStart -lt 0) {
        throw "Section [$Section] not found in $Path"
    }

    $sectionEnd = $contentLines.Count
    for ($lineIndex = $sectionStart + 1; $lineIndex -lt $contentLines.Count; $lineIndex += 1) {
        if ($contentLines[$lineIndex] -match '^\[.+\]$') {
            $sectionEnd = $lineIndex
            break
        }
    }

    $matchingIndices = @()
    $duplicateIndices = @()

    for ($lineIndex = 0; $lineIndex -lt $contentLines.Count; $lineIndex += 1) {
        if ($contentLines[$lineIndex] -match $pattern) {
            if ($lineIndex -gt $sectionStart -and $lineIndex -lt $sectionEnd) {
                $matchingIndices += $lineIndex
            } else {
                $duplicateIndices += $lineIndex
            }
        }
    }

    if ($matchingIndices.Count -gt 0) {
        $contentLines[$matchingIndices[0]] = $replacement
        for ($matchIndex = $matchingIndices.Count - 1; $matchIndex -gt 0; $matchIndex -= 1) {
            $duplicateIndices += $matchingIndices[$matchIndex]
        }
    } else {
        $contentLines.Insert($sectionEnd, $replacement)
    }

    if ($duplicateIndices.Count -gt 0) {
        $duplicateIndices = @($duplicateIndices | Sort-Object -Descending -Unique)
        foreach ($duplicateIndex in $duplicateIndices) {
            $contentLines.RemoveAt($duplicateIndex)
        }
    }

    Set-Content -Path $Path -Value $contentLines -Encoding ASCII
}

function Get-InstanceConfigValue {
    param(
        [string]$Path,
        [string]$Key,
        [string]$Section = "General"
    )

    $contentLines = Get-Content -Path $Path
    $escapedKey = [regex]::Escape($Key)
    $sectionHeader = "[$Section]"
    $sectionStart = -1
    for ($lineIndex = 0; $lineIndex -lt $contentLines.Count; $lineIndex += 1) {
        if ($contentLines[$lineIndex] -eq $sectionHeader) {
            $sectionStart = $lineIndex
            break
        }
    }

    if ($sectionStart -lt 0) {
        return ""
    }

    $sectionEnd = $contentLines.Count
    for ($lineIndex = $sectionStart + 1; $lineIndex -lt $contentLines.Count; $lineIndex += 1) {
        if ($contentLines[$lineIndex] -match '^\[.+\]$') {
            $sectionEnd = $lineIndex
            break
        }
    }

    for ($lineIndex = $sectionStart + 1; $lineIndex -lt $sectionEnd; $lineIndex += 1) {
        if ($contentLines[$lineIndex] -match "^$escapedKey=(.*)$") {
            return $Matches[1]
        }
    }

    return ""
}

function Convert-ToPrismPath {
    param([string]$Path)

    return ($Path -replace '\\', '/')
}

function Ensure-PrismPreLaunchSync {
    param(
        [string]$InstanceConfig,
        [string]$ScriptPath,
        [string]$ConfigurationName,
        [string]$ConfiguredWindowMode,
        [string]$ConfiguredPlatformBackend,
        [string]$ConfiguredInputBackend,
        [bool]$ConfiguredUseNoApplet
    )

    $prismScriptPath = Convert-ToPrismPath -Path $ScriptPath
    $command = 'powershell -NoProfile -ExecutionPolicy Bypass -File {0} -Configuration {1} -WindowMode {2} -PlatformBackend {3} -InputBackend {4}' -f @(
        $prismScriptPath,
        $ConfigurationName,
        $ConfiguredWindowMode,
        $ConfiguredPlatformBackend,
        $ConfiguredInputBackend
    )
    if ($ConfiguredUseNoApplet) {
        $command += ' -UseNoApplet'
    }
    Set-InstanceConfigValue -Path $InstanceConfig -Key "OverrideCommands" -Value "true"
    Set-InstanceConfigValue -Path $InstanceConfig -Key "PreLaunchCommand" -Value $command
    return $command
}

function Remove-PrismPreLaunchSync {
    param([string]$InstanceConfig)

    Set-InstanceConfigValue -Path $InstanceConfig -Key "PreLaunchCommand" -Value ""
}

function Set-PrismRuntimeEnvironment {
    param(
        [string]$InstanceConfig,
        [string]$Mode,
        [string]$ConfiguredPlatformBackend,
        [string]$ConfiguredInputBackend
    )

    $envValue = '{\"MCRTX_WINDOW_MODE\":\"' + $Mode + '\",\"MCRTX_PLATFORM_BACKEND\":\"' + $ConfiguredPlatformBackend + '\",\"MCRTX_INPUT_BACKEND\":\"' + $ConfiguredInputBackend + '\"}'
    Set-InstanceConfigValue -Path $InstanceConfig -Key "OverrideEnv" -Value "true"
    Set-InstanceConfigValue -Path $InstanceConfig -Key "Env" -Value $envValue
}

function Remove-LegacyMcrtxJavaOverrides {
    param([string]$InstanceConfig)

    $currentJvmArgs = Get-InstanceConfigValue -Path $InstanceConfig -Key "JvmArgs"
    if ([string]::IsNullOrWhiteSpace($currentJvmArgs)) {
        Set-InstanceConfigValue -Path $InstanceConfig -Key "OverrideJavaArgs" -Value "false"
        Set-InstanceConfigValue -Path $InstanceConfig -Key "JvmArgs" -Value '""'
        return ""
    }

    $normalizedJvmArgs = $currentJvmArgs.Trim()
    if ($normalizedJvmArgs.Length -ge 2 -and $normalizedJvmArgs.StartsWith('"') -and $normalizedJvmArgs.EndsWith('"')) {
        $normalizedJvmArgs = $normalizedJvmArgs.Substring(1, $normalizedJvmArgs.Length - 2)
    }

    $normalizedJvmArgs = $normalizedJvmArgs -replace '(^|\s)-Dmcrtx\.(platformBackend|inputBackend|windowMode)=\S+', ' '
    $remainingArgs = @($normalizedJvmArgs -split '\s+' | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) -join ' '

    if ([string]::IsNullOrWhiteSpace($remainingArgs)) {
        Set-InstanceConfigValue -Path $InstanceConfig -Key "OverrideJavaArgs" -Value "false"
        Set-InstanceConfigValue -Path $InstanceConfig -Key "JvmArgs" -Value '""'
        return ""
    }

    Set-InstanceConfigValue -Path $InstanceConfig -Key "OverrideJavaArgs" -Value "true"
    Set-InstanceConfigValue -Path $InstanceConfig -Key "JvmArgs" -Value ('"' + $remainingArgs + '"')
    return $remainingArgs
}

function Set-RemixConfigValue {
    param(
        [string]$Path,
        [string]$Key,
        [string]$Value
    )

    $replacement = "$Key = $Value"
    $contentLines = [System.Collections.Generic.List[string]]::new()
    if (Test-Path $Path) {
        foreach ($contentLine in Get-Content -Path $Path) {
            $contentLines.Add($contentLine)
        }
    }

    $escapedKey = [regex]::Escape($Key)
    $pattern = "^\s*$escapedKey\s*=.*$"
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

function Ensure-RemixViewModelConfig {
    param([string]$ConfigPath)

    if (-not (Test-Path $ConfigPath)) {
        New-Item -ItemType File -Path $ConfigPath -Force | Out-Null
    }

    Set-RemixConfigValue -Path $ConfigPath -Key "rtx.viewModel.enable" -Value "True"
    Set-RemixConfigValue -Path $ConfigPath -Key "rtx.enableNearPlaneOverride" -Value "True"
    Set-RemixConfigValue -Path $ConfigPath -Key "rtx.nearPlaneOverride" -Value "0.001"
    Set-RemixConfigValue -Path $ConfigPath -Key "rtx.volumetrics.froxelMaxDistanceMeters" -Value "256"
}

foreach ($requiredPath in @($InstanceRoot, (Split-Path $MinecraftLibraryJar -Parent), $deployScript, $instanceConfigPath, $instanceMmcPackPath, $minecraftMetaPath, $selfScriptPath)) {
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

    if ($PSCmdlet.ShouldProcess($instanceMmcPackPath, "Reset Prism platform backend to LWJGL 2")) {
        Set-PrismPlatformBackend -PackPath $instanceMmcPackPath -MinecraftMetaPath $minecraftMetaPath -PatchPath $instanceMinecraftPatchPath -Backend "lwjgl2" -UseNoApplet:$false | Out-Null
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
New-Item -ItemType Directory -Force -Path $instancePatchesDir | Out-Null

$platformSpec = $null
if ($PSCmdlet.ShouldProcess($instanceMmcPackPath, "Configure Prism platform component backend")) {
    $platformSpec = Set-PrismPlatformBackend -PackPath $instanceMmcPackPath -MinecraftMetaPath $minecraftMetaPath -PatchPath $instanceMinecraftPatchPath -Backend $PlatformBackend -UseNoApplet:$UseNoApplet.IsPresent
}

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
    platformBackend = $PlatformBackend
    inputBackend = $InputBackend
    windowMode = $WindowMode
    useNoApplet = $UseNoApplet.IsPresent
    platformComponentUid = if ($platformSpec) { $platformSpec.ComponentUid } else { "" }
    platformComponentVersion = if ($platformSpec) { $platformSpec.Version } else { "" }
    patchedJar = $patchedJar
    patchedDll = $patchedDll
}

if ($PSCmdlet.ShouldProcess($deploymentInfo, "Write deployment marker")) {
    $deploymentRecord | ConvertTo-Json | Set-Content -Path $deploymentInfo -Encoding ASCII
    $didWriteMarker = $true
}

$preLaunchCommand = ""
if ($PSCmdlet.ShouldProcess($instanceConfigPath, "Configure Prism pre-launch sync command")) {
    $preLaunchCommand = Ensure-PrismPreLaunchSync -InstanceConfig $instanceConfigPath -ScriptPath $selfScriptPath -ConfigurationName $Configuration -ConfiguredWindowMode $WindowMode -ConfiguredPlatformBackend $PlatformBackend -ConfiguredInputBackend $InputBackend -ConfiguredUseNoApplet:$UseNoApplet.IsPresent
}

if ($PSCmdlet.ShouldProcess($instanceConfigPath, "Configure Prism runtime environment")) {
    Set-PrismRuntimeEnvironment -InstanceConfig $instanceConfigPath -Mode $WindowMode -ConfiguredPlatformBackend $PlatformBackend -ConfiguredInputBackend $InputBackend
}

if ($PSCmdlet.ShouldProcess($instanceConfigPath, "Remove stale mcrtx JVM launch overrides")) {
    Remove-LegacyMcrtxJavaOverrides -InstanceConfig $instanceConfigPath | Out-Null
}

if ($PSCmdlet.ShouldProcess($instanceRemixConfigPath, "Configure Remix viewmodel runtime settings")) {
    Ensure-RemixViewModelConfig -ConfigPath $instanceRemixConfigPath
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

Write-Host "Configured Prism runtime environment windowMode='$WindowMode' platformBackend='$PlatformBackend' inputBackend='$InputBackend' in $instanceConfigPath"
Write-Host "Configured Prism noapplet trait: $($UseNoApplet.IsPresent) in $instanceMinecraftPatchPath"

if ($platformSpec) {
    Write-Host "Configured Prism component graph for $($platformSpec.CachedName) $($platformSpec.Version) in $instanceMmcPackPath"
    Write-Host "Updated local Minecraft patch at $instanceMinecraftPatchPath to require $($platformSpec.ComponentUid) $($platformSpec.Version)"
}

Write-Host "Configured Remix viewmodel settings in $instanceRemixConfigPath"

Write-Host "Launch the b1.7.3 PrismLauncher instance to test the current build"