[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [string]$PrismRoot,
    [string]$InstanceName = "b1.7.3",
    [string]$InstanceRoot,
    [string]$MinecraftLibraryJar,
    [string]$JavaHome,
    [string]$BundleRoot,
    [string]$Configuration = "Release",
    [ValidateSet("lwjgl2", "lwjgl3", "glfw")]
    [string]$PlatformBackend = "lwjgl3",
    [ValidateSet("platform", "native")]
    [string]$InputBackend = "native",
    [ValidateSet("single", "overlay", "dual", "detached", "separate", "standalone", "single-native")]
    [string]$WindowMode = "overlay",
    [switch]$UseNoApplet,
    [switch]$Build,
    [switch]$Restore,
    [switch]$OverwriteAssets,
    [double]$NoCullDistance = 40,
    [switch]$ForceRequestedLaunchConfig,
    [switch]$VerboseInputLogging
)

$ErrorActionPreference = "Stop"

if (-not $PrismRoot) {
    if ($InstanceRoot) {
        $PrismRoot = Split-Path (Split-Path $InstanceRoot -Parent) -Parent
    } else {
        if (-not $env:APPDATA) {
            throw "PrismLauncher could not be located because APPDATA is not set. Pass -PrismRoot explicitly."
        }
        $PrismRoot = Join-Path $env:APPDATA "PrismLauncher"
    }
}
$PrismRoot = [System.IO.Path]::GetFullPath($PrismRoot)

if (-not $InstanceRoot) {
    $InstanceRoot = Join-Path (Join-Path $PrismRoot "instances") $InstanceName
}
if (-not $MinecraftLibraryJar) {
    $MinecraftLibraryJar = Join-Path $PrismRoot "libraries\com\mojang\minecraft\b1.7.3\minecraft-b1.7.3-client.jar"
}

function Get-FileHashString {
    param([string]$Path)

    if (-not (Test-Path $Path)) {
        return $null
    }

    return (Get-FileHash -Algorithm SHA256 -Path $Path).Hash
}

