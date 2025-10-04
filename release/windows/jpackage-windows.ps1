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

Write-Host "Running jpackage with args:" -ForegroundColor Cyan
$jpackageArgs | ForEach-Object { Write-Host "  $_" }

$processInfo = New-Object System.Diagnostics.ProcessStartInfo
$processInfo.FileName = $jpackage
$processInfo.RedirectStandardOutput = $true
$processInfo.RedirectStandardError = $true
$processInfo.UseShellExecute = $false
$processInfo.Arguments = ($jpackageArgs -join ' ')

$process = [System.Diagnostics.Process]::Start($processInfo)
$process.WaitForExit()

if ($process.ExitCode -ne 0) {
    Write-Error "jpackage exited with code $($process.ExitCode)"
    Write-Host $process.StandardOutput.ReadToEnd()
    Write-Host $process.StandardError.ReadToEnd()
    exit $process.ExitCode
}

Write-Host $process.StandardOutput.ReadToEnd()
Write-Host $process.StandardError.ReadToEnd()

Write-Host "Done. Output in: $Dest"
