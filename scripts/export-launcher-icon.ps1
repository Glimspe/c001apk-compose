$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$projectRoot = Split-Path -Parent $PSScriptRoot
$assetDir = Join-Path $projectRoot 'app/src/main'
[xml]$vector = Get-Content -LiteralPath (Join-Path $assetDir 'res/drawable/ic_launcher_revived_foreground.xml') -Raw
$androidNamespace = 'http://schemas.android.com/apk/res/android'

function New-IconPath([string]$pathData) {
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
    return $path
}

[xml]$colors = Get-Content -LiteralPath (Join-Path $assetDir 'res/values/colors.xml') -Raw
$background = ($colors.resources.color | Where-Object name -eq 'ic_launcher_background').InnerText
# 自适应图标的可视区为前景画布的 2/3，导出时保持相同的图形占比。
$iconScale = [double]::Parse($vector.vector.group.GetAttribute('scaleX', $androidNamespace), [System.Globalization.CultureInfo]::InvariantCulture) * 1.5
$offset = 627 * (1 - $iconScale)
$svgPaths = @()
$bitmap = [System.Drawing.Bitmap]::new(512, 512)
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
try {
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.Clear([System.Drawing.ColorTranslator]::FromHtml($background))
    $graphics.ScaleTransform(512 / 1254, 512 / 1254)
    $graphics.TranslateTransform($offset, $offset)
    $graphics.ScaleTransform($iconScale, $iconScale)
    foreach ($element in $vector.vector.group.path) {
        $pathData = $element.GetAttribute('pathData', $androidNamespace)
        $colorName = $element.GetAttribute('fillColor', $androidNamespace).Replace('@color/', '')
        $color = ($colors.resources.color | Where-Object name -eq $colorName).InnerText
        $path = New-IconPath $pathData
        $brush = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml($color))
        try {
            $graphics.FillPath($brush, $path)
        } finally {
            $brush.Dispose()
            $path.Dispose()
        }
        $svgPaths += "    <path fill=`"$color`" d=`"$pathData`"/>"
    }
    $bitmap.Save((Join-Path $assetDir 'ic_launcher-playstore.png'), [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $graphics.Dispose()
    $bitmap.Dispose()
}

$svgOffset = $offset.ToString('0.####', [System.Globalization.CultureInfo]::InvariantCulture)
$svgScale = $iconScale.ToString('0.####', [System.Globalization.CultureInfo]::InvariantCulture)
$svg = @"
<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 1254 1254">
  <rect width="1254" height="1254" rx="270" fill="$background"/>
  <g transform="translate($svgOffset $svgOffset) scale($svgScale)">
$($svgPaths -join "`n")
  </g>
</svg>
"@
[System.IO.File]::WriteAllText((Join-Path $assetDir 'ic_launcher-revived.svg'), $svg, [System.Text.UTF8Encoding]::new($false))
