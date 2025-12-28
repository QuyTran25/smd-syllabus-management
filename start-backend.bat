@echo off
chcp 65001 > nul
echo ========================================
echo   SMD Syllabus Management System
echo ========================================
echo.

REM Kill existing processes
echo [1/6] Stopping existing services...
taskkill /F /IM java.exe > nul 2>&1
taskkill /F /IM node.exe > nul 2>&1
timeout /t 2 > nul

REM Start Core Service in new window
echo [2/6] Starting Core Service on port 8081...
start "SMD Core Service" cmd /k "cd /d %~dp0backend\core-service && java -jar target\core-service-1.0.0.jar"
echo Core Service starting... (waiting 15 seconds)
timeout /t 15 > nul

REM Start Gateway in new window
echo [3/6] Starting Gateway on port 8080...
start "SMD Gateway" cmd /k "cd /d %~dp0backend\gateway && java -jar target\gateway-1.0.0.jar"
echo Gateway starting... (waiting 8 seconds)
timeout /t 8 > nul

REM Start Frontend in new window
echo [4/6] Starting Frontend on port 3000...
start "SMD Frontend" cmd /k "cd /d %~dp0frontend && npm run dev"
echo Frontend starting... (waiting 5 seconds)
timeout /t 5 > nul

REM Verify services
echo [5/6] Verifying services...
netstat -ano | findstr ":8080.*LISTENING" > nul
if %errorlevel% equ 0 (
    echo ✓ Gateway is running on port 8080
) else (
    echo ✗ Gateway failed to start on port 8080
)

netstat -ano | findstr ":8081.*LISTENING" > nul
if %errorlevel% equ 0 (
    echo ✓ Core Service is running on port 8081
) else (
    echo ✗ Core Service failed to start on port 8081
)

echo.
echo [6/6] All services started!
echo ========================================
echo   Core Service:  http://localhost:8081
echo   Gateway:       http://localhost:8080
echo   Frontend:      http://localhost:3000
echo ========================================
echo.
echo ℹ Three windows opened:
echo   - SMD Core Service (Java)
echo   - SMD Gateway (Java)
echo   - SMD Frontend (Node.js)
echo ========================================
echo.
taskkill /F /IM node.exe > nul 2>&1
echo Press any key to stop all services...
pause > nul

echo.
echo Stopping all services...
taskkill /F /IM java.exe > nul 2>&1
echo All services stopped.
timeout /t 2 > nul
