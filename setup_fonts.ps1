$ErrorActionPreference = 'Stop'
$fontDir = "$PSScriptRoot\app\src\main\res\font"
if (-not (Test-Path $fontDir)) {
    New-Item -ItemType Directory -Force -Path $fontDir | Out-Null
}

$fonts = @{
    "https://raw.githubusercontent.com/google/fonts/main/ofl/lora/static/Lora-Regular.ttf" = "lora_regular.ttf"
    "https://raw.githubusercontent.com/google/fonts/main/ofl/lora/static/Lora-Medium.ttf" = "lora_medium.ttf"
    "https://raw.githubusercontent.com/google/fonts/main/ofl/lora/static/Lora-SemiBold.ttf" = "lora_semibold.ttf"
    "https://raw.githubusercontent.com/google/fonts/main/ofl/lora/static/Lora-Bold.ttf" = "lora_bold.ttf"
    "https://raw.githubusercontent.com/google/fonts/main/ofl/lora/static/Lora-Italic.ttf" = "lora_italic.ttf"
    "https://raw.githubusercontent.com/google/fonts/main/ofl/lora/static/Lora-BoldItalic.ttf" = "lora_bold_italic.ttf"
    "https://raw.githubusercontent.com/google/fonts/main/ofl/dmsans/static/DMSans-Regular.ttf" = "dmsans_regular.ttf"
    "https://raw.githubusercontent.com/google/fonts/main/ofl/dmsans/static/DMSans-Medium.ttf" = "dmsans_medium.ttf"
    "https://raw.githubusercontent.com/google/fonts/main/ofl/dmsans/static/DMSans-SemiBold.ttf" = "dmsans_semibold.ttf"
    "https://raw.githubusercontent.com/google/fonts/main/ofl/dmsans/static/DMSans-Bold.ttf" = "dmsans_bold.ttf"
}

Write-Host "Downloading fonts to $fontDir..."

foreach ($url in $fonts.Keys) {
    $filename = $fonts[$url]
    $dest = Join-Path $fontDir $filename
    Write-Host "Downloading $filename..."
    Invoke-WebRequest -Uri $url -OutFile $dest
}

Write-Host "Done!"
