# SMD Syllabus Management System - Step by Step Startup
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SMD Syllabus Management System" -ForegroundColor Cyan
Write-Host "  Step-by-Step Startup Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check Docker containers
Write-Host "[1/5] Checking infrastructure services..." -ForegroundColor Yellow
$postgresStatus = docker ps --filter "name=smd-postgres" --format "{{.Status}}"
$redisStatus = docker ps --filter "name=smd-redis" --format "{{.Status}}"
$rabbitmqStatus = docker ps --filter "name=smd-rabbitmq" --format "{{.Status}}"

if (!$postgresStatus -or !$redisStatus -or !$rabbitmqStatus) {
    Write-Host "  Starting infrastructure services..." -ForegroundColor Yellow
    docker start smd-postgres smd-redis smd-rabbitmq 2>$null
    Write-Host "  Waiting for services to be healthy..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
}

Write-Host "  ✓ PostgreSQL: $postgresStatus" -ForegroundColor Green
Write-Host "  ✓ Redis: $redisStatus" -ForegroundColor Green
Write-Host "  ✓ RabbitMQ: $rabbitmqStatus" -ForegroundColor Green
Write-Host ""

# Step 2: Check Frontend dependencies
Write-Host "[2/5] Checking frontend dependencies..." -ForegroundColor Yellow
if (!(Test-Path "frontend\node_modules")) {
    Write-Host "  Installing npm packages..." -ForegroundColor Yellow
    Push-Location frontend
    npm install
    Pop-Location
} else {
    Write-Host "  ✓ Dependencies already installed" -ForegroundColor Green
}

# Clean Vite cache to prevent "504 Outdated Optimize Dep" error
Write-Host "  Cleaning Vite cache..." -ForegroundColor Yellow
if (Test-Path "frontend\node_modules\.vite") {
    Remove-Item -Recurse -Force "frontend\node_modules\.vite" -ErrorAction SilentlyContinue
}
Write-Host "  ✓ Vite cache cleared" -ForegroundColor Green
Write-Host ""

# Step 3: Start Core Service
Write-Host "[3/5] Starting Core Service on port 8081..." -ForegroundColor Yellow
$coreRunning = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue
if ($coreRunning) {
    Write-Host "  ✓ Core Service already running on port 8081" -ForegroundColor Green
} else {
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d $PSScriptRoot\backend\core-service && java -jar target\core-service-1.0.0.jar" -WindowStyle Normal
    Write-Host "  Waiting for Core Service to start (up to 60 seconds)..." -ForegroundColor Yellow
    $maxRetries = 12
    $retryCount = 0
    $coreRunning = $false
    while (-not $coreRunning -and $retryCount -lt $maxRetries) {
        Start-Sleep -Seconds 5
        $retryCount++
        $coreRunning = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue
        if (-not $coreRunning) {
            Write-Host "    Waiting... ($($retryCount * 5)s)" -ForegroundColor Gray
        }
    }
    if ($coreRunning) {
        Write-Host "  ✓ Core Service is running on port 8081" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Core Service failed to start after 60 seconds" -ForegroundColor Red
    }
}
Write-Host ""

# Step 4: Start Gateway
Write-Host "[4/5] Starting Gateway on port 8888..." -ForegroundColor Yellow
$gatewayRunning = Get-NetTCPConnection -LocalPort 8888 -State Listen -ErrorAction SilentlyContinue
if ($gatewayRunning) {
    Write-Host "  ✓ Gateway already running on port 8888" -ForegroundColor Green
} else {
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d $PSScriptRoot\backend\gateway && java -jar target\gateway-1.0.0.jar" -WindowStyle Normal
    Write-Host "  Waiting for Gateway to start (up to 30 seconds)..." -ForegroundColor Yellow
    $maxRetries = 6
    $retryCount = 0
    $gatewayRunning = $false
    while (-not $gatewayRunning -and $retryCount -lt $maxRetries) {
        Start-Sleep -Seconds 5
        $retryCount++
        $gatewayRunning = Get-NetTCPConnection -LocalPort 8888 -State Listen -ErrorAction SilentlyContinue
        if (-not $gatewayRunning) {
            Write-Host "    Waiting... ($($retryCount * 5)s)" -ForegroundColor Gray
        }
    }
    if ($gatewayRunning) {
        Write-Host "  ✓ Gateway is running on port 8888" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Gateway failed to start after 30 seconds" -ForegroundColor Red
    }
}
Write-Host ""

# Step 5: Start Frontend (Admin + Student)
Write-Host "[5/5] Starting Frontend (Admin port 3000, Student port 3001)..." -ForegroundColor Yellow
$frontendAdminRunning = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue
if ($frontendAdminRunning) {
    Write-Host "  ✓ Frontend already running on port 3000" -ForegroundColor Green
} else {
    Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d $PSScriptRoot\frontend && npm run dev" -WindowStyle Normal
    Write-Host "  Waiting for Frontend to start..." -ForegroundColor Yellow
    Start-Sleep -Seconds 10
    $frontendAdminRunning = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue
    if ($frontendAdminRunning) {
        Write-Host "  ✓ Admin Frontend is running on port 3000" -ForegroundColor Green
    } else {
        Write-Host "  ✗ Admin Frontend failed to start" -ForegroundColor Red
    }
}
$frontendStudentRunning = Get-NetTCPConnection -LocalPort 3001 -State Listen -ErrorAction SilentlyContinue
if ($frontendStudentRunning) {
    Write-Host "  ✓ Student Frontend is running on port 3001" -ForegroundColor Green
} else {
    Write-Host "  ✗ Student Frontend failed to start" -ForegroundColor Red
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Services Status Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Infrastructure:" -ForegroundColor White
Write-Host "    PostgreSQL:  http://localhost:5432" -ForegroundColor White
Write-Host "    Redis:       http://localhost:6379" -ForegroundColor White
Write-Host "    RabbitMQ:    http://localhost:15672" -ForegroundColor White
Write-Host ""
Write-Host "  Application:" -ForegroundColor White
Write-Host "    Core Service:     http://localhost:8081" -ForegroundColor White
Write-Host "    Gateway:          http://localhost:8888" -ForegroundColor White
Write-Host "    Admin Frontend:   http://localhost:3000" -ForegroundColor Green
Write-Host "    Student Frontend: http://localhost:3001" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Opening browsers..." -ForegroundColor Yellow
Start-Sleep -Seconds 3
# Open both Admin and Student portals
Start-Process "http://localhost:3000"
Start-Process "http://localhost:3001"
Write-Host "  → Admin Portal: http://localhost:3000" -ForegroundColor Cyan
Write-Host "  → Student Portal: http://localhost:3001" -ForegroundColor Cyan
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  All services are running!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "To stop all services, run:" -ForegroundColor Yellow
Write-Host "  .\stop-backend.bat" -ForegroundColor White
Write-Host ""
