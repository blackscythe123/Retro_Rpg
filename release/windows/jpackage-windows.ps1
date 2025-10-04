param(
    [string]$Input = "..\bin",
    [string]$MainJar = "..\RetroGame.jar",
    [string]$MainClass = "utils.RetroConsole",
    [string]$Name = "RetroGame",
    [string]$Vendor = "RetroRpg",
    [string]$Dest = "..\release-output"
)

# Ensure jpackage is available
$jpackage = "jpackage"
if (-not (Get-Command $jpackage -ErrorAction SilentlyContinue)) {
    Write-Error "jpackage not found on PATH. Install JDK 14+ or JDK 17+ and ensure jpackage is available."
    exit 1
}

New-Item -ItemType Directory -Force -Path $Dest | Out-Null

$jpackageArgs = @(
    "--input", $Input,
    "--main-jar", $MainJar,
    "--main-class", $MainClass,
    "--name", $Name,
    "--app-version", "1.0",
    "--vendor", $Vendor,
    "--dest", $Dest,
    "--type", "exe"
)

# if icon exists in current windows folder, include it
$iconPath = Join-Path -Path (Get-Location) -ChildPath "retro_rpg.ico"
if (Test-Path $iconPath) {
    $jpackageArgs += @("--icon", $iconPath)
    Write-Host "Using icon: $iconPath"
} else {
    Write-Host "No Windows icon found at: $iconPath (optional)"
}

Write-Host "Running jpackage with args:`n$jpackageArgs"
$jpackage @jpackageArgs

Write-Host "Done. Output in: $Dest"
