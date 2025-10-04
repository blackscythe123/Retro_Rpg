Linux packaging notes

- Linux desktop files typically use PNG icons. Place `retro_rpg.png` in this folder.
- jpackage accepts `--icon` with a PNG for Linux: pass `--icon release/linux/retro_rpg.png` when running jpackage.
- Optionally create .deb/.rpm with the icon included in the package resources.
