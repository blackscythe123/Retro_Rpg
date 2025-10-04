Windows packaging notes

- Place the source icon `retro_rpg.jpeg` in this folder and rename to `retro_rpg.jpeg` (already exists in project root).
- Launch4j requires a .ico file. You can convert the JPEG to ICO using ImageMagick (convert) or an online tool.

Example ImageMagick command (PowerShell):

# from project root
magick convert ..\..\retro_rpg.jpeg -resize 256x256 ..\..\release\windows\retro_rpg.ico

- After creating `retro_rpg.ico`, edit `release/launch4j-config.xml` and set <icon> to `release\windows\retro_rpg.ico`.
- Alternatively, jpackage supports a `--icon` argument on Windows; pass the .ico path when running jpackage.
