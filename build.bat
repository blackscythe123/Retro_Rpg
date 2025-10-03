@echo off
echo Building Retro Gaming Console...

if not exist bin mkdir bin

echo Compiling source files...
javac -d bin -cp src src\interfaces\*.java src\exceptions\*.java src\gameobjects\*.java src\utils\*.java src\consoles\*.java

if %errorlevel% equ 0 (
    echo Build successful!
    echo.
    echo To run the game, use: java -cp bin utils.RetroConsole
    echo.
) else (
    echo Build failed!
)