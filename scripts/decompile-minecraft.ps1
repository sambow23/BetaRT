param(
    [string]$MinecraftJar = 'C:/Users/cr/AppData/Roaming/PrismLauncher/libraries/com/mojang/minecraft/b1.7.3/minecraft-b1.7.3-client.jar',
    [string]$CfrJar = 'c:/Users/cr/proj/mc-rtx/cfr-0.152.jar',
    [string]$VineflowerJar = 'c:/Users/cr/proj/mc-rtx/vineflower-1.11.2.jar'
)

$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$workRoot = Join-Path $repoRoot 'work'
$jarContents = Join-Path $workRoot 'jar-contents'
$cfrOut = Join-Path $workRoot 'decompiled-cfr'
$vineflowerOut = Join-Path $workRoot 'decompiled-vineflower'
$reports = Join-Path $workRoot 'reports'

foreach ($path in @($workRoot, $jarContents, $cfrOut, $vineflowerOut, $reports)) {
    New-Item -ItemType Directory -Force -Path $path | Out-Null
}

Get-ChildItem $jarContents -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
Get-ChildItem $cfrOut -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
Get-ChildItem $vineflowerOut -Force -ErrorAction SilentlyContinue | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue

New-Item -ItemType Directory -Force -Path $jarContents | Out-Null
New-Item -ItemType Directory -Force -Path $cfrOut | Out-Null
New-Item -ItemType Directory -Force -Path $vineflowerOut | Out-Null

Push-Location $jarContents
try {
    & jar xf $MinecraftJar
}
finally {
    Pop-Location
}

& java -jar $CfrJar $MinecraftJar --outputdir $cfrOut 2>&1 | Tee-Object -FilePath (Join-Path $reports 'cfr-decompile.log')

& java -jar $VineflowerJar -din=1 $MinecraftJar $vineflowerOut 2>&1 | Tee-Object -FilePath (Join-Path $reports 'vineflower-decompile.log')

Write-Host 'Decompilation complete.'
