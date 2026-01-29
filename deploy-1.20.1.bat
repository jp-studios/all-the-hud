@echo off
REM Quick deploy script for 1.20.1 testing
cd /d "%~dp0"

echo Switching to 1.20.1 properties...
copy /Y gradle-1.20.1.properties gradle.properties >nul

echo Building mod...
call .\gradlew build -q
if errorlevel 1 (
    echo Build failed!
    exit /b 1
)

echo Copying to Prism instance...
copy /Y "build\libs\allthehud-0.1.2.jar" "C:\Users\johnn\AppData\Roaming\PrismLauncher\instances\AllTheHUD-Dev-1.20.1\minecraft\mods\allthehud-0.1.2.jar"

echo.
echo Done! JAR deployed to Prism 1.20.1 instance.
echo Launch "AllTheHUD-Dev-1.20.1" in Prism to test.
