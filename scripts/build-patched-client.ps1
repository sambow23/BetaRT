[CmdletBinding()]
param(
    [string]$PrismRoot,
    [string]$InstanceName = "b1.7.3",
    [string]$MinecraftJar,
    [string]$PatchSourceJar,
    [string]$ModdedMinecraftJar,
    [switch]$DisableModdedPatchSource,
    [string]$LwjglJar,
    [string]$LwjglUtilJar,
    [string]$AsmJar,
    [string]$AsmTreeJar,
    [string]$JavaHome,
    [string]$JavacPath,
    [string]$JavaPath,
    [string]$JarPath,
    [string]$Configuration = "Release",
    [string]$OutputRoot
)

$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.IO.Compression.FileSystem
Add-Type -AssemblyName System.Drawing

function Resolve-JavaToolchain {
    param(
        [string]$RequestedJavaHome,
        [string]$RequestedJavacPath,
        [string]$RequestedJavaPath,
        [string]$RequestedJarPath
    )

    $resolvedJavacPath = $RequestedJavacPath
    $resolvedJavaPath = $RequestedJavaPath
    $resolvedJarPath = $RequestedJarPath
    $candidateHomes = [System.Collections.Generic.List[string]]::new()

    if ($RequestedJavaHome) {
        $candidateHomes.Add($RequestedJavaHome)
    } elseif ($resolvedJavacPath -or $resolvedJavaPath -or $resolvedJarPath) {
        $seedPath = @($resolvedJavacPath, $resolvedJavaPath, $resolvedJarPath) |
            Where-Object { $_ } |
            Select-Object -First 1
        $candidateHomes.Add((Split-Path (Split-Path $seedPath -Parent) -Parent))
    } else {
        if ($env:JAVA_HOME) {
            $candidateHomes.Add($env:JAVA_HOME)
        }

        Get-Command javac.exe -All -ErrorAction SilentlyContinue |
            ForEach-Object {
                $candidateHomes.Add((Split-Path (Split-Path $_.Source -Parent) -Parent))
            }
    }

    foreach ($candidateHome in $candidateHomes) {
        $candidateBin = Join-Path $candidateHome "bin"
        $candidateJavac = if ($resolvedJavacPath) { $resolvedJavacPath } else { Join-Path $candidateBin "javac.exe" }
        $candidateJava = if ($resolvedJavaPath) { $resolvedJavaPath } else { Join-Path $candidateBin "java.exe" }
        $candidateJar = if ($resolvedJarPath) { $resolvedJarPath } else { Join-Path $candidateBin "jar.exe" }

        if ((Test-Path $candidateJavac -PathType Leaf) -and
            (Test-Path $candidateJava -PathType Leaf) -and
            (Test-Path $candidateJar -PathType Leaf)) {
            $resolvedJavacPath = (Resolve-Path $candidateJavac).Path
            $resolvedJavaPath = (Resolve-Path $candidateJava).Path
            $resolvedJarPath = (Resolve-Path $candidateJar).Path
            break
        }
    }

    if (-not $resolvedJavacPath -or -not (Test-Path $resolvedJavacPath -PathType Leaf) -or
        -not $resolvedJavaPath -or -not (Test-Path $resolvedJavaPath -PathType Leaf) -or
        -not $resolvedJarPath -or -not (Test-Path $resolvedJarPath -PathType Leaf)) {
        throw "A complete JDK was not found. Pass -JavaHome, set JAVA_HOME, or add a JDK bin directory containing java.exe, javac.exe, and jar.exe to PATH."
    }

    $javacVersionText = (& $resolvedJavacPath -version 2>&1 | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or $javacVersionText -notmatch 'javac\s+(?:1\.)?(\d+)') {
        throw "Unable to determine javac version from '$resolvedJavacPath': $javacVersionText"
    }

    $javacMajorVersion = [int]$Matches[1]
    if ($javacMajorVersion -lt 9) {
        throw "JDK 9 or newer is required because the patched client is compiled with javac --release 8. Found: $javacVersionText"
    }

    return [pscustomobject]@{
        JavaHome = Split-Path (Split-Path $resolvedJavacPath -Parent) -Parent
        JavacPath = $resolvedJavacPath
        JavaPath = $resolvedJavaPath
        JarPath = $resolvedJarPath
        Version = $javacVersionText
    }
}

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

