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

Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.Drawing

function Convert-PngToDds {
    param(
        [string]$SourcePngPath,
        [string]$DestinationDdsPath
    )

    $bitmap = New-Object System.Drawing.Bitmap($SourcePngPath)
    $convertedBitmap = $null

    try {
        if ($bitmap.PixelFormat -ne [System.Drawing.Imaging.PixelFormat]::Format32bppArgb) {
            $convertedBitmap = New-Object System.Drawing.Bitmap($bitmap.Width, $bitmap.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
            $graphics = [System.Drawing.Graphics]::FromImage($convertedBitmap)
            try {
                $graphics.DrawImage($bitmap, 0, 0, $bitmap.Width, $bitmap.Height)
            } finally {
                $graphics.Dispose()
            }
        } else {
            $convertedBitmap = $bitmap
            $bitmap = $null
        }

        $rect = New-Object System.Drawing.Rectangle(0, 0, $convertedBitmap.Width, $convertedBitmap.Height)
        $bitmapData = $convertedBitmap.LockBits(
            $rect,
            [System.Drawing.Imaging.ImageLockMode]::ReadOnly,
            [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)

        try {
            $rowByteCount = $convertedBitmap.Width * 4
            $pixelBytes = New-Object byte[] ($convertedBitmap.Width * $convertedBitmap.Height * 4)
            $rowBuffer = New-Object byte[] $rowByteCount

            for ($y = 0; $y -lt $convertedBitmap.Height; $y++) {
                $sourceRowIndex = if ($bitmapData.Stride -lt 0) { $convertedBitmap.Height - 1 - $y } else { $y }
                $rowPointer = [System.IntPtr]::new($bitmapData.Scan0.ToInt64() + ($sourceRowIndex * $bitmapData.Stride))
                [System.Runtime.InteropServices.Marshal]::Copy($rowPointer, $rowBuffer, 0, $rowByteCount)
                [System.Array]::Copy($rowBuffer, 0, $pixelBytes, $y * $rowByteCount, $rowByteCount)
            }
        } finally {
            $convertedBitmap.UnlockBits($bitmapData)
        }

        $destinationDirectory = Split-Path $DestinationDdsPath -Parent
        New-Item -ItemType Directory -Force -Path $destinationDirectory | Out-Null

        $fileStream = [System.IO.File]::Open($DestinationDdsPath, [System.IO.FileMode]::Create, [System.IO.FileAccess]::Write, [System.IO.FileShare]::None)
        $writer = New-Object System.IO.BinaryWriter($fileStream)
        try {
            $writer.Write([byte[]][char[]]"DDS ")
            $writer.Write([uint32]124)
            $writer.Write([uint32]0x100F)
            $writer.Write([uint32]$convertedBitmap.Height)
            $writer.Write([uint32]$convertedBitmap.Width)
            $writer.Write([uint32]($convertedBitmap.Width * 4))
            $writer.Write([uint32]0)
            $writer.Write([uint32]0)
            for ($index = 0; $index -lt 11; $index++) {
                $writer.Write([uint32]0)
            }

            $writer.Write([uint32]32)
            $writer.Write([uint32]0x41)
            $writer.Write([uint32]0)
            $writer.Write([uint32]32)
            $writer.Write([uint32]0x00FF0000)
            $writer.Write([uint32]0x0000FF00)
            $writer.Write([uint32]0x000000FF)
            $writer.Write([uint32]4278190080)

            $writer.Write([uint32]0x1000)
            $writer.Write([uint32]0)
            $writer.Write([uint32]0)
            $writer.Write([uint32]0)
            $writer.Write([uint32]0)

            $writer.Write($pixelBytes)
        } finally {
            $writer.Dispose()
            $fileStream.Dispose()
        }
    } finally {
        if ($bitmap -ne $null) {
            $bitmap.Dispose()
        }
        if ($convertedBitmap -ne $null -and $convertedBitmap -ne $bitmap) {
            $convertedBitmap.Dispose()
        }
    }
}

function Export-ZipEntryFile {
    param(
        [string]$ArchivePath,
        [string]$EntryName,
        [string]$DestinationPath
    )

    $archive = [System.IO.Compression.ZipFile]::OpenRead($ArchivePath)
    try {
        $entry = $archive.GetEntry($EntryName)
        if ($null -eq $entry) {
            throw "Archive entry not found: $EntryName"
        }

        $destinationDirectory = Split-Path $DestinationPath -Parent
        New-Item -ItemType Directory -Force -Path $destinationDirectory | Out-Null

        $entryStream = $entry.Open()
        try {
            $outputStream = [System.IO.File]::Open($DestinationPath, [System.IO.FileMode]::Create, [System.IO.FileAccess]::Write, [System.IO.FileShare]::None)
            try {
                $entryStream.CopyTo($outputStream)
            } finally {
                $outputStream.Dispose()
            }
        } finally {
            $entryStream.Dispose()
        }
    } finally {
        $archive.Dispose()
    }
}

function Export-ZipEntriesByPrefix {
    param(
        [string]$ArchivePath,
        [string[]]$Prefixes,
        [string]$DestinationRoot,
        [switch]$ConvertToDds
    )

    $archive = [System.IO.Compression.ZipFile]::OpenRead($ArchivePath)
    try {
        foreach ($entry in $archive.Entries) {
            if ($entry.FullName.EndsWith('/')) {
                continue
            }

            $matchesPrefix = $false
            foreach ($prefix in $Prefixes) {
                if ($entry.FullName.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) {
                    $matchesPrefix = $true
                    break
                }
            }

            if (-not $matchesPrefix) {
                continue
            }

            $destinationPath = Join-Path $DestinationRoot ($entry.FullName -replace '/', '\\')
            $destinationDirectory = Split-Path $destinationPath -Parent
            New-Item -ItemType Directory -Force -Path $destinationDirectory | Out-Null

            $entryStream = $entry.Open()
            try {
                $outputStream = [System.IO.File]::Open($destinationPath, [System.IO.FileMode]::Create, [System.IO.FileAccess]::Write, [System.IO.FileShare]::None)
                try {
                    $entryStream.CopyTo($outputStream)
                } finally {
                    $outputStream.Dispose()
                }
            } finally {
                $entryStream.Dispose()
            }

            if ($ConvertToDds -and $destinationPath.EndsWith('.png', [System.StringComparison]::OrdinalIgnoreCase)) {
                Convert-PngToDds -SourcePngPath $destinationPath -DestinationDdsPath ([System.IO.Path]::ChangeExtension($destinationPath, '.dds'))
            }
        }
    } finally {
        $archive.Dispose()
    }
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$backupMinecraftJar = Join-Path $repoRoot "out\deploy-state\minecraft-b1.7.3-client.original.jar"

if (Test-Path $backupMinecraftJar) {
    $MinecraftJar = $backupMinecraftJar
}

if (-not $OutputRoot) {
    $OutputRoot = Join-Path $repoRoot "out\patched-client"
}

$classesDir = Join-Path $OutputRoot "classes"
$toolClassesDir = Join-Path $OutputRoot "tool-classes"
$assetsDir = Join-Path $OutputRoot "mcrtx_assets"
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
foreach ($dir in @($classesDir, $toolClassesDir, $assetsDir)) {
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

Export-ZipEntryFile -ArchivePath $MinecraftJar -EntryName "terrain.png" -DestinationPath (Join-Path $assetsDir "terrain.png")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "terrain.png") -DestinationDdsPath (Join-Path $assetsDir "terrain.dds")
Export-ZipEntryFile -ArchivePath $MinecraftJar -EntryName "environment/clouds.png" -DestinationPath (Join-Path $assetsDir "clouds.png")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "clouds.png") -DestinationDdsPath (Join-Path $assetsDir "clouds.dds")
Export-ZipEntriesByPrefix -ArchivePath $MinecraftJar -Prefixes @('mob/', 'armor/') -DestinationRoot (Join-Path $assetsDir 'entities') -ConvertToDds

if (-not (Test-Path $nativeDll)) {
    throw "Native DLL not found at $nativeDll. Build it first with: cmake --build build --config $Configuration --target mcrtx_jni"
}

Copy-Item $nativeDll (Join-Path $OutputRoot "mcrtx_jni.dll") -Force

Write-Host "Patched client bundle ready: $OutputRoot"
Write-Host "Patched jar: $patchedJar"
Write-Host "Patch source jar: $MinecraftJar"
Write-Host "Extracted terrain atlas: $(Join-Path $assetsDir 'terrain.png')"
Write-Host "Converted terrain atlas DDS: $(Join-Path $assetsDir 'terrain.dds')"
Write-Host "Extracted cloud texture: $(Join-Path $assetsDir 'clouds.png')"
Write-Host "Converted cloud texture DDS: $(Join-Path $assetsDir 'clouds.dds')"
Write-Host "Extracted entity texture directories under: $(Join-Path $assetsDir 'entities')"