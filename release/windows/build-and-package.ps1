param(
    [string]$BuildScript = "..\build.bat",
    [string]$BuildJar = ".\build-jar.bat",
    [string]$JPackageScript = ".\jpackage-windows.ps1"
)

Write-Host "Running local build + package sequence"

if (Test-Path $BuildScript) {
    Write-Host "Running project build: $BuildScript"
    & $BuildScript
} else {
    Write-Host "Build script not found: $BuildScript"
}

if (Test-Path $BuildJar) {
    Write-Host "Creating jar: $BuildJar"
    & $BuildJar
} else {
    Write-Host "Jar builder not found: $BuildJar"
}

if (Get-Command magick -ErrorAction SilentlyContinue) {
    Write-Host "Converting icon with ImageMagick"
    magick convert ..\..\retro_rpg.jpeg -resize 256x256 .\retro_rpg.ico
} else {
    Write-Host "ImageMagick not found. Skipping icon conversion."
}

if (Test-Path $JPackageScript) {
    Write-Host "Running jpackage script: $JPackageScript"
    pwsh -File $JPackageScript -Input ..\bin -MainJar ..\RetroGame.jar -MainClass utils.RetroConsole -Name RetroGame -Dest ..\release\release-output
} else {
    Write-Host "jpackage script not found: $JPackageScript"
}

Write-Host "Done."