function Replace-TerrainLiquidTiles {
    param(
        [string]$TerrainPngPath,
        [string]$WaterPngPath,
        [string]$WaterNormalPngPath,
        [string]$LavaPngPath,
        [string]$LavaEmissivePngPath
    )

    $terrainSource = [System.Drawing.Image]::FromFile($TerrainPngPath)
    $terrainBitmap = $null
    $waterAtlasBitmap = $null
    $waterNormalAtlasBitmap = $null
    $lavaAtlasBitmap = $null
    $lavaEmissiveAtlasBitmap = $null
    $waterFrames = @()
    $lavaFrames = @()
    $periodicWaterFrames = @()
    $periodicLavaFrames = @()
    $graphics = $null
    $waterGraphics = $null
    $waterNormalGraphics = $null
    $lavaGraphics = $null
    $lavaEmissiveGraphics = $null

    try {
        $terrainBitmap = New-Object System.Drawing.Bitmap($terrainSource)
        $frameCount = 32
        $frameWidth = 32
        $tileSize = 16
        $atlasWidth = $frameCount * $frameWidth
        $waterAtlasBitmap = New-Object System.Drawing.Bitmap -ArgumentList $atlasWidth, $tileSize, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $waterNormalAtlasBitmap = New-Object System.Drawing.Bitmap -ArgumentList $atlasWidth, $tileSize, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $lavaAtlasBitmap = New-Object System.Drawing.Bitmap -ArgumentList $atlasWidth, $tileSize, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $lavaEmissiveAtlasBitmap = New-Object System.Drawing.Bitmap -ArgumentList $atlasWidth, $tileSize, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $graphics = [System.Drawing.Graphics]::FromImage($terrainBitmap)
        $waterGraphics = [System.Drawing.Graphics]::FromImage($waterAtlasBitmap)
        $waterNormalGraphics = [System.Drawing.Graphics]::FromImage($waterNormalAtlasBitmap)
        $lavaGraphics = [System.Drawing.Graphics]::FromImage($lavaAtlasBitmap)
        $lavaEmissiveGraphics = [System.Drawing.Graphics]::FromImage($lavaEmissiveAtlasBitmap)

        function Set-NearestNeighborGraphics {
            param($TargetGraphics)

            $TargetGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
            $TargetGraphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
            $TargetGraphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
        }

        function New-WaterSimulation {
            param(
                [bool]$Flowing,
                [int]$Seed
            )

            return @{
                Current = (New-Object 'float[]' 256)
                Next = (New-Object 'float[]' 256)
                Accumulated = (New-Object 'float[]' 256)
                Impulse = (New-Object 'float[]' 256)
                Flowing = $Flowing
                Offset = 0
                Random = [System.Random]::new($Seed)
            }
        }

        function Step-WaterSimulation {
            param([hashtable]$Simulation)

            $current = $Simulation.Current
            $next = $Simulation.Next
            $accumulated = $Simulation.Accumulated
            $impulse = $Simulation.Impulse
            $random = $Simulation.Random

            if ($Simulation.Flowing) {
                $Simulation.Offset++
            }

            for ($x = 0; $x -lt 16; $x++) {
                for ($y = 0; $y -lt 16; $y++) {
                    $sum = 0.0
                    if ($Simulation.Flowing) {
                        for ($sampleY = $y - 2; $sampleY -le $y; $sampleY++) {
                            $wrappedX = $x -band 0xF
                            $wrappedY = $sampleY -band 0xF
                            $sum += $current[$wrappedX + ($wrappedY * 16)]
                        }
                        $next[$x + ($y * 16)] = $sum / 3.2 + $accumulated[$x + ($y * 16)] * 0.8
                    } else {
                        for ($sampleX = $x - 1; $sampleX -le $x + 1; $sampleX++) {
                            $wrappedX = $sampleX -band 0xF
                            $wrappedY = $y -band 0xF
                            $sum += $current[$wrappedX + ($wrappedY * 16)]
                        }
                        $next[$x + ($y * 16)] = $sum / 3.3 + $accumulated[$x + ($y * 16)] * 0.8
                    }
                }
            }

            for ($x = 0; $x -lt 16; $x++) {
                for ($y = 0; $y -lt 16; $y++) {
                    $index = $x + ($y * 16)
                    $accumulated[$index] += $impulse[$index] * 0.05
                    if ($accumulated[$index] -lt 0.0) {
                        $accumulated[$index] = 0.0
                    }

                    $impulseDecay = 0.1
                    $randomImpulseChance = 0.05
                    if ($Simulation.Flowing) {
                        $impulseDecay = 0.3
                        $randomImpulseChance = 0.2
                    }

                    $impulse[$index] -= $impulseDecay
                    if ($random.NextDouble() -lt $randomImpulseChance) {
                        $impulse[$index] = 0.5
                    }
                }
            }

            $Simulation.Next = $current
            $Simulation.Current = $next
        }

        function New-WaterTileBitmap {
            param([hashtable]$Simulation)

            $bitmap = New-Object System.Drawing.Bitmap(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
            $rowOffset = 0

            for ($pixelIndex = 0; $pixelIndex -lt 256; $pixelIndex++) {
                $sampleIndex = ($pixelIndex - $rowOffset) -band 0xFF
                $intensity = $Simulation.Current[$sampleIndex]
                if ($intensity -gt 1.0) {
                    $intensity = 1.0
                }
                if ($intensity -lt 0.0) {
                    $intensity = 0.0
                }

                $squared = $intensity * $intensity
                $red = [int](32.0 + $squared * 32.0)
                $green = [int](50.0 + $squared * 64.0)
                $blue = 255
                $alpha = [int](146.0 + $squared * 50.0)

                $pixelX = $pixelIndex % 16
                $pixelY = [int][Math]::Floor($pixelIndex / 16)
                $bitmap.SetPixel($pixelX, $pixelY, [System.Drawing.Color]::FromArgb($alpha, $red, $green, $blue))
            }

            return $bitmap
        }

        function Get-NormalMapHeight {
            param([System.Drawing.Color]$Color)

            return (($Color.R * 0.299) + ($Color.G * 0.587) + ($Color.B * 0.114)) / 255.0
        }

        function New-NormalMapBitmap {
            param(
                [System.Drawing.Bitmap]$SourceBitmap,
                [double]$Strength = 2.0,
                [bool]$InvertY = $false
            )

            $width = $SourceBitmap.Width
            $height = $SourceBitmap.Height
            $bitmap = New-Object System.Drawing.Bitmap($width, $height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)

            for ($pixelY = 0; $pixelY -lt $height; $pixelY++) {
                $upY = (($pixelY - 1) + $height) % $height
                $downY = ($pixelY + 1) % $height

                for ($pixelX = 0; $pixelX -lt $width; $pixelX++) {
                    $leftX = (($pixelX - 1) + $width) % $width
                    $rightX = ($pixelX + 1) % $width

                    $centerColor = $SourceBitmap.GetPixel($pixelX, $pixelY)
                    $leftHeight = Get-NormalMapHeight -Color ($SourceBitmap.GetPixel($leftX, $pixelY))
                    $rightHeight = Get-NormalMapHeight -Color ($SourceBitmap.GetPixel($rightX, $pixelY))
                    $upHeight = Get-NormalMapHeight -Color ($SourceBitmap.GetPixel($pixelX, $upY))
                    $downHeight = Get-NormalMapHeight -Color ($SourceBitmap.GetPixel($pixelX, $downY))

                    $normalX = ($leftHeight - $rightHeight) * $Strength
                    $normalY = ($upHeight - $downHeight) * $Strength
                    $normalZ = 1.0
                    $normalLength = [Math]::Sqrt(($normalX * $normalX) + ($normalY * $normalY) + ($normalZ * $normalZ))
                    if ($normalLength -le 0.0) {
                        $normalLength = 1.0
                    }

                    $normalX /= $normalLength
                    $normalY /= $normalLength
                    $normalZ /= $normalLength

                    if ($InvertY) {
                        $normalY = -$normalY
                    }

                    $red = [Math]::Max(0, [Math]::Min(255, [int][Math]::Round((($normalX * 0.5) + 0.5) * 255.0)))
                    $green = [Math]::Max(0, [Math]::Min(255, [int][Math]::Round((($normalY * 0.5) + 0.5) * 255.0)))
                    $blue = [Math]::Max(0, [Math]::Min(255, [int][Math]::Round((($normalZ * 0.5) + 0.5) * 255.0)))

                    $bitmap.SetPixel($pixelX, $pixelY, [System.Drawing.Color]::FromArgb($centerColor.A, $red, $green, $blue))
                }
            }

            return $bitmap
        }

        function New-LavaSimulation {
            param(
                [bool]$Flowing,
                [int]$Seed
            )

            return @{
                Current = (New-Object 'float[]' 256)
                Next = (New-Object 'float[]' 256)
                Accumulated = (New-Object 'float[]' 256)
                Impulse = (New-Object 'float[]' 256)
                Flowing = $Flowing
                Offset = 0
                Random = [System.Random]::new($Seed)
            }
        }

        function Step-LavaSimulation {
            param([hashtable]$Simulation)

            $current = $Simulation.Current
            $next = $Simulation.Next
            $accumulated = $Simulation.Accumulated
            $impulse = $Simulation.Impulse
            $random = $Simulation.Random
            $twoPi = [Math]::PI * 2.0

            if ($Simulation.Flowing) {
                $Simulation.Offset++
            }

            for ($x = 0; $x -lt 16; $x++) {
                for ($y = 0; $y -lt 16; $y++) {
                    $sum = 0.0
                    $xOffset = [int]([Math]::Sin($y * $twoPi / 16.0) * 1.2)
                    $yOffset = [int]([Math]::Sin($x * $twoPi / 16.0) * 1.2)

                    for ($sampleX = $x - 1; $sampleX -le $x + 1; $sampleX++) {
                        for ($sampleY = $y - 1; $sampleY -le $y + 1; $sampleY++) {
                            $wrappedX = ($sampleX + $xOffset) -band 0xF
                            $wrappedY = ($sampleY + $yOffset) -band 0xF
                            $sum += $current[$wrappedX + ($wrappedY * 16)]
                        }
                    }

                    $index = $x + ($y * 16)
                    $averageAccumulation = (
                        $accumulated[(($x + 0) -band 0xF) + ((($y + 0) -band 0xF) * 16)] +
                        $accumulated[(($x + 1) -band 0xF) + ((($y + 0) -band 0xF) * 16)] +
                        $accumulated[(($x + 1) -band 0xF) + ((($y + 1) -band 0xF) * 16)] +
                        $accumulated[(($x + 0) -band 0xF) + ((($y + 1) -band 0xF) * 16)]
                    ) / 4.0
                    $next[$index] = $sum / 10.0 + $averageAccumulation * 0.8
                    $accumulated[$index] += $impulse[$index] * 0.01
                    if ($accumulated[$index] -lt 0.0) {
                        $accumulated[$index] = 0.0
                    }

                    $impulse[$index] -= 0.06
                    if ($random.NextDouble() -lt 0.005) {
                        $impulse[$index] = 1.5
                    }
                }
            }

            $Simulation.Next = $current
            $Simulation.Current = $next
        }

        function New-LavaTileBitmap {
            param([hashtable]$Simulation)

            $bitmap = New-Object System.Drawing.Bitmap(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
            $rowOffset = 0

            for ($pixelIndex = 0; $pixelIndex -lt 256; $pixelIndex++) {
                $sampleIndex = ($pixelIndex - $rowOffset) -band 0xFF
                $intensity = $Simulation.Current[$sampleIndex] * 2.0
                if ($intensity -gt 1.0) {
                    $intensity = 1.0
                }
                if ($intensity -lt 0.0) {
                    $intensity = 0.0
                }

                $red = [int]($intensity * 100.0 + 155.0)
                $green = [int]($intensity * $intensity * 255.0)
                $blue = [int]([Math]::Pow($intensity, 4.0) * 128.0)

                $pixelX = $pixelIndex % 16
                $pixelY = [int][Math]::Floor($pixelIndex / 16)
                $bitmap.SetPixel($pixelX, $pixelY, [System.Drawing.Color]::FromArgb(255, $red, $green, $blue))
            }

            return $bitmap
        }

        function New-LavaEmissiveTileBitmap {
            param([hashtable]$Simulation)

            $bitmap = New-Object System.Drawing.Bitmap(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
            $rowOffset = 0

            for ($pixelIndex = 0; $pixelIndex -lt 256; $pixelIndex++) {
                $sampleIndex = ($pixelIndex - $rowOffset) -band 0xFF
                $intensity = $Simulation.Current[$sampleIndex] * 2.0
                if ($intensity -gt 1.0) {
                    $intensity = 1.0
                }
                if ($intensity -lt 0.0) {
                    $intensity = 0.0
                }

                $emissive = ($intensity - 0.18) / 0.82
                if ($emissive -lt 0.0) {
                    $emissive = 0.0
                }
                if ($emissive -gt 1.0) {
                    $emissive = 1.0
                }

                $emissive = [Math]::Pow($emissive, 1.15)
                # Match the albedo palette exactly so emission only modulates
                # brightness, never hue. Cool pixels still fall to black via the
                # $emissive mask above.
                $red = [int](($intensity * 100.0 + 155.0) * $emissive)
                $green = [int](($intensity * $intensity * 255.0) * $emissive)
                $blue = [int]([Math]::Pow($intensity, 4.0) * 128.0 * $emissive)

                $pixelX = $pixelIndex % 16
                $pixelY = [int][Math]::Floor($pixelIndex / 16)
                $bitmap.SetPixel($pixelX, $pixelY, [System.Drawing.Color]::FromArgb(255, $red, $green, $blue))
            }

            return $bitmap
        }

        function Get-BitmapDifference {
            param(
                [System.Drawing.Bitmap]$BitmapA,
                [System.Drawing.Bitmap]$BitmapB
            )

            $difference = 0.0
            for ($pixelY = 0; $pixelY -lt 16; $pixelY++) {
                for ($pixelX = 0; $pixelX -lt 16; $pixelX++) {
                    $colorA = $BitmapA.GetPixel($pixelX, $pixelY)
                    $colorB = $BitmapB.GetPixel($pixelX, $pixelY)
                    $difference += [Math]::Abs($colorA.R - $colorB.R)
                    $difference += [Math]::Abs($colorA.G - $colorB.G)
                    $difference += [Math]::Abs($colorA.B - $colorB.B)
                }
            }

            return $difference
        }

        function Select-BestLoopStart {
            param(
                [object[]]$Frames,
                [int]$CycleFrameCount,
                [scriptblock]$DifferenceEvaluator
            )

            $searchFrameCount = $Frames.Count - ($CycleFrameCount * 2) + 1
            $bestStart = 0
            $bestScore = [double]::PositiveInfinity

            if ($searchFrameCount -le 0) {
                $searchFrameCount = $Frames.Count - $CycleFrameCount
                for ($candidateStart = 0; $candidateStart -lt $searchFrameCount; $candidateStart++) {
                    $candidateScore = & $DifferenceEvaluator $Frames[$candidateStart] $Frames[$candidateStart + $CycleFrameCount]
                    if ($candidateScore -lt $bestScore) {
                        $bestScore = $candidateScore
                        $bestStart = $candidateStart
                    }
                }

                return $bestStart
            }

            for ($candidateStart = 0; $candidateStart -lt $searchFrameCount; $candidateStart++) {
                $candidateScore = 0.0
                for ($cycleOffset = 0; $cycleOffset -lt $CycleFrameCount; $cycleOffset++) {
                    $candidateScore += & $DifferenceEvaluator $Frames[$candidateStart + $cycleOffset] $Frames[$candidateStart + $CycleFrameCount + $cycleOffset]
                }
                if ($candidateScore -lt $bestScore) {
                    $bestScore = $candidateScore
                    $bestStart = $candidateStart
                }
            }

            return $bestStart
        }

        function Blend-Bitmap {
            param(
                [System.Drawing.Bitmap]$BitmapA,
                [System.Drawing.Bitmap]$BitmapB,
                [double]$BlendAmount
            )

            $clampedBlendAmount = [Math]::Max(0.0, [Math]::Min(1.0, $BlendAmount))
            $inverseBlendAmount = 1.0 - $clampedBlendAmount
            $blendedBitmap = New-Object System.Drawing.Bitmap(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)

            for ($pixelY = 0; $pixelY -lt 16; $pixelY++) {
                for ($pixelX = 0; $pixelX -lt 16; $pixelX++) {
                    $colorA = $BitmapA.GetPixel($pixelX, $pixelY)
                    $colorB = $BitmapB.GetPixel($pixelX, $pixelY)
                    $alpha = [int][Math]::Round($colorA.A * $inverseBlendAmount + $colorB.A * $clampedBlendAmount)
                    $red = [int][Math]::Round($colorA.R * $inverseBlendAmount + $colorB.R * $clampedBlendAmount)
                    $green = [int][Math]::Round($colorA.G * $inverseBlendAmount + $colorB.G * $clampedBlendAmount)
                    $blue = [int][Math]::Round($colorA.B * $inverseBlendAmount + $colorB.B * $clampedBlendAmount)
                    $blendedBitmap.SetPixel($pixelX, $pixelY, [System.Drawing.Color]::FromArgb($alpha, $red, $green, $blue))
                }
            }

            return $blendedBitmap
        }

        function Shift-BitmapRows {
            param(
                [System.Drawing.Bitmap]$Bitmap,
                [int]$RowOffset
            )

            $normalizedRowOffset = (($RowOffset % 16) + 16) % 16
            if ($normalizedRowOffset -eq 0) {
                return Blend-Bitmap -BitmapA $Bitmap -BitmapB $Bitmap -BlendAmount 0.0
            }

            $shiftedBitmap = New-Object System.Drawing.Bitmap(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
            for ($pixelY = 0; $pixelY -lt 16; $pixelY++) {
                $sourceY = (($pixelY - $normalizedRowOffset) + 16) % 16
                for ($pixelX = 0; $pixelX -lt 16; $pixelX++) {
                    $shiftedBitmap.SetPixel($pixelX, $pixelY, $Bitmap.GetPixel($pixelX, $sourceY))
                }
            }

            return $shiftedBitmap
        }

        function Select-BestLoopBoundary {
            param(
                [object[]]$Frames,
                [scriptblock]$DifferenceEvaluator
            )

            $bestBoundary = $Frames.Count - 1
            $bestScore = [double]::PositiveInfinity

            for ($boundaryIndex = 0; $boundaryIndex -lt $Frames.Count; $boundaryIndex++) {
                $currentFrame = $Frames[$boundaryIndex]
                $nextFrame = $Frames[(($boundaryIndex + 1) % $Frames.Count)]
                $boundaryScore = & $DifferenceEvaluator $currentFrame $nextFrame
                if ($boundaryScore -lt $bestScore) {
                    $bestScore = $boundaryScore
                    $bestBoundary = $boundaryIndex
                }
            }

            return $bestBoundary
        }

        function Repair-WorstLoopBoundary {
            param(
                [object[]]$Frames,
                [scriptblock]$DifferenceEvaluator,
                [string[]]$BitmapPropertyNames,
                [double]$OutlierRatio = 4.0,
                [int]$MinimumRepairFrameCount = 4,
                [int]$MaximumRepairFrameCount = 10
            )

            if ($Frames.Count -lt 6) {
                return
            }

            $boundaryScores = @()
            for ($boundaryIndex = 0; $boundaryIndex -lt $Frames.Count; $boundaryIndex++) {
                $currentFrame = $Frames[$boundaryIndex]
                $nextFrame = $Frames[(($boundaryIndex + 1) % $Frames.Count)]
                $boundaryScores += (& $DifferenceEvaluator $currentFrame $nextFrame)
            }

            $averageBoundaryScore = ($boundaryScores | Measure-Object -Average).Average
            $maximumBoundaryScore = ($boundaryScores | Measure-Object -Maximum).Maximum
            if (($averageBoundaryScore -le 0) -or ($maximumBoundaryScore -lt ($averageBoundaryScore * $OutlierRatio))) {
                return
            }

            $worstBoundary = [array]::IndexOf($boundaryScores, $maximumBoundaryScore)
            $repairFrameCount = [int][Math]::Ceiling($maximumBoundaryScore / $averageBoundaryScore)
            $repairFrameCount = [Math]::Max($MinimumRepairFrameCount, $repairFrameCount)
            $repairFrameCount = [Math]::Min($MaximumRepairFrameCount, $repairFrameCount)
            $repairFrameCount = [Math]::Min($Frames.Count - 2, $repairFrameCount)
            $repairFrameCount = [Math]::Max(1, $repairFrameCount)

            $leftSpan = [int][Math]::Floor($repairFrameCount / 2)
            $rightSpan = $repairFrameCount - $leftSpan + 1
            $anchorLeftIndex = (($worstBoundary - $leftSpan + $Frames.Count) % $Frames.Count)
            $anchorRightIndex = (($worstBoundary + $rightSpan) % $Frames.Count)

            for ($repairStep = 1; $repairStep -le $repairFrameCount; $repairStep++) {
                $repairIndex = (($anchorLeftIndex + $repairStep) % $Frames.Count)
                $blendAmount = $repairStep / ($repairFrameCount + 1.0)
                $frame = $Frames[$repairIndex]
                foreach ($bitmapPropertyName in $BitmapPropertyNames) {
                    $originalBitmap = $frame[$bitmapPropertyName]
                    $frame[$bitmapPropertyName] = Blend-Bitmap -BitmapA $Frames[$anchorLeftIndex][$bitmapPropertyName] -BitmapB $Frames[$anchorRightIndex][$bitmapPropertyName] -BlendAmount $blendAmount
                    if ($null -ne $originalBitmap) {
                        $originalBitmap.Dispose()
                    }
                }
            }
        }

        Set-NearestNeighborGraphics -TargetGraphics $graphics
        Set-NearestNeighborGraphics -TargetGraphics $waterGraphics
        Set-NearestNeighborGraphics -TargetGraphics $waterNormalGraphics
        Set-NearestNeighborGraphics -TargetGraphics $lavaGraphics
        Set-NearestNeighborGraphics -TargetGraphics $lavaEmissiveGraphics

        $waterStillSimulation = New-WaterSimulation -Flowing $false -Seed 1337
        $waterFlowSimulation = New-WaterSimulation -Flowing $true -Seed 1338
        $lavaStillSimulation = New-LavaSimulation -Flowing $false -Seed 1339
        $lavaFlowSimulation = New-LavaSimulation -Flowing $true -Seed 1340

        for ($warmupStep = 0; $warmupStep -lt 32; $warmupStep++) {
            Step-WaterSimulation -Simulation $waterStillSimulation
            Step-WaterSimulation -Simulation $waterFlowSimulation
            Step-LavaSimulation -Simulation $lavaStillSimulation
            Step-LavaSimulation -Simulation $lavaFlowSimulation
        }

        $waterTerrainTileIndices = @(205, 206)
        $lavaTerrainTileIndices = @(237, 238)
        $loopSearchFrameCount = 96
        $totalGeneratedFrameCount = $frameCount + $loopSearchFrameCount

        for ($frameIndex = 0; $frameIndex -lt $totalGeneratedFrameCount; $frameIndex++) {
            $waterStillBitmap = New-WaterTileBitmap -Simulation $waterStillSimulation
            $waterFlowBitmap = New-WaterTileBitmap -Simulation $waterFlowSimulation
            $lavaStillBitmap = New-LavaTileBitmap -Simulation $lavaStillSimulation
            $lavaFlowBitmap = New-LavaTileBitmap -Simulation $lavaFlowSimulation
            $lavaStillEmissiveBitmap = New-LavaEmissiveTileBitmap -Simulation $lavaStillSimulation
            $lavaFlowEmissiveBitmap = New-LavaEmissiveTileBitmap -Simulation $lavaFlowSimulation
            $waterFrames += ,@{
                Still = $waterStillBitmap
                Flow = $waterFlowBitmap
            }
            $lavaFrames += ,@{
                Still = $lavaStillBitmap
                Flow = $lavaFlowBitmap
                StillEmissive = $lavaStillEmissiveBitmap
                FlowEmissive = $lavaFlowEmissiveBitmap
            }

            Step-WaterSimulation -Simulation $waterStillSimulation
            Step-WaterSimulation -Simulation $waterFlowSimulation
            Step-LavaSimulation -Simulation $lavaStillSimulation
            Step-LavaSimulation -Simulation $lavaFlowSimulation
        }

        $waterLoopStart = Select-BestLoopStart -Frames $waterFrames -CycleFrameCount $frameCount -DifferenceEvaluator {
            param($startFrame, $endFrame)
            return (Get-BitmapDifference -BitmapA $startFrame.Still -BitmapB $endFrame.Still) + (Get-BitmapDifference -BitmapA $startFrame.Flow -BitmapB $endFrame.Flow)
        }
        $lavaLoopStart = Select-BestLoopStart -Frames $lavaFrames -CycleFrameCount $frameCount -DifferenceEvaluator {
            param($startFrame, $endFrame)
            return (Get-BitmapDifference -BitmapA $startFrame.Still -BitmapB $endFrame.Still) +
                (Get-BitmapDifference -BitmapA $startFrame.Flow -BitmapB $endFrame.Flow) +
                (Get-BitmapDifference -BitmapA $startFrame.StillEmissive -BitmapB $endFrame.StillEmissive) +
                (Get-BitmapDifference -BitmapA $startFrame.FlowEmissive -BitmapB $endFrame.FlowEmissive)
        }

        for ($frameIndex = 0; $frameIndex -lt $frameCount; $frameIndex++) {
            $waterFrameA = $waterFrames[$waterLoopStart + $frameIndex]
            $waterFrameB = $waterFrames[$waterLoopStart + $frameCount + $frameIndex]
            $lavaFrameA = $lavaFrames[$lavaLoopStart + $frameIndex]
            $lavaFrameB = $lavaFrames[$lavaLoopStart + $frameCount + $frameIndex]
            $periodicWaterFrames += ,@{
                Still = Blend-Bitmap -BitmapA $waterFrameA.Still -BitmapB $waterFrameB.Still -BlendAmount 0.5
                Flow = Blend-Bitmap -BitmapA $waterFrameA.Flow -BitmapB $waterFrameB.Flow -BlendAmount 0.5
            }
            $periodicLavaFrames += ,@{
                Still = Blend-Bitmap -BitmapA $lavaFrameA.Still -BitmapB $lavaFrameB.Still -BlendAmount 0.5
                Flow = Blend-Bitmap -BitmapA $lavaFrameA.Flow -BitmapB $lavaFrameB.Flow -BlendAmount 0.5
                StillEmissive = Blend-Bitmap -BitmapA $lavaFrameA.StillEmissive -BitmapB $lavaFrameB.StillEmissive -BlendAmount 0.5
                FlowEmissive = Blend-Bitmap -BitmapA $lavaFrameA.FlowEmissive -BitmapB $lavaFrameB.FlowEmissive -BlendAmount 0.5
            }
        }

        Repair-WorstLoopBoundary -Frames $periodicLavaFrames -DifferenceEvaluator {
            param($currentFrame, $nextFrame)
            return (Get-BitmapDifference -BitmapA $currentFrame.Still -BitmapB $nextFrame.Still) +
                (Get-BitmapDifference -BitmapA $currentFrame.Flow -BitmapB $nextFrame.Flow) +
                (Get-BitmapDifference -BitmapA $currentFrame.StillEmissive -BitmapB $nextFrame.StillEmissive) +
                (Get-BitmapDifference -BitmapA $currentFrame.FlowEmissive -BitmapB $nextFrame.FlowEmissive)
        } -BitmapPropertyNames @('Still', 'Flow', 'StillEmissive', 'FlowEmissive')

        Repair-WorstLoopBoundary -Frames $periodicWaterFrames -DifferenceEvaluator {
            param($currentFrame, $nextFrame)
            return (Get-BitmapDifference -BitmapA $currentFrame.Still -BitmapB $nextFrame.Still) +
                (Get-BitmapDifference -BitmapA $currentFrame.Flow -BitmapB $nextFrame.Flow)
        } -BitmapPropertyNames @('Still', 'Flow')

        $waterLoopBoundary = Select-BestLoopBoundary -Frames $periodicWaterFrames -DifferenceEvaluator {
            param($currentFrame, $nextFrame)
            return (Get-BitmapDifference -BitmapA $currentFrame.Still -BitmapB $nextFrame.Still) + (Get-BitmapDifference -BitmapA $currentFrame.Flow -BitmapB $nextFrame.Flow)
        }
        $lavaLoopBoundary = Select-BestLoopBoundary -Frames $periodicLavaFrames -DifferenceEvaluator {
            param($currentFrame, $nextFrame)
            return (Get-BitmapDifference -BitmapA $currentFrame.Still -BitmapB $nextFrame.Still) +
                (Get-BitmapDifference -BitmapA $currentFrame.Flow -BitmapB $nextFrame.Flow) +
                (Get-BitmapDifference -BitmapA $currentFrame.StillEmissive -BitmapB $nextFrame.StillEmissive) +
                (Get-BitmapDifference -BitmapA $currentFrame.FlowEmissive -BitmapB $nextFrame.FlowEmissive)
        }
        $waterLoopRotation = ($waterLoopBoundary + 1) % $frameCount
        $lavaLoopRotation = ($lavaLoopBoundary + 1) % $frameCount

        for ($frameIndex = 0; $frameIndex -lt $frameCount; $frameIndex++) {
            $frameDestinationX = $frameIndex * $frameWidth
            $waterFrame = $periodicWaterFrames[(($frameIndex + $waterLoopRotation) % $frameCount)]
            $lavaFrame = $periodicLavaFrames[(($frameIndex + $lavaLoopRotation) % $frameCount)]
            $waterFlowRowOffset = [int][Math]::Floor($frameIndex / 2)
            $lavaFlowRowOffset = [int][Math]::Floor($frameIndex / 2)
            $waterStillOutput = $waterFrame.Still
            $waterFlowOutput = Shift-BitmapRows -Bitmap $waterFrame.Flow -RowOffset $waterFlowRowOffset
            $waterStillNormalOutput = New-NormalMapBitmap -SourceBitmap $waterStillOutput -Strength 0.73 -InvertY $true
            $waterFlowNormalOutput = New-NormalMapBitmap -SourceBitmap $waterFlowOutput -Strength 0.73 -InvertY $true
            $lavaStillOutput = $lavaFrame.Still
            $lavaFlowOutput = Shift-BitmapRows -Bitmap $lavaFrame.Flow -RowOffset $lavaFlowRowOffset
            $lavaStillEmissiveOutput = $lavaFrame.StillEmissive
            $lavaFlowEmissiveOutput = Shift-BitmapRows -Bitmap $lavaFrame.FlowEmissive -RowOffset $lavaFlowRowOffset

            try {
                $waterGraphics.DrawImage($waterStillOutput, (New-Object System.Drawing.Rectangle -ArgumentList $frameDestinationX, 0, $tileSize, $tileSize))
                $waterGraphics.DrawImage($waterFlowOutput, (New-Object System.Drawing.Rectangle -ArgumentList ($frameDestinationX + $tileSize), 0, $tileSize, $tileSize))
                $waterNormalGraphics.DrawImage($waterStillNormalOutput, (New-Object System.Drawing.Rectangle -ArgumentList $frameDestinationX, 0, $tileSize, $tileSize))
                $waterNormalGraphics.DrawImage($waterFlowNormalOutput, (New-Object System.Drawing.Rectangle -ArgumentList ($frameDestinationX + $tileSize), 0, $tileSize, $tileSize))
                $lavaGraphics.DrawImage($lavaStillOutput, (New-Object System.Drawing.Rectangle -ArgumentList $frameDestinationX, 0, $tileSize, $tileSize))
                $lavaGraphics.DrawImage($lavaFlowOutput, (New-Object System.Drawing.Rectangle -ArgumentList ($frameDestinationX + $tileSize), 0, $tileSize, $tileSize))
                $lavaEmissiveGraphics.DrawImage($lavaStillEmissiveOutput, (New-Object System.Drawing.Rectangle -ArgumentList $frameDestinationX, 0, $tileSize, $tileSize))
                $lavaEmissiveGraphics.DrawImage($lavaFlowEmissiveOutput, (New-Object System.Drawing.Rectangle -ArgumentList ($frameDestinationX + $tileSize), 0, $tileSize, $tileSize))

                if ($frameIndex -eq 0) {
                    $frameZeroBitmaps = @($waterStillOutput, $waterFlowOutput, $lavaStillOutput, $lavaFlowOutput)
                    $frameZeroTileIndices = @($waterTerrainTileIndices[0], $waterTerrainTileIndices[1], $lavaTerrainTileIndices[0], $lavaTerrainTileIndices[1])
                    for ($tileArrayIndex = 0; $tileArrayIndex -lt $frameZeroBitmaps.Count; $tileArrayIndex++) {
                        $tileIndex = $frameZeroTileIndices[$tileArrayIndex]
                        $destinationTileX = ($tileIndex % 16) * $tileSize
                        $destinationTileY = [int][Math]::Floor($tileIndex / 16) * $tileSize
                        $graphics.DrawImage($frameZeroBitmaps[$tileArrayIndex], (New-Object System.Drawing.Rectangle -ArgumentList $destinationTileX, $destinationTileY, $tileSize, $tileSize))
                    }
                }
            } finally {
                $waterStillNormalOutput.Dispose()
                $waterFlowNormalOutput.Dispose()
                $waterFlowOutput.Dispose()
                $lavaFlowOutput.Dispose()
                $lavaFlowEmissiveOutput.Dispose()
            }
        }

        $waterGraphics.Dispose()
        $waterGraphics = $null
        $waterNormalGraphics.Dispose()
        $waterNormalGraphics = $null
        $lavaGraphics.Dispose()
        $lavaGraphics = $null
        $lavaEmissiveGraphics.Dispose()
        $lavaEmissiveGraphics = $null
        $graphics.Dispose()
        $graphics = $null

        $temporaryTerrainPath = "$TerrainPngPath.tmp"
        $temporaryWaterPath = "$WaterPngPath.tmp"
        $temporaryWaterNormalPath = "$WaterNormalPngPath.tmp"
        $temporaryLavaPath = "$LavaPngPath.tmp"
        $temporaryLavaEmissivePath = "$LavaEmissivePngPath.tmp"
        $terrainBitmap.Save($temporaryTerrainPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $waterAtlasBitmap.Save($temporaryWaterPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $waterNormalAtlasBitmap.Save($temporaryWaterNormalPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $lavaAtlasBitmap.Save($temporaryLavaPath, [System.Drawing.Imaging.ImageFormat]::Png)
        $lavaEmissiveAtlasBitmap.Save($temporaryLavaEmissivePath, [System.Drawing.Imaging.ImageFormat]::Png)

        $waterAtlasBitmap.Dispose()
        $waterAtlasBitmap = $null
        $waterNormalAtlasBitmap.Dispose()
        $waterNormalAtlasBitmap = $null
        $lavaAtlasBitmap.Dispose()
        $lavaAtlasBitmap = $null
        $lavaEmissiveAtlasBitmap.Dispose()
        $lavaEmissiveAtlasBitmap = $null
        foreach ($waterFrame in $waterFrames) {
            $waterFrame.Still.Dispose()
            $waterFrame.Flow.Dispose()
        }
        foreach ($lavaFrame in $lavaFrames) {
            $lavaFrame.Still.Dispose()
            $lavaFrame.Flow.Dispose()
            $lavaFrame.StillEmissive.Dispose()
            $lavaFrame.FlowEmissive.Dispose()
        }
        foreach ($waterFrame in $periodicWaterFrames) {
            $waterFrame.Still.Dispose()
            $waterFrame.Flow.Dispose()
        }
        foreach ($lavaFrame in $periodicLavaFrames) {
            $lavaFrame.Still.Dispose()
            $lavaFrame.Flow.Dispose()
            $lavaFrame.StillEmissive.Dispose()
            $lavaFrame.FlowEmissive.Dispose()
        }
        $waterFrames = @()
        $lavaFrames = @()
        $periodicWaterFrames = @()
        $periodicLavaFrames = @()
        $terrainBitmap.Dispose()
        $terrainBitmap = $null
        $terrainSource.Dispose()
        $terrainSource = $null

        Copy-Item -Force $temporaryTerrainPath $TerrainPngPath
        Remove-Item -Force $temporaryTerrainPath
        Copy-Item -Force $temporaryWaterPath $WaterPngPath
        Remove-Item -Force $temporaryWaterPath
        Copy-Item -Force $temporaryWaterNormalPath $WaterNormalPngPath
        Remove-Item -Force $temporaryWaterNormalPath
        Copy-Item -Force $temporaryLavaPath $LavaPngPath
        Remove-Item -Force $temporaryLavaPath
        Copy-Item -Force $temporaryLavaEmissivePath $LavaEmissivePngPath
        Remove-Item -Force $temporaryLavaEmissivePath
    } finally {
        if ($null -ne $lavaEmissiveGraphics) {
            $lavaEmissiveGraphics.Dispose()
        }
        if ($null -ne $lavaGraphics) {
            $lavaGraphics.Dispose()
        }
        if ($null -ne $waterGraphics) {
            $waterGraphics.Dispose()
        }
        if ($null -ne $waterNormalGraphics) {
            $waterNormalGraphics.Dispose()
        }
        if ($null -ne $graphics) {
            $graphics.Dispose()
        }
        if ($null -ne $lavaAtlasBitmap) {
            $lavaAtlasBitmap.Dispose()
        }
        if ($null -ne $lavaEmissiveAtlasBitmap) {
            $lavaEmissiveAtlasBitmap.Dispose()
        }
        if ($null -ne $waterAtlasBitmap) {
            $waterAtlasBitmap.Dispose()
        }
        if ($null -ne $waterNormalAtlasBitmap) {
            $waterNormalAtlasBitmap.Dispose()
        }
        foreach ($waterFrame in $waterFrames) {
            $waterFrame.Still.Dispose()
            $waterFrame.Flow.Dispose()
        }
        foreach ($lavaFrame in $lavaFrames) {
            $lavaFrame.Still.Dispose()
            $lavaFrame.Flow.Dispose()
            $lavaFrame.StillEmissive.Dispose()
            $lavaFrame.FlowEmissive.Dispose()
        }
        foreach ($waterFrame in $periodicWaterFrames) {
            $waterFrame.Still.Dispose()
            $waterFrame.Flow.Dispose()
        }
        foreach ($lavaFrame in $periodicLavaFrames) {
            $lavaFrame.Still.Dispose()
            $lavaFrame.Flow.Dispose()
            $lavaFrame.StillEmissive.Dispose()
            $lavaFrame.FlowEmissive.Dispose()
        }
        if ($null -ne $terrainBitmap) {
            $terrainBitmap.Dispose()
        }
        if ($null -ne $terrainSource) {
            $terrainSource.Dispose()
        }
    }
}

function New-PortalAtlas {
    param(
        [string]$PortalPngPath
    )

    $portalAtlasBitmap = $null
    $graphics = $null

    try {
        $frameCount = 32
        $tileSize = 16
        $atlasWidth = $frameCount * $tileSize
        $portalAtlasBitmap = New-Object System.Drawing.Bitmap -ArgumentList $atlasWidth, $tileSize, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $graphics = [System.Drawing.Graphics]::FromImage($portalAtlasBitmap)
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
        $graphics.CompositingMode = [System.Drawing.Drawing2D.CompositingMode]::SourceCopy
        $graphics.Clear([System.Drawing.Color]::FromArgb(0, 0, 0, 0))

        $random = [System.Random]::new(100)

        for ($frameIndex = 0; $frameIndex -lt $frameCount; $frameIndex++) {
            $frameOffsetX = $frameIndex * $tileSize
            for ($pixelX = 0; $pixelX -lt $tileSize; $pixelX++) {
                for ($pixelY = 0; $pixelY -lt $tileSize; $pixelY++) {
                    $portalSample = 0.0
                    for ($layerIndex = 0; $layerIndex -lt 2; $layerIndex++) {
                        $layerOffset = $layerIndex * ($tileSize / 2.0)
                        $sampleX = (($pixelX - $layerOffset) / $tileSize) * 2.0
                        $sampleY = (($pixelY - $layerOffset) / $tileSize) * 2.0
                        if ($sampleX -lt -1.0) {
                            $sampleX += 2.0
                        }
                        if ($sampleX -ge 1.0) {
                            $sampleX -= 2.0
                        }
                        if ($sampleY -lt -1.0) {
                            $sampleY += 2.0
                        }
                        if ($sampleY -ge 1.0) {
                            $sampleY -= 2.0
                        }

                        $radius = $sampleX * $sampleX + $sampleY * $sampleY
                        $phase = [Math]::Atan2($sampleY, $sampleX)
                        $phase += ((($frameIndex / [double]$frameCount) * [Math]::PI * 2.0) - ($radius * 10.0) + ($layerIndex * 2.0)) * (($layerIndex * 2.0) - 1.0)
                        $wave = ([Math]::Sin($phase) + 1.0) / 2.0
                        $portalSample += ($wave / ($radius + 1.0)) * 0.5
                    }

                    $portalSample += $random.NextDouble() * 0.1
                    $red = [Math]::Min(255, [Math]::Max(0, [int]([Math]::Round(($portalSample * $portalSample * 200.0) + 55.0))))
                    $greenStrength = $portalSample * $portalSample
                    $greenStrength *= $greenStrength
                    $green = [Math]::Min(255, [Math]::Max(0, [int]([Math]::Round($greenStrength * 255.0))))
                    $blue = [Math]::Min(255, [Math]::Max(0, [int]([Math]::Round(($portalSample * 100.0) + 155.0))))
                    $alpha = [Math]::Min(255, [Math]::Max(0, [int]([Math]::Round(($portalSample * 100.0) + 155.0))))
                    $portalAtlasBitmap.SetPixel(
                        $frameOffsetX + $pixelX,
                        $pixelY,
                        [System.Drawing.Color]::FromArgb($alpha, $red, $green, $blue))
                }
            }
        }

        $graphics.Dispose()
        $graphics = $null

        $temporaryPortalPath = "$PortalPngPath.tmp"
        $portalAtlasBitmap.Save($temporaryPortalPath, [System.Drawing.Imaging.ImageFormat]::Png)

        $portalAtlasBitmap.Dispose()
        $portalAtlasBitmap = $null

        Copy-Item -Force $temporaryPortalPath $PortalPngPath
        Remove-Item -Force $temporaryPortalPath
    } finally {
        if ($null -ne $graphics) {
            $graphics.Dispose()
        }
        if ($null -ne $portalAtlasBitmap) {
            $portalAtlasBitmap.Dispose()
        }
    }
}

function New-RedstoneEmissiveAtlas {
    param(
        [string]$TerrainPngPath,
        [string]$RedstoneEmissivePngPath
    )

    $terrainSource = [System.Drawing.Image]::FromFile($TerrainPngPath)
    $terrainBitmap = $null
    $emissiveBitmap = $null
    $graphics = $null

    try {
        $terrainBitmap = New-Object System.Drawing.Bitmap($terrainSource)
        $emissiveBitmap = New-Object System.Drawing.Bitmap($terrainBitmap.Width, $terrainBitmap.Height, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        $graphics = [System.Drawing.Graphics]::FromImage($emissiveBitmap)
        $graphics.Clear([System.Drawing.Color]::FromArgb(255, 0, 0, 0))

        $tileSize = 16
        $redstoneTileIndices = @(164, 165)
        foreach ($tileIndex in $redstoneTileIndices) {
            $tileOriginX = ($tileIndex % 16) * $tileSize
            $tileOriginY = [int][Math]::Floor($tileIndex / 16) * $tileSize
            for ($pixelY = 0; $pixelY -lt $tileSize; $pixelY++) {
                for ($pixelX = 0; $pixelX -lt $tileSize; $pixelX++) {
                    $sourceColor = $terrainBitmap.GetPixel($tileOriginX + $pixelX, $tileOriginY + $pixelY)
                    if ($sourceColor.A -eq 0) {
                        continue
                    }

                    $red = [Math]::Min(255, [int]([Math]::Round($sourceColor.R * 2.4)))
                    $green = [Math]::Min(255, [int]([Math]::Round($sourceColor.G * 1.4)))
                    $blue = [Math]::Min(255, [int]([Math]::Round($sourceColor.B * 1.2)))
                    $emissiveBitmap.SetPixel(
                        $tileOriginX + $pixelX,
                        $tileOriginY + $pixelY,
                        [System.Drawing.Color]::FromArgb(255, $red, $green, $blue))
                }
            }
        }

        $graphics.Dispose()
        $graphics = $null

        $temporaryEmissivePath = "$RedstoneEmissivePngPath.tmp"
        $emissiveBitmap.Save($temporaryEmissivePath, [System.Drawing.Imaging.ImageFormat]::Png)

        $emissiveBitmap.Dispose()
        $emissiveBitmap = $null
        $terrainBitmap.Dispose()
        $terrainBitmap = $null
        $terrainSource.Dispose()
        $terrainSource = $null

        Copy-Item -Force $temporaryEmissivePath $RedstoneEmissivePngPath
        Remove-Item -Force $temporaryEmissivePath
    } finally {
        if ($null -ne $graphics) {
            $graphics.Dispose()
        }
        if ($null -ne $emissiveBitmap) {
            $emissiveBitmap.Dispose()
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
$javaSourceRoot = Join-Path $repoRoot "src\java-src"
$toolSourceRoot = Join-Path $repoRoot "src\tools-src"

if (-not $PrismRoot) {
    if (-not $env:APPDATA) {
        throw "PrismLauncher could not be located because APPDATA is not set. Pass -PrismRoot explicitly."
    }
    $PrismRoot = Join-Path $env:APPDATA "PrismLauncher"
}
$PrismRoot = [System.IO.Path]::GetFullPath($PrismRoot)

$prismLibrariesRoot = Join-Path $PrismRoot "libraries"
$instanceRoot = Join-Path (Join-Path $PrismRoot "instances") $InstanceName
$lwjglVersion = "2.9.4-nightly-20150209"

if (-not $MinecraftJar) {
    $MinecraftJar = Join-Path $prismLibrariesRoot "com\mojang\minecraft\b1.7.3\minecraft-b1.7.3-client.jar"
}
if (-not $ModdedMinecraftJar) {
    $ModdedMinecraftJar = Join-Path $instanceRoot "libraries\customjar-1.jar"
}
if (-not $LwjglJar) {
    $LwjglJar = Join-Path $prismLibrariesRoot "org\lwjgl\lwjgl\lwjgl\$lwjglVersion\lwjgl-$lwjglVersion.jar"
}
if (-not $LwjglUtilJar) {
    $LwjglUtilJar = Join-Path $prismLibrariesRoot "org\lwjgl\lwjgl\lwjgl_util\$lwjglVersion\lwjgl_util-$lwjglVersion.jar"
}
if (-not $AsmJar) {
    $AsmJar = Join-Path $prismLibrariesRoot "org\ow2\asm\asm\9.9\asm-9.9.jar"
}
if (-not $AsmTreeJar) {
    $AsmTreeJar = Join-Path $prismLibrariesRoot "org\ow2\asm\asm-tree\9.9\asm-tree-9.9.jar"
}

$javaToolchain = Resolve-JavaToolchain `
    -RequestedJavaHome $JavaHome `
    -RequestedJavacPath $JavacPath `
    -RequestedJavaPath $JavaPath `
    -RequestedJarPath $JarPath
$JavaHome = $javaToolchain.JavaHome
$JavacPath = $javaToolchain.JavacPath
$JavaPath = $javaToolchain.JavaPath
$JarPath = $javaToolchain.JarPath

$backupMinecraftJar = Join-Path $repoRoot "out\deploy-state\minecraft-b1.7.3-client.original.jar"

if (Test-Path $backupMinecraftJar) {
    $MinecraftJar = $backupMinecraftJar
}

if (-not $PatchSourceJar) {
    $PatchSourceJar = $MinecraftJar
    if (-not $DisableModdedPatchSource -and $env:MCRTX_USE_MODDED_PATCH_SOURCE -eq '1' -and (Test-Path $ModdedMinecraftJar)) {
        $PatchSourceJar = $ModdedMinecraftJar
    }
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
$nativePdb = Join-Path $repoRoot "build\native\$Configuration\mcrtx_jni.pdb"

$runtimeSourceFiles = @(
    (Join-Path $javaSourceRoot "lifecycle\MinecraftRemixLifecycleHooks.java"),
    (Join-Path $javaSourceRoot "scene\MinecraftRemixSceneHooks.java"),
    (Join-Path $javaSourceRoot "ui\MinecraftRemixUiHooks.java"),
    (Join-Path $javaSourceRoot "entities\MinecraftRemixEntityHooks.java"),
    (Join-Path $javaSourceRoot "particles\MinecraftRemixParticleHooks.java"),
    (Join-Path $javaSourceRoot "chunks\MinecraftRemixChunkHooks.java"),
    (Join-Path $javaSourceRoot "lifecycle\McrtxHookScreenshotHelper.java"),
    (Join-Path $javaSourceRoot "lifecycle\McrtxHookPerfTracker.java"),
    (Join-Path $javaSourceRoot "settings\McrtxQuickSettingsScreen.java"),
    (Join-Path $javaSourceRoot "settings\McrtxSettingsCategoryUi.java"),
    (Join-Path $javaSourceRoot "settings\McrtxSettingsCategories.java"),
    (Join-Path $javaSourceRoot "settings\McrtxGameplaySettingsUi.java"),
    (Join-Path $javaSourceRoot "settings\McrtxGraphicsSettingsUi.java"),
    (Join-Path $javaSourceRoot "settings\McrtxDebugSettingsUi.java"),
    (Join-Path $javaSourceRoot "settings\McrtxMaterialSettingsUi.java"),
    (Join-Path $javaSourceRoot "platform\cb.java"),
    (Join-Path $javaSourceRoot "particles\RemixBlockOutlineCapture.java"),
    (Join-Path $javaSourceRoot "scene\RemixCameraState.java"),
    (Join-Path $javaSourceRoot "scene\RemixCaveCulling.java"),
    (Join-Path $javaSourceRoot "chunks\RemixChunkCapture.java"),
    (Join-Path $javaSourceRoot "chunks\RemixChunkBuildSession.java"),
    (Join-Path $javaSourceRoot "chunks\RemixChunkBlockCapture.java"),
    (Join-Path $javaSourceRoot "chunks\RemixChunkWorldState.java"),
    (Join-Path $javaSourceRoot "chunks\RemixChunkSectionKey.java"),
    (Join-Path $javaSourceRoot "chunks\RemixChunkRecapturePass.java"),
    (Join-Path $javaSourceRoot "chunks\RemixChunkRecaptureQueue.java"),
    (Join-Path $javaSourceRoot "chunks\RemixChunkNeighborRefresh.java"),
    (Join-Path $javaSourceRoot "scene\RemixCloudCapture.java"),
    (Join-Path $javaSourceRoot "scene\RemixFogCapture.java"),
    (Join-Path $javaSourceRoot "particles\RemixDestroyOverlayCapture.java"),
    (Join-Path $javaSourceRoot "entities\RemixDynamicEntityCapture.java"),
    (Join-Path $javaSourceRoot "entities\RemixDynamicEntitySession.java"),
    (Join-Path $javaSourceRoot "entities\RemixDynamicModelCapture.java"),
    (Join-Path $javaSourceRoot "entities\CreeperFuseTracker.java"),
    (Join-Path $javaSourceRoot "entities\RemixLivingEntityCapture.java"),
    (Join-Path $javaSourceRoot "entities\RemixItemEntityCapture.java"),
    (Join-Path $javaSourceRoot "entities\RemixHeldItemCapture.java"),
    (Join-Path $javaSourceRoot "entities\RemixFirstPersonCapture.java"),
    (Join-Path $javaSourceRoot "entities\RemixSignCapture.java"),
    (Join-Path $javaSourceRoot "entities\RemixPaintingCapture.java"),
    (Join-Path $javaSourceRoot "entities\RemixEntityFireCapture.java"),
    (Join-Path $javaSourceRoot "particles\RemixParticleCapture.java"),
    (Join-Path $javaSourceRoot "ui\RemixUiCapture.java"),
    (Join-Path $javaSourceRoot "ui\RemixUiCaptureSession.java"),
    (Join-Path $javaSourceRoot "ui\RemixUiDrawList.java"),
    (Join-Path $javaSourceRoot "ui\RemixUiTextureRegistry.java"),
    (Join-Path $javaSourceRoot "ui\RemixUiProjection.java"),
    (Join-Path $javaSourceRoot "ui\RemixUiFontCapture.java"),
    (Join-Path $javaSourceRoot "ui\RemixUiModelCapture.java"),
    (Join-Path $javaSourceRoot "ui\RemixNameTagCapture.java"),
    (Join-Path $javaSourceRoot "scene\RemixWorldListener.java"),
    (Join-Path $javaSourceRoot "chunks\DirtyChunkSection.java"),
    (Join-Path $javaSourceRoot "core\mcrtx\bridge\CameraPose.java"),
    (Join-Path $javaSourceRoot "core\mcrtx\bridge\ColorMath.java"),
    (Join-Path $javaSourceRoot "core\mcrtx\bridge\MatrixMath.java"),
    (Join-Path $javaSourceRoot "core\mcrtx\bridge\McrtxRuntimeConfig.java"),
    (Join-Path $javaSourceRoot "settings\mcrtx\bridge\McrtxRuntimeSettingParser.java"),
    (Join-Path $javaSourceRoot "settings\mcrtx\bridge\McrtxRuntimeSettingFormatter.java"),
    (Join-Path $javaSourceRoot "settings\mcrtx\bridge\McrtxSettingsStore.java"),
    (Join-Path $javaSourceRoot "settings\mcrtx\bridge\McrtxGameplaySettings.java"),
    (Join-Path $javaSourceRoot "settings\mcrtx\bridge\McrtxGraphicsSettings.java"),
    (Join-Path $javaSourceRoot "settings\mcrtx\bridge\McrtxDebugSettings.java"),
    (Join-Path $javaSourceRoot "settings\mcrtx\bridge\McrtxMaterialSettings.java"),
    (Join-Path $javaSourceRoot "settings\mcrtx\bridge\McrtxGameplaySettingsNative.java"),
    (Join-Path $javaSourceRoot "settings\mcrtx\bridge\McrtxGraphicsSettingsNative.java"),
    (Join-Path $javaSourceRoot "settings\mcrtx\bridge\McrtxDebugSettingsNative.java"),
    (Join-Path $javaSourceRoot "settings\mcrtx\bridge\McrtxMaterialSettingsNative.java"),
    (Join-Path $javaSourceRoot "core\mcrtx\bridge\RemixBridgeNative.java"),
    (Join-Path $javaSourceRoot "lifecycle\mcrtx\bridge\RemixLifecycleBridge.java"),
    (Join-Path $javaSourceRoot "lifecycle\mcrtx\bridge\McrtxPerfNative.java"),
    (Join-Path $javaSourceRoot "scene\mcrtx\bridge\RemixSceneBridge.java"),
    (Join-Path $javaSourceRoot "ui\mcrtx\bridge\RemixUiBridge.java"),
    (Join-Path $javaSourceRoot "entities\mcrtx\bridge\RemixDynamicEntityBridge.java"),
    (Join-Path $javaSourceRoot "particles\mcrtx\bridge\RemixParticleOverlayBridge.java"),
    (Join-Path $javaSourceRoot "chunks\mcrtx\bridge\RemixChunkBridge.java"),
    (Join-Path $javaSourceRoot "core\mcrtx\bridge\HookProfiler.java"),
    (Join-Path $javaSourceRoot "platform\mcrtx\bridge\MinecraftPlatformKey.java"),
    (Join-Path $javaSourceRoot "platform\mcrtx\bridge\MinecraftPlatform.java"),
    (Join-Path $javaSourceRoot "platform\mcrtx\bridge\MinecraftPlatformRuntime.java"),
    (Join-Path $javaSourceRoot "platform\mcrtx\bridge\Lwjgl2MinecraftPlatform.java"),
    (Join-Path $javaSourceRoot "platform\mcrtx\bridge\Lwjgl3MinecraftPlatform.java"),
    (Join-Path $javaSourceRoot "platform\mcrtx\bridge\LwjglWindowHandleResolver.java"),
    (Join-Path $javaSourceRoot "platform\mcrtx\lwjglshim\LegacyAL10.java"),
    (Join-Path $javaSourceRoot "platform\mcrtx\lwjglshim\OpenAlCompat.java"),
    (Join-Path $javaSourceRoot "platform\mcrtx\lwjglshim\OpenGlCompat.java"),
    (Join-Path $javaSourceRoot "ui\mcrtx\bridge\UiOverlayCapture.java"),
    (Join-Path $javaSourceRoot "platform\paulscode\sound\libraries\LibraryLWJGLOpenAL.java")
)

$compatSourceFiles = @(
    (Join-Path $javaSourceRoot "platform\mcrtx\lwjglshim\GlfwBindings.java"),
    (Join-Path $javaSourceRoot "platform\mcrtx\lwjglshim\LegacyARBOcclusionQuery.java"),
    (Join-Path $javaSourceRoot "platform\mcrtx\lwjglshim\LegacyGL11.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\LWJGLException.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\Sys.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\opengl\DisplayMode.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\opengl\Display.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\opengl\ContextCapabilities.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\opengl\GL11.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\opengl\GLContext.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\input\Controllers.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\input\Cursor.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\input\Keyboard.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\input\Mouse.java"),
    (Join-Path $javaSourceRoot "platform\org\lwjgl\util\glu\GLU.java")
)

$toolSourceFiles = @(
    (Join-Path $toolSourceRoot "patcher\mcrtx\tools\ClientPatchTool.java")
)

$requiredPaths = @($MinecraftJar, $PatchSourceJar, $LwjglJar, $LwjglUtilJar, $AsmJar, $AsmTreeJar, $JavacPath, $JavaPath, $JarPath) + $runtimeSourceFiles + $compatSourceFiles + $toolSourceFiles
foreach ($path in $requiredPaths) {
    if (-not (Test-Path $path)) {
        throw "Required path not found: $path"
    }
}

Write-Host "Using PrismLauncher root: $PrismRoot"
Write-Host "Using PrismLauncher instance: $InstanceName"
Write-Host "Using Java toolchain: $($javaToolchain.Version) at $JavaHome"

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

& $JavacPath --release 8 -Xlint:-options -cp $classesDir -d $classesDir $compatSourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "javac compatibility compile failed with exit code $LASTEXITCODE"
}

& $JavacPath -cp $toolClasspath -d $toolClassesDir $toolSourceFiles
if ($LASTEXITCODE -ne 0) {
    throw "javac tool compile failed with exit code $LASTEXITCODE"
}

& $JavaPath -cp ($toolClassesDir + ';' + $toolClasspath) mcrtx.tools.ClientPatchTool $PatchSourceJar $patchedJarTemp
if ($LASTEXITCODE -ne 0) {
    throw "bytecode patch tool failed with exit code $LASTEXITCODE"
}

Move-Item -Force $patchedJarTemp $patchedJar
& $JarPath uf $patchedJar -C $classesDir .
if ($LASTEXITCODE -ne 0) {
    throw "jar update failed with exit code $LASTEXITCODE"
}

Export-ZipEntryFile -ArchivePath $PatchSourceJar -EntryName "particles.png" -DestinationPath (Join-Path $assetsDir "particles.png")
Export-ZipEntryFile -ArchivePath $PatchSourceJar -EntryName "terrain.png" -DestinationPath (Join-Path $assetsDir "terrain.png")
Replace-TerrainFireTiles -TerrainPngPath (Join-Path $assetsDir "terrain.png") -FirePngPath (Join-Path $assetsDir "fire.png")
Replace-TerrainLiquidTiles -TerrainPngPath (Join-Path $assetsDir "terrain.png") -WaterPngPath (Join-Path $assetsDir "water.png") -WaterNormalPngPath (Join-Path $assetsDir "water_normal.png") -LavaPngPath (Join-Path $assetsDir "lava.png") -LavaEmissivePngPath (Join-Path $assetsDir "lava_emissive.png")
New-PortalAtlas -PortalPngPath (Join-Path $assetsDir "portal.png")
New-RedstoneEmissiveAtlas -TerrainPngPath (Join-Path $assetsDir "terrain.png") -RedstoneEmissivePngPath (Join-Path $assetsDir "redstone_emissive.png")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "terrain.png") -DestinationDdsPath (Join-Path $assetsDir "terrain.dds")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "redstone_emissive.png") -DestinationDdsPath (Join-Path $assetsDir "redstone_emissive.dds")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "fire.png") -DestinationDdsPath (Join-Path $assetsDir "fire.dds")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "portal.png") -DestinationDdsPath (Join-Path $assetsDir "portal.dds")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "water.png") -DestinationDdsPath (Join-Path $assetsDir "water.dds")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "water_normal.png") -DestinationDdsPath (Join-Path $assetsDir "water_normal.dds")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "lava.png") -DestinationDdsPath (Join-Path $assetsDir "lava.dds")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "lava_emissive.png") -DestinationDdsPath (Join-Path $assetsDir "lava_emissive.dds")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "particles.png") -DestinationDdsPath (Join-Path $assetsDir "particles.dds")
Export-ZipEntryFile -ArchivePath $PatchSourceJar -EntryName "terrain/sun.png" -DestinationPath (Join-Path $assetsDir "sun.png")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "sun.png") -DestinationDdsPath (Join-Path $assetsDir "sun.dds")
Export-ZipEntryFile -ArchivePath $PatchSourceJar -EntryName "terrain/moon.png" -DestinationPath (Join-Path $assetsDir "moon.png")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "moon.png") -DestinationDdsPath (Join-Path $assetsDir "moon.dds")
Export-ZipEntryFile -ArchivePath $PatchSourceJar -EntryName "gui/items.png" -DestinationPath (Join-Path $assetsDir "gui\items.png")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "gui\items.png") -DestinationDdsPath (Join-Path $assetsDir "gui\items.dds")
Export-ZipEntryFile -ArchivePath $PatchSourceJar -EntryName "environment/rain.png" -DestinationPath (Join-Path $assetsDir "rain.png")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "rain.png") -DestinationDdsPath (Join-Path $assetsDir "rain.dds")
Export-ZipEntryFile -ArchivePath $PatchSourceJar -EntryName "environment/clouds.png" -DestinationPath (Join-Path $assetsDir "clouds.png")
Convert-PngToDds -SourcePngPath (Join-Path $assetsDir "clouds.png") -DestinationDdsPath (Join-Path $assetsDir "clouds.dds")
Export-ZipEntriesByPrefix -ArchivePath $PatchSourceJar -Prefixes @('mob/', 'armor/') -DestinationRoot (Join-Path $assetsDir 'entities') -ConvertToDds
Export-ZipEntriesByPrefix -ArchivePath $PatchSourceJar -Prefixes @('art/') -DestinationRoot $assetsDir -ConvertToDds
Export-ZipEntriesByPrefix -ArchivePath $PatchSourceJar -Prefixes @('item/') -DestinationRoot $assetsDir -ConvertToDds
Export-ZipEntriesByPrefix -ArchivePath $PatchSourceJar -Prefixes @('font/') -DestinationRoot $assetsDir -ConvertToDds

