@echo off
echo Building Blocky Outline mod v1.1.4 for Minecraft 1.21.11...
call gradlew.bat build
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)
echo Build completed successfully! Output: dist\blocky-outline-1.1.4.jar
