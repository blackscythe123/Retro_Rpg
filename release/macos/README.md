macOS packaging notes

- macOS expects an ICNS icon for `.app` bundles. Convert `retro_rpg.jpeg` to `.icns` using `iconutil` (macOS) or ImageMagick + `iconutil`.

Example flow on macOS:

1) Convert jpeg to png variants and create an Icon.iconset folder:
   sips -z 16 16 retro_rpg.jpeg --out icon_16x16.png
   sips -z 32 32 retro_rpg.jpeg --out icon_16x16@2x.png
   ...
2) Place PNGs into `Retro.iconset` and run:
   iconutil -c icns Retro.iconset

- jpackage accepts `--icon` with a .icns file on macOS: pass `--icon release/macos/retro_rpg.icns` when running jpackage.