if (-not (Test-Path $nativeDll)) {
    throw "Native DLL not found at $nativeDll. Build it first with: cmake --build build --config $Configuration --target mcrtx_jni"
}

Copy-Item $nativeDll (Join-Path $OutputRoot "mcrtx_jni.dll") -Force
if (Test-Path $nativePdb) {
    Copy-Item $nativePdb (Join-Path $OutputRoot "mcrtx_jni.pdb") -Force
} elseif (Test-Path (Join-Path $OutputRoot "mcrtx_jni.pdb")) {
    Remove-Item (Join-Path $OutputRoot "mcrtx_jni.pdb") -Force
}

Write-Host "Patched client bundle ready: $OutputRoot"
Write-Host "Patched jar: $patchedJar"
if (Test-Path $nativePdb) {
    Write-Host "Patched native symbols: $(Join-Path $OutputRoot 'mcrtx_jni.pdb')"
}
Write-Host "Compile baseline jar: $MinecraftJar"
Write-Host "Patch source jar: $PatchSourceJar"
Write-Host "Extracted and converted terrain atlas (with generated Beta fire texels)"
Write-Host "Generated and converted redstone emissive atlas"
Write-Host "Generated and converted fire animation atlas"
Write-Host "Generated and converted portal animation atlas"
Write-Host "Generated and converted water animation atlas"
Write-Host "Generated and converted lava animation atlas"
Write-Host "Generated and converted lava emissive atlas"
Write-Host "Extracted and converted particles atlas"
Write-Host "Extracted and converted GUI item atlas"
Write-Host "Extracted and converted rain texture"
Write-Host "Extracted and converted cloud texture"
Write-Host "Extracted entity, art, item, and font texture directories"
