Add-Type -AssemblyName System.Drawing

$sourcePath = "app/src/main/icon-source.png"
if (-not (Test-Path $sourcePath)) {
    throw "Source icon not found: $sourcePath"
}

$legacySizes = @{
    mdpi = 48
    hdpi = 72
    xhdpi = 96
    xxhdpi = 144
    xxxhdpi = 192
}

$adaptiveSizes = @{
    mdpi = 108
    hdpi = 162
    xhdpi = 216
    xxhdpi = 324
    xxxhdpi = 432
}

function Save-Resized([System.Drawing.Bitmap]$image, [int]$size, [string]$outputPath) {
    $bitmap = New-Object System.Drawing.Bitmap($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
    $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $graphics.DrawImage($image, 0, 0, $size, $size)
    $graphics.Dispose()
    $bitmap.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

function Save-SolidBackground([int]$size, [string]$outputPath) {
    $bitmap = New-Object System.Drawing.Bitmap($size, $size, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([System.Drawing.Color]::FromArgb(255, 31, 62, 103))
    $graphics.Dispose()
    $bitmap.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

$source = [System.Drawing.Bitmap]::FromFile($sourcePath)
$side = [Math]::Min($source.Width, $source.Height)
$offsetX = [int](($source.Width - $side) / 2)
$offsetY = [int](($source.Height - $side) / 2)

$square = New-Object System.Drawing.Bitmap($side, $side, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$cropGraphics = [System.Drawing.Graphics]::FromImage($square)
$cropGraphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality
$cropGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$cropGraphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
$destinationRect = New-Object System.Drawing.Rectangle(0, 0, $side, $side)
$sourceRect = New-Object System.Drawing.Rectangle($offsetX, $offsetY, $side, $side)
$cropGraphics.DrawImage($source, $destinationRect, $sourceRect, [System.Drawing.GraphicsUnit]::Pixel)
$cropGraphics.Dispose()

Save-Resized -image $square -size 512 -outputPath "app/src/main/ic_launcher-playstore.png"

foreach ($density in $legacySizes.Keys) {
    $dir = "app/src/main/res/mipmap-$density"
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir | Out-Null
    }

    Get-ChildItem $dir -File -Include "ic_launcher*.webp", "ic_launcher*.png" -ErrorAction SilentlyContinue | Remove-Item -Force

    Save-Resized -image $square -size $legacySizes[$density] -outputPath (Join-Path $dir "ic_launcher.png")
    Save-Resized -image $square -size $legacySizes[$density] -outputPath (Join-Path $dir "ic_launcher_round.png")
    Save-Resized -image $square -size $adaptiveSizes[$density] -outputPath (Join-Path $dir "ic_launcher_foreground.png")
    Save-SolidBackground -size $adaptiveSizes[$density] -outputPath (Join-Path $dir "ic_launcher_background.png")
}

$source.Dispose()
$square.Dispose()

Write-Output "Icon generation complete."
