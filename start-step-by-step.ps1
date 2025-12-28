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
Write-Host ""

# Step 3: Start Core Service
Write-Host "[3/5] Starting Core Service on port 8081..." -ForegroundColor Yellow
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d $PSScriptRoot\backend\core-service && java -jar target\core-service-1.0.0.jar" -WindowStyle Normal
Write-Host "  Waiting for Core Service to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

$coreRunning = Get-NetTCPConnection -LocalPort 8081 -State Listen -ErrorAction SilentlyContinue
if ($coreRunning) {
    Write-Host "  ✓ Core Service is running on port 8081" -ForegroundColor Green
} else {
    Write-Host "  ✗ Core Service failed to start" -ForegroundColor Red
}
Write-Host ""

# Step 4: Start Gateway
Write-Host "[4/5] Starting Gateway on port 8080..." -ForegroundColor Yellow
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d $PSScriptRoot\backend\gateway && java -jar target\gateway-1.0.0.jar" -WindowStyle Normal
Write-Host "  Waiting for Gateway to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

$gatewayRunning = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($gatewayRunning) {
    Write-Host "  ✓ Gateway is running on port 8080" -ForegroundColor Green
} else {
    Write-Host "  ✗ Gateway failed to start" -ForegroundColor Red
}
Write-Host ""

# Step 5: Start Frontend
Write-Host "[5/5] Starting Frontend on port 3000..." -ForegroundColor Yellow
Start-Process -FilePath "cmd.exe" -ArgumentList "/k", "cd /d $PSScriptRoot\frontend && npm run dev:admin" -WindowStyle Normal
Write-Host "  Waiting for Frontend to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 8

$frontendRunning = Get-NetTCPConnection -LocalPort 3000 -State Listen -ErrorAction SilentlyContinue
if ($frontendRunning) {
    Write-Host "  ✓ Frontend is running on port 3000" -ForegroundColor Green
} else {
    Write-Host "  ✗ Frontend failed to start" -ForegroundColor Red
}
Write-Host ""

# Summary
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Services Status Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Infrastructure:" -ForegroundColor White
Write-Host "    PostgreSQL:  http://localhost:5432" -ForegroundColor White
Write-Host "    Redis:       http://localhost:6379" -ForegroundColor White
Write-Host "    RabbitMQ:    http://localhost:15673" -ForegroundColor White
Write-Host ""
Write-Host "  Application:" -ForegroundColor White
Write-Host "    Core Service:  http://localhost:8081" -ForegroundColor White
Write-Host "    Gateway:       http://localhost:8080" -ForegroundColor White
Write-Host "    Frontend:      http://localhost:3000" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Opening browser..." -ForegroundColor Yellow
Start-Sleep -Seconds 3
Start-Process "http://localhost:3000"
Write-Host ""
Write-Host "Press Enter to stop all services..." -ForegroundColor Yellow
Read-Host

# Cleanup
Write-Host "Stopping all services..." -ForegroundColor Yellow
Stop-Process -Name java -Force -ErrorAction SilentlyContinue
Stop-Process -Name node -Force -ErrorAction SilentlyContinue
Write-Host "All services stopped." -ForegroundColor Green
