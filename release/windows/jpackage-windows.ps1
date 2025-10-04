param(
    [string]$MainJar = "..\..\RetroGame.jar",
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

$mainJarPath = Resolve-Path $MainJar -ErrorAction Stop
$mainJarName = Split-Path -Path $mainJarPath -Leaf

$stagingInput = Join-Path -Path (Resolve-Path $Dest).Path -ChildPath "windows-input"
if (Test-Path $stagingInput) {
    Remove-Item -Recurse -Force $stagingInput
}
New-Item -ItemType Directory -Force -Path $stagingInput | Out-Null
Copy-Item -Force $mainJarPath -Destination (Join-Path -Path $stagingInput -ChildPath $mainJarName)

$jpackageArgs = @(
    "--input", $stagingInput,
    "--main-jar", $mainJarName,
    "--main-class", $MainClass,
    "--name", $Name,
    "--app-version", "1.0",
    "--vendor", $Vendor,
    "--dest", $Dest,
<<<<<<< HEAD
    "--type", "exe"
=======
    "--type", "exe",
    "--win-per-user-install",
    "--win-shortcut",
    "--win-menu",
    "--win-menu-group", $Vendor
>>>>>>> c184fe7 (Add writable directories and improve crash handling)
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
$quotedArgs = $jpackageArgs | ForEach-Object {
    if ($_ -match '\s') {
        '"{0}"' -f $_
    } else {
        $_
    }
}
$processInfo.Arguments = [string]::Join(' ', $quotedArgs)
$processInfo.RedirectStandardOutput = $true
$processInfo.RedirectStandardError = $true
$processInfo.UseShellExecute = $false

$process = [System.Diagnostics.Process]::Start($processInfo)
$process.WaitForExit()

$stdOut = $process.StandardOutput.ReadToEnd()
$stdErr = $process.StandardError.ReadToEnd()

if ($stdOut) { Write-Host $stdOut }
if ($stdErr) { Write-Host $stdErr }

if ($process.ExitCode -ne 0) {
    Write-Error "jpackage exited with code $($process.ExitCode)"
    exit $process.ExitCode
}

Write-Host "Done. Output in: $Dest"
