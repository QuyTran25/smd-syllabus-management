# ========================================
# START AI SERVICE WORKERS
# Khởi động cả 2 workers cho AI Service
# ========================================

Write-Host "🤖 Starting SMD AI Service Workers..." -ForegroundColor Cyan
Write-Host ""

# Kiểm tra Python
Write-Host "📋 Checking Python installation..." -ForegroundColor Yellow
$pythonVersion = python --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Python not found! Please install Python 3.11+" -ForegroundColor Red
    exit 1
}
Write-Host "✅ $pythonVersion" -ForegroundColor Green
Write-Host ""

# Kiểm tra virtual environment
if (-not (Test-Path ".\venv\Scripts\Activate.ps1")) {
    Write-Host "⚠️ Virtual environment not found. Creating..." -ForegroundColor Yellow
    python -m venv venv
    Write-Host "✅ Virtual environment created" -ForegroundColor Green
}

# Activate virtual environment
Write-Host "📦 Activating virtual environment..." -ForegroundColor Yellow
& ".\venv\Scripts\Activate.ps1"
Write-Host "✅ Virtual environment activated" -ForegroundColor Green
Write-Host ""

# Check dependencies
Write-Host "📦 Checking dependencies..." -ForegroundColor Yellow
$pikaInstalled = pip show pika 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️ Dependencies not installed. Installing..." -ForegroundColor Yellow
    pip install -r requirements.txt
    Write-Host "✅ Dependencies installed" -ForegroundColor Green
} else {
    Write-Host "✅ Dependencies OK" -ForegroundColor Green
}
Write-Host ""

# Check .env file
if (-not (Test-Path ".env")) {
    Write-Host "⚠️ .env file not found. Copying from .env.example..." -ForegroundColor Yellow
    Copy-Item .env.example .env
    Write-Host "✅ .env file created" -ForegroundColor Green
    Write-Host ""
}

# Kiểm tra RabbitMQ
Write-Host "🐰 Checking RabbitMQ connection..." -ForegroundColor Yellow
$env:PYTHONPATH = "$PWD"
$testScript = @"
import pika
import sys

try:
    credentials = pika.PlainCredentials('guest', 'guest')
    parameters = pika.ConnectionParameters(
        host='localhost',
        port=5672,
        credentials=credentials,
        connection_attempts=1,
        retry_delay=1,
        socket_timeout=3
    )
    connection = pika.BlockingConnection(parameters)
    connection.close()
    print('✅ RabbitMQ connection OK')
    sys.exit(0)
except Exception as e:
    print(f'❌ Cannot connect to RabbitMQ: {e}')
    sys.exit(1)
"@

$testScript | python
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "❌ RabbitMQ is not running or not accessible!" -ForegroundColor Red
    Write-Host "Please start RabbitMQ first:" -ForegroundColor Yellow
    Write-Host "   docker-compose up -d rabbitmq" -ForegroundColor Cyan
    Write-Host ""
    exit 1
}
Write-Host ""

# Menu chọn worker
Write-Host "================================" -ForegroundColor Cyan
Write-Host "  SELECT WORKER TO START" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host "1. Analysis Worker (MAP_CLO_PLO, COMPARE_VERSIONS)" -ForegroundColor White
Write-Host "2. Summarize Worker (SUMMARIZE_SYLLABUS)" -ForegroundColor White
Write-Host "3. Both Workers (2 terminal windows)" -ForegroundColor White
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

$choice = Read-Host "Enter your choice (1-3)"

switch ($choice) {
    "1" {
        Write-Host ""
        Write-Host "🚀 Starting Analysis Worker..." -ForegroundColor Green
        Write-Host "   Queues: ai_processing_queue" -ForegroundColor Gray
        Write-Host "   Actions: MAP_CLO_PLO, COMPARE_VERSIONS" -ForegroundColor Gray
        Write-Host ""
        python -m app.workers.analysis_worker
    }
    "2" {
        Write-Host ""
        Write-Host "🚀 Starting Summarize Worker..." -ForegroundColor Green
        Write-Host "   Queues: ai_summarize_queue" -ForegroundColor Gray
        Write-Host "   Actions: SUMMARIZE_SYLLABUS" -ForegroundColor Gray
        Write-Host ""
        python -m app.workers.summarize_worker
    }
    "3" {
        Write-Host ""
        Write-Host "🚀 Starting both workers..." -ForegroundColor Green
        Write-Host ""
        
        # Start analysis worker in new terminal
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\venv\Scripts\Activate.ps1; Write-Host '🔬 ANALYSIS WORKER' -ForegroundColor Cyan; python -m app.workers.analysis_worker"
        
        Start-Sleep -Seconds 2
        
        # Start summarize worker in new terminal
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PWD'; .\venv\Scripts\Activate.ps1; Write-Host '📝 SUMMARIZE WORKER' -ForegroundColor Yellow; python -m app.workers.summarize_worker"
        
        Write-Host "✅ Both workers started in separate terminals" -ForegroundColor Green
        Write-Host "   Press Ctrl+C in each terminal to stop" -ForegroundColor Gray
    }
    default {
        Write-Host "❌ Invalid choice!" -ForegroundColor Red
        exit 1
    }
}
