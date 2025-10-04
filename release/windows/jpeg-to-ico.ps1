$src = Join-Path -Path (Resolve-Path "..\..\retro_rpg.jpeg") -ChildPath ''
$dest = Join-Path -Path (Get-Location) -ChildPath "retro_rpg.ico"

if (Get-Command magick -ErrorAction SilentlyContinue) {
    Write-Host "Converting jpeg to ico using ImageMagick..."
    magick convert ..\..\retro_rpg.jpeg -resize 256x256 $dest
    Write-Host "Wrote: $dest"
} else {
    Write-Host "ImageMagick (magick) not found. Install ImageMagick or convert manually using an online tool."
    Write-Host "Example (on macOS or with ImageMagick): magick convert retro_rpg.jpeg -resize 256x256 retro_rpg.ico"
}
