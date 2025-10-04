@echo off
REM Build a runnable jar using the project's compiled classes in bin\
setlocal
set JAR_NAME=RetroGame.jar

if not exist ..\..\bin goto NO_BIN

if exist "..\..\%JAR_NAME%" del "..\..\%JAR_NAME%"

REM Create jar: c=create, f=file, m=manifest
jar cfm "..\..\%JAR_NAME%" "..\MANIFEST.MF" -C ..\..\bin .
if errorlevel 1 goto JAR_FAILED

echo Created %JAR_NAME% in project root.
if /I "%CI%"=="true" (
	endlocal
	goto :EOF
)
pause
endlocal
goto :EOF

:NO_BIN
echo Compiled classes not found in bin - run build.bat first.
if /I "%CI%"=="true" (
	endlocal
	exit /b 0
)
pause
endlocal
exit /b 1

:JAR_FAILED
echo Failed to create jar. Ensure the JDK jar tool is on PATH (for example: C:\Program Files\Java\jdk...\bin).
if /I "%CI%"=="true" (
	endlocal
	exit /b 1
)
pause
endlocal
exit /b 1