function Sync-DirectoryFiles {
    param(
        [string]$SourcePath,
        [string]$DestinationPath
    )

    $destinationRoot = $DestinationPath
    New-Item -ItemType Directory -Force -Path $destinationRoot | Out-Null

    $sourceFiles = Get-ChildItem -Path $SourcePath -Recurse -File
    $trimChars = [char[]]@('\', '/')

    foreach ($sourceFile in $sourceFiles) {
        $relativePath = $sourceFile.FullName.Substring($SourcePath.Length).TrimStart($trimChars)

        $targetFilePath = Join-Path $destinationRoot $relativePath
        $destinationDir = Split-Path $targetFilePath -Parent
        if ($destinationDir -and -not (Test-Path $destinationDir)) {
            New-Item -ItemType Directory -Force -Path $destinationDir | Out-Null
        }

        Copy-Item $sourceFile.FullName $targetFilePath -Force
    }
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
if (-not $BundleRoot) {
    $BundleRoot = Join-Path $repoRoot "out\patched-client"
}

$patchedJar = Join-Path $BundleRoot "minecraft-b1.7.3-client-mcrtx.jar"
$patchedDll = Join-Path $BundleRoot "mcrtx_jni.dll"
$patchedPdb = Join-Path $BundleRoot "mcrtx_jni.pdb"
$bundleAssetsDir = Join-Path $BundleRoot "mcrtx_assets"
$libraryJarDirectory = Split-Path $MinecraftLibraryJar -Parent
$deployedDllPath = Join-Path $libraryJarDirectory "mcrtx_jni.dll"
$deployedPdbPath = Join-Path $libraryJarDirectory "mcrtx_jni.pdb"
$sharedAssetsDir = Join-Path $libraryJarDirectory "mcrtx_assets"
$legacyInstanceDllPath = Join-Path (Join-Path $InstanceRoot "natives") "mcrtx_jni.dll"
$legacyInstancePdbPath = Join-Path (Join-Path $InstanceRoot "natives") "mcrtx_jni.pdb"
$instanceLibrariesDir = Join-Path $InstanceRoot "libraries"
$instanceMinecraftDir = Join-Path $InstanceRoot "minecraft"
$instanceLibraryDllPath = Join-Path $instanceLibrariesDir "mcrtx_jni.dll"
$instanceLibraryPdbPath = Join-Path $instanceLibrariesDir "mcrtx_jni.pdb"
$instanceAssetsDir = Join-Path $instanceLibrariesDir "mcrtx_assets"
$instanceMinecraftDllPath = Join-Path $instanceMinecraftDir "mcrtx_jni.dll"
$instanceMinecraftPdbPath = Join-Path $instanceMinecraftDir "mcrtx_jni.pdb"
$runtimeConfigPath = Join-Path $instanceMinecraftDir "mcrtx-runtime.env"
$instanceConfigPath = Join-Path $InstanceRoot "instance.cfg"
$instanceMmcPackPath = Join-Path $InstanceRoot "mmc-pack.json"
$instancePatchesDir = Join-Path $InstanceRoot "patches"
$instanceMinecraftPatchPath = Join-Path $instancePatchesDir "net.minecraft.json"
$instanceRemixConfigPath = Join-Path $instanceMinecraftDir "rtx.conf"
$prismMetaRoot = Join-Path $prismRoot "meta"
$minecraftMetaPath = Join-Path $prismMetaRoot "net.minecraft\b1.7.3.json"
$deployStateDir = Join-Path $repoRoot "out\deploy-state"
$backupJar = Join-Path $deployStateDir "minecraft-b1.7.3-client.original.jar"
$deploymentInfo = Join-Path $deployStateDir "last-deploy.json"
$deployScript = Join-Path $repoRoot "scripts\build-patched-client.ps1"
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

function Resolve-LaunchConfig {
    param(
        [string]$DeploymentInfoPath,
        [string]$RequestedPlatformBackend,
        [string]$RequestedInputBackend,
        [string]$RequestedWindowMode,
        [bool]$RequestedUseNoApplet,
        [double]$RequestedNoCullDistance,
        [bool]$RequestedVerboseInputLogging,
        [bool]$ForceRequestedConfig
    )

    $resolvedConfig = [ordered]@{
        PlatformBackend = $RequestedPlatformBackend
        InputBackend = $RequestedInputBackend
        WindowMode = $RequestedWindowMode
        UseNoApplet = $RequestedUseNoApplet
        NoCullDistance = $RequestedNoCullDistance
        VerboseInputLogging = $RequestedVerboseInputLogging
    }

    if ($ForceRequestedConfig -or -not (Test-Path $DeploymentInfoPath)) {
        return [pscustomobject]$resolvedConfig
    }

    try {
        $lastDeploy = Read-JsonFile -Path $DeploymentInfoPath
    } catch {
        return [pscustomobject]$resolvedConfig
    }

    if ($null -eq $lastDeploy) {
        return [pscustomobject]$resolvedConfig
    }

    $pinnedPlatformBackend = [string]$lastDeploy.platformBackend
    $pinnedInputBackend = [string]$lastDeploy.inputBackend
    $pinnedWindowMode = [string]$lastDeploy.windowMode
    $pinnedUseNoApplet = [bool]$lastDeploy.useNoApplet
    $pinnedNoCullDistance = $RequestedNoCullDistance
    if ($lastDeploy.PSObject.Properties.Name -contains "noCullDistance") {
        $pinnedNoCullDistance = [double]$lastDeploy.noCullDistance
    }
    $pinnedVerboseInputLogging = [bool]$lastDeploy.verboseInputLogging
    if (([string]::IsNullOrWhiteSpace($pinnedPlatformBackend)) -or ([string]::IsNullOrWhiteSpace($pinnedInputBackend)) -or ([string]::IsNullOrWhiteSpace($pinnedWindowMode))) {
        return [pscustomobject]$resolvedConfig
    }

    if (($RequestedPlatformBackend -ne $pinnedPlatformBackend) -or ($RequestedInputBackend -ne $pinnedInputBackend) -or ($RequestedWindowMode -ne $pinnedWindowMode) -or ($RequestedUseNoApplet -ne $pinnedUseNoApplet) -or ($RequestedNoCullDistance -ne $pinnedNoCullDistance) -or ($RequestedVerboseInputLogging -ne $pinnedVerboseInputLogging)) {
        Write-Host "Using pinned launch config from $DeploymentInfoPath instead of requested arguments: windowMode='$pinnedWindowMode' platformBackend='$pinnedPlatformBackend' inputBackend='$pinnedInputBackend' useNoApplet='$pinnedUseNoApplet' noCullDistance='$pinnedNoCullDistance' verboseInputLogging='$pinnedVerboseInputLogging'"
    }

    $resolvedConfig.PlatformBackend = $pinnedPlatformBackend
    $resolvedConfig.InputBackend = $pinnedInputBackend
    $resolvedConfig.WindowMode = $pinnedWindowMode
    $resolvedConfig.UseNoApplet = $pinnedUseNoApplet
    $resolvedConfig.NoCullDistance = $pinnedNoCullDistance
    $resolvedConfig.VerboseInputLogging = $pinnedVerboseInputLogging
    return [pscustomobject]$resolvedConfig
}

$resolvedLaunchConfig = Resolve-LaunchConfig -DeploymentInfoPath $deploymentInfo -RequestedPlatformBackend $PlatformBackend -RequestedInputBackend $InputBackend -RequestedWindowMode $WindowMode -RequestedUseNoApplet:$UseNoApplet.IsPresent -RequestedNoCullDistance $NoCullDistance -RequestedVerboseInputLogging:$VerboseInputLogging.IsPresent -ForceRequestedConfig:$ForceRequestedLaunchConfig.IsPresent
$PlatformBackend = $resolvedLaunchConfig.PlatformBackend
$InputBackend = $resolvedLaunchConfig.InputBackend
$WindowMode = $resolvedLaunchConfig.WindowMode
$useNoAppletEnabled = [bool]$resolvedLaunchConfig.UseNoApplet
$NoCullDistance = [double]$resolvedLaunchConfig.NoCullDistance
$verboseInputLoggingEnabled = [bool]$resolvedLaunchConfig.VerboseInputLogging

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

function Remove-McrtxPrismPreLaunchCommand {
    param([string]$InstanceConfig)

    $preLaunchCommand = Get-InstanceConfigValue -Path $InstanceConfig -Key "PreLaunchCommand"
    if (-not $preLaunchCommand -or $preLaunchCommand -notmatch '(?i)deploy-test-build\.ps1') {
        return $false
    }

    Set-InstanceConfigValue -Path $InstanceConfig -Key "PreLaunchCommand" -Value ""
    return $true
}

function Set-PrismRuntimeEnvironment {
    param(
        [string]$InstanceConfig,
        [string]$Mode,
        [string]$ConfiguredPlatformBackend,
        [string]$ConfiguredInputBackend,
        [double]$ConfiguredNoCullDistance,
        [bool]$ConfiguredVerboseInputLogging
    )

    $envPairs = [System.Collections.Generic.List[string]]::new()
    $envPairs.Add('\"MCRTX_WINDOW_MODE\":\"' + $Mode + '\"')
    $envPairs.Add('\"MCRTX_PLATFORM_BACKEND\":\"' + $ConfiguredPlatformBackend + '\"')
    $envPairs.Add('\"MCRTX_INPUT_BACKEND\":\"' + $ConfiguredInputBackend + '\"')
    $envPairs.Add('\"MCRTX_NO_CULL_DISTANCE\":\"' + $ConfiguredNoCullDistance.ToString([System.Globalization.CultureInfo]::InvariantCulture) + '\"')
    $envPairs.Add('\"MCRTX_FLOATING_ORIGIN\":\"1\"')
    if ($ConfiguredVerboseInputLogging) {
        $envPairs.Add('\"MCRTX_VERBOSE_INPUT_LOG\":\"1\"')
    }
    $envValue = '{' + ($envPairs -join ',') + '}'
    Set-InstanceConfigValue -Path $InstanceConfig -Key "OverrideEnv" -Value "true"
    Set-InstanceConfigValue -Path $InstanceConfig -Key "Env" -Value $envValue
}

function Set-McrtxRuntimeConfig {
    param(
        [string]$Path,
        [string]$Mode,
        [string]$ConfiguredPlatformBackend,
        [string]$ConfiguredInputBackend,
        [double]$ConfiguredNoCullDistance,
        [bool]$ConfiguredVerboseInputLogging
    )

    $contentLines = [System.Collections.Generic.List[string]]::new()
    if (Test-Path $Path) {
        foreach ($contentLine in Get-Content -Path $Path) {
            $contentLines.Add($contentLine)
        }
    }

    $managedEntries = @(
        [pscustomobject]@{ Key = "MCRTX_WINDOW_MODE"; Value = $Mode; Remove = $false }
        [pscustomobject]@{ Key = "MCRTX_PLATFORM_BACKEND"; Value = $ConfiguredPlatformBackend; Remove = $false }
        [pscustomobject]@{ Key = "MCRTX_INPUT_BACKEND"; Value = $ConfiguredInputBackend; Remove = $false }
        [pscustomobject]@{
            Key = "MCRTX_NO_CULL_DISTANCE"
            Value = $ConfiguredNoCullDistance.ToString([System.Globalization.CultureInfo]::InvariantCulture)
            Remove = $false
        }
        [pscustomobject]@{ Key = "MCRTX_FLOATING_ORIGIN"; Value = "1"; Remove = $false }
        [pscustomobject]@{ Key = "MCRTX_VERBOSE_INPUT_LOG"; Value = "1"; Remove = -not $ConfiguredVerboseInputLogging }
    )

    foreach ($managedEntry in $managedEntries) {
        $escapedKey = [regex]::Escape($managedEntry.Key)
        $pattern = "^\s*$escapedKey\s*=.*$"
        $matchingIndices = [System.Collections.Generic.List[int]]::new()

        for ($lineIndex = 0; $lineIndex -lt $contentLines.Count; $lineIndex += 1) {
            if ($contentLines[$lineIndex] -match $pattern) {
                $matchingIndices.Add($lineIndex)
            }
        }

        if ($managedEntry.Remove) {
            for ($matchIndex = $matchingIndices.Count - 1; $matchIndex -ge 0; $matchIndex -= 1) {
                $contentLines.RemoveAt($matchingIndices[$matchIndex])
            }
            continue
        }

        $replacement = "$($managedEntry.Key)=$($managedEntry.Value)"
        if ($matchingIndices.Count -gt 0) {
            $contentLines[$matchingIndices[0]] = $replacement
            for ($matchIndex = $matchingIndices.Count - 1; $matchIndex -gt 0; $matchIndex -= 1) {
                $contentLines.RemoveAt($matchingIndices[$matchIndex])
            }
        } else {
            $contentLines.Add($replacement)
        }
    }

    Set-Content -Path $Path -Value $contentLines -Encoding ASCII
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
    Set-RemixConfigValue -Path $ConfigPath -Key "rtx.enableDirectTranslucentShadows" -Value "True"
    Set-RemixConfigValue -Path $ConfigPath -Key "rtx.volumetrics.froxelMaxDistanceMeters" -Value "256"
}

foreach ($requiredPath in @($InstanceRoot, (Split-Path $MinecraftLibraryJar -Parent), $deployScript, $instanceConfigPath, $instanceMmcPackPath, $minecraftMetaPath)) {
    if (-not (Test-Path $requiredPath)) {
        throw "Required path not found: $requiredPath"
    }
}

$removedPreLaunchCommand = $false
if ($PSCmdlet.ShouldProcess($instanceConfigPath, "Remove mc-rtx Prism pre-launch command")) {
    $removedPreLaunchCommand = Remove-McrtxPrismPreLaunchCommand -InstanceConfig $instanceConfigPath
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

    $patchedClientBuildArgs = @{
        PrismRoot = $PrismRoot
        InstanceName = $InstanceName
        MinecraftJar = $MinecraftLibraryJar
        ModdedMinecraftJar = Join-Path $InstanceRoot "libraries\customjar-1.jar"
        Configuration = $Configuration
        OutputRoot = $BundleRoot
    }
    if ($JavaHome) {
        $patchedClientBuildArgs.JavaHome = $JavaHome
    }

    & $deployScript @patchedClientBuildArgs
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

    if (Test-Path $runtimeConfigPath) {
        if ($PSCmdlet.ShouldProcess($runtimeConfigPath, "Remove mcrtx runtime launch config")) {
            Remove-Item $runtimeConfigPath -Force
        }
    }

    foreach ($customJarTarget in $customJarTargets) {
        if ($PSCmdlet.ShouldProcess($customJarTarget, "Remove Prism custom jar override")) {
            Remove-Item $customJarTarget -Force
        }
    }

    foreach ($pathToRemove in @($deployedPdbPath, $legacyInstancePdbPath, $instanceLibraryDllPath, $instanceLibraryPdbPath, $instanceMinecraftDllPath, $instanceMinecraftPdbPath)) {
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
    $platformSpec = Set-PrismPlatformBackend -PackPath $instanceMmcPackPath -MinecraftMetaPath $minecraftMetaPath -PatchPath $instanceMinecraftPatchPath -Backend $PlatformBackend -UseNoApplet:$useNoAppletEnabled
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

if (Test-Path $patchedPdb) {
    if ($PSCmdlet.ShouldProcess($deployedPdbPath, "Deploy mcrtx JNI PDB next to the shared PrismLauncher Beta 1.7.3 jar")) {
        Copy-Item $patchedPdb $deployedPdbPath -Force
    }
} elseif (Test-Path $deployedPdbPath) {
    if ($PSCmdlet.ShouldProcess($deployedPdbPath, "Remove stale shared PrismLauncher mcrtx JNI PDB")) {
        Remove-Item $deployedPdbPath -Force
    }
}

foreach ($dllTarget in @($instanceLibraryDllPath, $instanceMinecraftDllPath)) {
    if ($PSCmdlet.ShouldProcess($dllTarget, "Deploy instance-local mcrtx JNI DLL")) {
        Copy-Item $patchedDll $dllTarget -Force
        $didCopyDll = $true
    }
}

if (Test-Path $patchedPdb) {
    foreach ($pdbTarget in @($instanceLibraryPdbPath, $instanceMinecraftPdbPath)) {
        if ($PSCmdlet.ShouldProcess($pdbTarget, "Deploy instance-local mcrtx JNI PDB")) {
            Copy-Item $patchedPdb $pdbTarget -Force
        }
    }
} else {
    foreach ($pdbTarget in @($instanceLibraryPdbPath, $instanceMinecraftPdbPath)) {
        if (Test-Path $pdbTarget) {
            if ($PSCmdlet.ShouldProcess($pdbTarget, "Remove stale instance-local mcrtx JNI PDB")) {
                Remove-Item $pdbTarget -Force
            }
        }
    }
}

foreach ($assetsTarget in @($sharedAssetsDir, $instanceAssetsDir)) {
    if ($OverwriteAssets -and (Test-Path $assetsTarget)) {
        if ($PSCmdlet.ShouldProcess($assetsTarget, "Clear existing mcrtx atlas assets before redeploy")) {
            Remove-Item $assetsTarget -Recurse -Force
        }
    }

    if ($PSCmdlet.ShouldProcess($assetsTarget, "Sync mcrtx atlas assets")) {
        Sync-DirectoryFiles -SourcePath $bundleAssetsDir -DestinationPath $assetsTarget
    }
}

if (Test-Path $legacyInstanceDllPath) {
    if ($PSCmdlet.ShouldProcess($legacyInstanceDllPath, "Remove stale instance-native mcrtx JNI DLL")) {
        Remove-Item $legacyInstanceDllPath -Force
    }
}

if (Test-Path $legacyInstancePdbPath) {
    if ($PSCmdlet.ShouldProcess($legacyInstancePdbPath, "Remove stale instance-native mcrtx JNI PDB")) {
        Remove-Item $legacyInstancePdbPath -Force
    }
}

$deploymentRecord = [ordered]@{
    deployedAt = (Get-Date).ToString("o")
    prismRoot = $PrismRoot
    instanceName = $InstanceName
    instanceRoot = $InstanceRoot
    minecraftLibraryJar = $MinecraftLibraryJar
    deployedDll = $deployedDllPath
    deployedPdb = $deployedPdbPath
    instanceLibraryDll = $instanceLibraryDllPath
    instanceLibraryPdb = $instanceLibraryPdbPath
    instanceMinecraftDll = $instanceMinecraftDllPath
    instanceMinecraftPdb = $instanceMinecraftPdbPath
    sharedAssetsDir = $sharedAssetsDir
    instanceAssetsDir = $instanceAssetsDir
    customJarTargets = $customJarTargets
    bundleRoot = $BundleRoot
    configuration = $Configuration
    platformBackend = $PlatformBackend
    inputBackend = $InputBackend
    windowMode = $WindowMode
    useNoApplet = $useNoAppletEnabled
    noCullDistance = $NoCullDistance
    verboseInputLogging = $verboseInputLoggingEnabled
    platformComponentUid = if ($platformSpec) { $platformSpec.ComponentUid } else { "" }
    platformComponentVersion = if ($platformSpec) { $platformSpec.Version } else { "" }
    patchedJar = $patchedJar
    patchedDll = $patchedDll
    patchedPdb = if (Test-Path $patchedPdb) { $patchedPdb } else { "" }
    runtimeConfigPath = $runtimeConfigPath
}

if ($PSCmdlet.ShouldProcess($deploymentInfo, "Write deployment marker")) {
    $deploymentRecord | ConvertTo-Json | Set-Content -Path $deploymentInfo -Encoding ASCII
    $didWriteMarker = $true
}

if ($PSCmdlet.ShouldProcess($instanceConfigPath, "Configure Prism runtime environment")) {
    Set-PrismRuntimeEnvironment -InstanceConfig $instanceConfigPath -Mode $WindowMode -ConfiguredPlatformBackend $PlatformBackend -ConfiguredInputBackend $InputBackend -ConfiguredNoCullDistance $NoCullDistance -ConfiguredVerboseInputLogging:$verboseInputLoggingEnabled
}

if ($PSCmdlet.ShouldProcess($runtimeConfigPath, "Write mcrtx runtime launch config")) {
    Set-McrtxRuntimeConfig -Path $runtimeConfigPath -Mode $WindowMode -ConfiguredPlatformBackend $PlatformBackend -ConfiguredInputBackend $InputBackend -ConfiguredNoCullDistance $NoCullDistance -ConfiguredVerboseInputLogging:$verboseInputLoggingEnabled
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
    if (Test-Path $patchedPdb) {
        Write-Host "Deployed mcrtx_jni.pdb to $deployedPdbPath"
        Write-Host "Deployed instance-local mcrtx_jni.pdb to $instanceLibraryPdbPath"
    }
}

Write-Host "Deployed terrain atlas assets to $sharedAssetsDir"
Write-Host "Deployed terrain atlas assets to $instanceAssetsDir"

if ($didWriteMarker) {
    Write-Host "Updated deployment marker at $deploymentInfo"
}

if ($removedPreLaunchCommand) {
    Write-Host "Removed mc-rtx Prism pre-launch command from $instanceConfigPath"
}

Write-Host "Configured Prism runtime environment windowMode='$WindowMode' platformBackend='$PlatformBackend' inputBackend='$InputBackend' in $instanceConfigPath"
Write-Host "Configured Prism noapplet trait: $useNoAppletEnabled in $instanceMinecraftPatchPath"
Write-Host "Configured verbose input logging: $verboseInputLoggingEnabled"
Write-Host "Wrote mcrtx runtime launch config to $runtimeConfigPath"

if ($platformSpec) {
    Write-Host "Configured Prism component graph for $($platformSpec.CachedName) $($platformSpec.Version) in $instanceMmcPackPath"
    Write-Host "Updated local Minecraft patch at $instanceMinecraftPatchPath to require $($platformSpec.ComponentUid) $($platformSpec.Version)"
}

Write-Host "Configured Remix viewmodel settings in $instanceRemixConfigPath"

Write-Host "Launch the b1.7.3 PrismLauncher instance to test the current build"
