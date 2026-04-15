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
        if ($null -ne $bitmap) {
            $bitmap.Dispose()
        }
        if ($null -ne $convertedBitmap -and $null -ne $bitmap -and $convertedBitmap -ne $bitmap) {
            $convertedBitmap.Dispose()
        }
    }
}

function Replace-TerrainFireTiles {
    param(
        [string]$TerrainPngPath,
        [string]$FirePngPath
    )

    $terrainSource = [System.Drawing.Image]::FromFile($TerrainPngPath)
    $terrainBitmap = $null
    $fireAtlasBitmap = $null
    $graphics = $null
    $fireGraphics = $null

    try {
        $terrainBitmap = New-Object System.Drawing.Bitmap($terrainSource)
        $fireAtlasBitmap = New-Object System.Drawing.Bitmap(256, 32, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $graphics = [System.Drawing.Graphics]::FromImage($terrainBitmap)
        $fireGraphics = [System.Drawing.Graphics]::FromImage($fireAtlasBitmap)

        function New-FireSimulation {
            param(
                [int]$SeedOffset
            )

            return @{
                TileWidth = 16
                SimulationHeight = 20
                Current = (New-Object 'float[]' (16 * 20))
                Next = (New-Object 'float[]' (16 * 20))
                Random = [System.Random]::new(1337 + $SeedOffset)
            }
        }

        function Step-FireSimulation {
            param(
                [hashtable]$Simulation
            )

            $tileWidth = $Simulation.TileWidth
            $simulationHeight = $Simulation.SimulationHeight
            $current = $Simulation.Current
            $next = $Simulation.Next
            $random = $Simulation.Random

            for ($x = 0; $x -lt $tileWidth; $x++) {
                for ($y = 0; $y -lt $simulationHeight; $y++) {
                        $weight = 18
                        $value = $current[$x + ((($y + 1) % $simulationHeight) * $tileWidth)] * $weight

                        for ($sampleX = $x - 1; $sampleX -le $x + 1; $sampleX++) {
                            for ($sampleY = $y; $sampleY -le $y + 1; $sampleY++) {
                                if ($sampleX -ge 0 -and $sampleY -ge 0 -and $sampleX -lt $tileWidth -and $sampleY -lt $simulationHeight) {
                                    $value += $current[$sampleX + ($sampleY * $tileWidth)]
                                }
                                $weight++
                            }
                        }

                        $nextIndex = $x + ($y * $tileWidth)
                    $next[$nextIndex] = $value / ($weight * 1.06)
                    if ($y -eq ($simulationHeight - 1)) {
                        $randomValue = $random.NextDouble()
                        $randomValue *= $random.NextDouble()
                        $randomValue *= $random.NextDouble()
                        $next[$nextIndex] = [float]($randomValue * 4.0 + ($random.NextDouble() * 0.1) + 0.2)
                    }
                }
            }

            $Simulation.Current = $next
            $Simulation.Next = $current
        }

        function New-FireTileBitmap {
            param(
                [hashtable]$Simulation
            )

            $tileWidth = $Simulation.TileWidth
            $current = $Simulation.Current
            $bitmap = New-Object System.Drawing.Bitmap($tileWidth, $tileWidth, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)

            for ($pixelIndex = 0; $pixelIndex -lt 256; $pixelIndex++) {
                $intensity = $current[$pixelIndex] * 1.8
                if ($intensity -gt 1.0) {
                    $intensity = 1.0
                }
                if ($intensity -lt 0.0) {
                    $intensity = 0.0
                }

                $red = [int]($intensity * 155.0 + 100.0)
                $green = [int]($intensity * $intensity * 255.0)
                $blue = [int]([Math]::Pow($intensity, 10.0) * 255.0)
                $alpha = if ($intensity -lt 0.5) { 0 } else { 255 }

                $pixelX = $pixelIndex % $tileWidth
                $pixelY = [int][Math]::Floor($pixelIndex / $tileWidth)
                $bitmap.SetPixel($pixelX, $pixelY, [System.Drawing.Color]::FromArgb($alpha, $red, $green, $blue))
            }

            return $bitmap
        }

        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
        $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
        $fireGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $fireGraphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
        $fireGraphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy

        $tileSize = 16
        $fireTerrainTileIndices = @(31, 47)
        $frameCount = 16
        $primarySimulation = New-FireSimulation -SeedOffset 0
        $alternateSimulation = New-FireSimulation -SeedOffset 1

        for ($warmupStep = 0; $warmupStep -lt 32; $warmupStep++) {
            Step-FireSimulation -Simulation $primarySimulation
            Step-FireSimulation -Simulation $alternateSimulation
        }

        for ($frameIndex = 0; $frameIndex -lt $frameCount; $frameIndex++) {
            $primaryTileBitmap = New-FireTileBitmap -Simulation $primarySimulation
            $alternateTileBitmap = New-FireTileBitmap -Simulation $alternateSimulation
            try {
                $frameDestinationX = $frameIndex * $tileSize
                $primaryAtlasRect = New-Object System.Drawing.Rectangle($frameDestinationX, 0, $tileSize, $tileSize)
                $alternateAtlasRect = New-Object System.Drawing.Rectangle($frameDestinationX, $tileSize, $tileSize, $tileSize)
                $fireGraphics.DrawImage($primaryTileBitmap, $primaryAtlasRect)
                $fireGraphics.DrawImage($alternateTileBitmap, $alternateAtlasRect)

                if ($frameIndex -eq 0) {
                    for ($tileArrayIndex = 0; $tileArrayIndex -lt $fireTerrainTileIndices.Count; $tileArrayIndex++) {
                        $tileIndex = $fireTerrainTileIndices[$tileArrayIndex]
                        $destinationTileX = ($tileIndex % 16) * $tileSize
                        $destinationTileY = [int][Math]::Floor($tileIndex / 16) * $tileSize
                        $destinationRect = New-Object System.Drawing.Rectangle($destinationTileX, $destinationTileY, $tileSize, $tileSize)
                        $sourceBitmap = if ($tileArrayIndex -eq 0) { $primaryTileBitmap } else { $alternateTileBitmap }
                        $graphics.DrawImage($sourceBitmap, $destinationRect)
                    }
                }
            } finally {
                $primaryTileBitmap.Dispose()
                $alternateTileBitmap.Dispose()
            }

            Step-FireSimulation -Simulation $primarySimulation
            Step-FireSimulation -Simulation $alternateSimulation
        }

        $fireGraphics.Dispose()
        $fireGraphics = $null
        $graphics.Dispose()
        $graphics = $null

        $temporaryTerrainPath = "$TerrainPngPath.tmp"
        $temporaryFirePath = "$FirePngPath.tmp"
        $terrainBitmap.Save($temporaryTerrainPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $fireAtlasBitmap.Save($temporaryFirePath, [System.Drawing.Imaging.ImageFormat]::Png)

        $fireAtlasBitmap.Dispose()
        $fireAtlasBitmap = $null
        $terrainBitmap.Dispose()
        $terrainBitmap = $null
        $terrainSource.Dispose()
        $terrainSource = $null

        Copy-Item -Force $temporaryTerrainPath $TerrainPngPath
        Remove-Item -Force $temporaryTerrainPath
        Copy-Item -Force $temporaryFirePath $FirePngPath
        Remove-Item -Force $temporaryFirePath
    } finally {
        if ($null -ne $fireGraphics) {
            $fireGraphics.Dispose()
        }
        if ($null -ne $graphics) {
            $graphics.Dispose()
        }
        if ($null -ne $fireAtlasBitmap) {
            $fireAtlasBitmap.Dispose()
        }
        if ($null -ne $terrainBitmap) {
            $terrainBitmap.Dispose()
        }
        if ($null -ne $terrainSource) {
            $terrainSource.Dispose()
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

Export-ZipEntryFile -ArchivePath $MinecraftJar -EntryName "particles.png" -DestinationPath (Join-Path $assetsDir "particles.png")
Export-ZipEntryFile -ArchivePath $MinecraftJar -EntryName "terrain.png" -DestinationPath (Join-Path $assetsDir "terrain.png")
Replace-TerrainFireTiles -TerrainPngPath (Join-Path $assetsDir "terrain.png") -FirePngPath (Join-Path $assetsDir "fire.png")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "terrain.png") -DestinationDdsPath (Join-Path $assetsDir "terrain.dds")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "fire.png") -DestinationDdsPath (Join-Path $assetsDir "fire.dds")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "particles.png") -DestinationDdsPath (Join-Path $assetsDir "particles.dds")
Export-ZipEntryFile -ArchivePath $MinecraftJar -EntryName "gui/items.png" -DestinationPath (Join-Path $assetsDir "gui\items.png")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "gui\items.png") -DestinationDdsPath (Join-Path $assetsDir "gui\items.dds")
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
Write-Host "Replaced terrain fire placeholder tiles with generated Beta fire texels"
Write-Host "Converted terrain atlas DDS: $(Join-Path $assetsDir 'terrain.dds')"
Write-Host "Generated fire animation atlas: $(Join-Path $assetsDir 'fire.png')"
Write-Host "Converted fire animation atlas DDS: $(Join-Path $assetsDir 'fire.dds')"
Write-Host "Extracted particles atlas: $(Join-Path $assetsDir 'particles.png')"
Write-Host "Converted particles atlas DDS: $(Join-Path $assetsDir 'particles.dds')"
Write-Host "Extracted GUI item atlas: $(Join-Path $assetsDir 'gui\items.png')"
Write-Host "Converted GUI item atlas DDS: $(Join-Path $assetsDir 'gui\items.dds')"
Write-Host "Extracted cloud texture: $(Join-Path $assetsDir 'clouds.png')"
Write-Host "Converted cloud texture DDS: $(Join-Path $assetsDir 'clouds.dds')"
Write-Host "Extracted entity texture directories under: $(Join-Path $assetsDir 'entities')"