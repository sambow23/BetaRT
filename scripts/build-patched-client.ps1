[CmdletBinding()]
param(
    [string]$MinecraftJar = "C:\Users\cr\AppData\Roaming\PrismLauncher\libraries\com\mojang\minecraft\b1.7.3\minecraft-b1.7.3-client.jar",
    [string]$LwjglJar = "C:\Users\cr\AppData\Roaming\PrismLauncher\libraries\org\lwjgl\lwjgl\lwjgl\2.9.4-nightly-20150209\lwjgl-2.9.4-nightly-20150209.jar",
    [string]$LwjglUtilJar = "C:\Users\cr\AppData\Roaming\PrismLauncher\libraries\org\lwjgl\lwjgl\lwjgl_util\2.9.4-nightly-20150209\lwjgl_util-2.9.4-nightly-20150209.jar",
    [string]$AsmJar = "C:\Users\cr\AppData\Roaming\PrismLauncher\libraries\org\ow2\asm\asm\9.9\asm-9.9.jar",
    [string]$AsmTreeJar = "C:\Users\cr\AppData\Roaming\PrismLauncher\libraries\org\ow2\asm\asm-tree\9.9\asm-tree-9.9.jar",
    [string]$JavacPath = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\javac.exe",
    [string]$JavaPath = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\java.exe",
    [string]$JarPath = "C:\Program Files\Eclipse Adoptium\jdk-21.0.7.6-hotspot\bin\jar.exe",
    [string]$Configuration = "Release",
    [string]$OutputRoot
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
if (-not $OutputRoot) {
    $OutputRoot = Join-Path $repoRoot "out\patched-client"
}

$classesDir = Join-Path $OutputRoot "classes"
$toolClassesDir = Join-Path $OutputRoot "tool-classes"
$patchedJarTemp = Join-Path $OutputRoot "minecraft-b1.7.3-client-mcrtx.base.jar"
$patchedJar = Join-Path $OutputRoot "minecraft-b1.7.3-client-mcrtx.jar"
$nativeDll = Join-Path $repoRoot "build\native\$Configuration\mcrtx_jni.dll"

$runtimeSourceFiles = @(
    (Join-Path $repoRoot "java-src\MinecraftRemixHooks.java"),
    (Join-Path $repoRoot "java-src\mcrtx\bridge\CameraPose.java"),
    (Join-Path $repoRoot "java-src\mcrtx\bridge\RemixBridgeNative.java"),
    (Join-Path $repoRoot "java-src\mcrtx\bridge\LwjglWindowHandleResolver.java"),
    (Join-Path $repoRoot "java-src\mcrtx\bridge\MinecraftRenderHooks.java")
)

$toolSourceFiles = @(
    (Join-Path $repoRoot "tools-src\mcrtx\tools\ClientPatchTool.java")
)

$requiredPaths = @($MinecraftJar, $LwjglJar, $LwjglUtilJar, $AsmJar, $AsmTreeJar, $JavacPath, $JavaPath, $JarPath) + $runtimeSourceFiles + $toolSourceFiles
foreach ($path in $requiredPaths) {
    if (-not (Test-Path $path)) {
        throw "Required path not found: $path"
    }
}

New-Item -ItemType Directory -Force -Path $OutputRoot | Out-Null
foreach ($dir in @($classesDir, $toolClassesDir)) {
    if (Test-Path $dir) {
        Remove-Item -Recurse -Force $dir
    }
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
}

$classpath = @($MinecraftJar, $LwjglJar, $LwjglUtilJar) -join ';'
$toolClasspath = @($AsmJar, $AsmTreeJar) -join ';'

& $JavacPath --release 8 -Xlint:-options -cp $classpath -d $classesDir $runtimeSourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "javac runtime compile failed with exit code $LASTEXITCODE"
}

& $JavacPath -cp $toolClasspath -d $toolClassesDir $toolSourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "javac tool compile failed with exit code $LASTEXITCODE"
}

& $JavaPath -cp ($toolClassesDir + ';' + $toolClasspath) mcrtx.tools.ClientPatchTool $MinecraftJar $patchedJarTemp
if ($LASTEXITCODE -ne 0) {
    throw "bytecode patch tool failed with exit code $LASTEXITCODE"
}

Move-Item -Force $patchedJarTemp $patchedJar
& $JarPath uf $patchedJar -C $classesDir .
if ($LASTEXITCODE -ne 0) {
    throw "jar update failed with exit code $LASTEXITCODE"
}

if (-not (Test-Path $nativeDll)) {
    throw "Native DLL not found at $nativeDll. Build it first with: cmake --build build --config $Configuration --target mcrtx_jni"
}

Copy-Item $nativeDll (Join-Path $OutputRoot "mcrtx_jni.dll") -Force

Write-Host "Patched client bundle ready: $OutputRoot"
Write-Host "Patched jar: $patchedJar"