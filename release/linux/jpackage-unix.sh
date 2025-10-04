#!/usr/bin/env bash
INPUT_DIR="../bin"
MAIN_JAR="../RetroGame.jar"
MAIN_CLASS="utils.RetroConsole"
APP_NAME="RetroGame"
DEST_DIR="../release-output"

command -v jpackage >/dev/null 2>&1 || { echo >&2 "jpackage not found. Install JDK 14+ or 17+."; exit 1; }

mkdir -p "$DEST_DIR"

jpackage --input "$INPUT_DIR" --main-jar "$MAIN_JAR" --main-class "$MAIN_CLASS" --name "$APP_NAME" --app-version 1.0 --vendor RetroRpg --dest "$DEST_DIR"

echo "Done. Output in $DEST_DIR"
