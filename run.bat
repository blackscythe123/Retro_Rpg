@echo off
if not exist bin (
    echo Please build the project first using build.bat
    pause
    exit /b 1
)

echo Starting Retro Gaming Console...
java -cp bin utils.RetroConsole