@echo off
echo Building Blocky Outline mod v1.1.3 for Minecraft 26.x...
python build_26.py
if %ERRORLEVEL% NEQ 0 (
    echo Build failed!
    pause
    exit /b %ERRORLEVEL%
)
echo Launching Minecraft Fabric 26.x...
python run_test_26.py
