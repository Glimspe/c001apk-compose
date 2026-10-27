$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$projectRoot = Split-Path -Parent $PSScriptRoot
$assetDir = Join-Path $projectRoot 'app/src/main'
[xml]$vector = Get-Content -LiteralPath (Join-Path $assetDir 'res/drawable/ic_launcher_revived_foreground.xml') -Raw
$pathData = $vector.vector.group.path.GetAttribute('pathData', 'http://schemas.android.com/apk/res/android')
$tokens = [regex]::Matches($pathData, '[MLCZ]|-?\d+(?:\.\d+)?') | ForEach-Object Value
$path = [System.Drawing.Drawing2D.GraphicsPath]::new()
$last = [System.Drawing.PointF]::new(0, 0)
$index = 0
while ($index -lt $tokens.Count) {
    $command = $tokens[$index++]
    if ($command -eq 'Z') {
        $path.CloseFigure()
        continue
    }
    $pointCount = if ($command -eq 'C') { 3 } elseif ($command -in 'M', 'L') { 1 } else { throw "Unsupported path command: $command" }
    $points = for ($point = 0; $point -lt $pointCount; $point++) {
        $x = [float]::Parse($tokens[$index++], [System.Globalization.CultureInfo]::InvariantCulture)
        $y = [float]::Parse($tokens[$index++], [System.Globalization.CultureInfo]::InvariantCulture)
        [System.Drawing.PointF]::new($x, $y)
    }
    switch ($command) {
        'M' { $path.StartFigure() }
        'L' { $path.AddLine($last, $points[0]) }
        'C' { $path.AddBezier($last, $points[0], $points[1], $points[2]) }
    }
    $last = $points[-1]
}

[xml]$colors = Get-Content -LiteralPath (Join-Path $assetDir 'res/values/colors.xml') -Raw
$foreground = ($colors.resources.color | Where-Object name -eq 'ic_launcher_foreground').InnerText
$background = ($colors.resources.color | Where-Object name -eq 'ic_launcher_background').InnerText
$bitmap = [System.Drawing.Bitmap]::new(512, 512)
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
$brush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml($foreground))
try {
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.Clear([System.Drawing.ColorTranslator]::FromHtml($background))
    $graphics.ScaleTransform(512 / 1254, 512 / 1254)
    $graphics.FillPath($brush, $path)
    $bitmap.Save((Join-Path $assetDir 'ic_launcher-playstore.png'), [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $brush.Dispose()
    $graphics.Dispose()
    $bitmap.Dispose()
    $path.Dispose()
}

$svg = @"
<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 1254 1254">
  <rect width="1254" height="1254" rx="270" fill="$background"/>
  <path fill="$foreground" d="$pathData"/>
</svg>
"@
[System.IO.File]::WriteAllText((Join-Path $assetDir 'ic_launcher-revived.svg'), $svg, [System.Text.UTF8Encoding]::new($false))
