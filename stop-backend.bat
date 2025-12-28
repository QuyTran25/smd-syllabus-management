@echo off
chcp 65001 > nul
echo ========================================
echo   Stopping SMD Backend Services
echo ========================================
echo.

taskkill /F /IM java.exe > nul 2>&1

if %errorlevel% equ 0 (
    echo ✓ All Java processes stopped
) else (
    echo ℹ No Java processes were running
)

echo.
echo Done!
timeout /t 2 > nul
