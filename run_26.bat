@echo off
echo Building Blocky Outline mod...
call .\gradlew build -x test
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)
echo Launching Minecraft Fabric 26.1.2...
python run_test_26.py
