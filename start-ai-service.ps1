# ========================================
# SMD AI Service Standalone Startup Script
# Khởi động AI Service độc lập
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SMD AI Service Starter" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if RabbitMQ is running
Write-Host "[1/4] Checking RabbitMQ status..." -ForegroundColor Yellow
$rabbitmqStatus = docker ps --filter "name=smd-rabbitmq" --format "{{.Status}}"

if (!$rabbitmqStatus) {
    Write-Host "  [X] RabbitMQ is not running!" -ForegroundColor Red
    Write-Host "  Please start infrastructure first:" -ForegroundColor Yellow
    Write-Host "    docker-compose up -d" -ForegroundColor White
    Write-Host ""
    exit 1
} else {
    Write-Host "  [OK] RabbitMQ: $rabbitmqStatus" -ForegroundColor Green
}
Write-Host ""

# Check Python installation
Write-Host "[2/4] Checking Python..." -ForegroundColor Yellow
$pythonVersion = python --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "  [X] Python not found! Please install Python 3.11+" -ForegroundColor Red
    exit 1
}
Write-Host "  [OK] $pythonVersion" -ForegroundColor Green
Write-Host ""

# Navigate to AI service directory
Push-Location "$PSScriptRoot\backend\ai-service"

# Check virtual environment
Write-Host "[3/4] Setting up Python virtual environment..." -ForegroundColor Yellow
if (-not (Test-Path ".\venv\Scripts\Activate.ps1")) {
    Write-Host "  Creating virtual environment..." -ForegroundColor Yellow
    python -m venv venv
    Write-Host "  [OK] Virtual environment created" -ForegroundColor Green
} else {
    Write-Host "  [OK] Virtual environment already exists" -ForegroundColor Green
}

# Activate virtual environment
& ".\venv\Scripts\Activate.ps1"

# Install dependencies
Write-Host "  Checking dependencies..." -ForegroundColor Yellow
$pikaInstalled = pip show pika 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "  Installing dependencies..." -ForegroundColor Yellow
    pip install -r requirements.txt --quiet
    Write-Host "  [OK] Dependencies installed" -ForegroundColor Green
} else {
    Write-Host "  [OK] Dependencies already installed" -ForegroundColor Green
}

# Check .env file
if (-not (Test-Path ".env")) {
    Write-Host "  Creating .env file from template..." -ForegroundColor Yellow
    Copy-Item .env.example .env
    Write-Host "  [OK] .env file created" -ForegroundColor Green
    Write-Host ""
    Write-Host "  ⚠️  IMPORTANT: Please configure your Gemini API Key in:" -ForegroundColor Yellow
    Write-Host "     backend\ai-service\.env" -ForegroundColor White
    Write-Host ""
}
Write-Host ""

# Start AI Workers
Write-Host "[4/4] Starting AI Service Workers..." -ForegroundColor Yellow
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  SELECT WORKER TO START" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "1. Analysis Worker (So sánh phiên bản, CLO-PLO mapping)" -ForegroundColor White
Write-Host "2. Summarize Worker (Tóm tắt đề cương bằng AI)" -ForegroundColor White
Write-Host "3. Both Workers (Khuyến nghị - chạy 2 cửa sổ)" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$choice = Read-Host "Enter your choice (1-3)"

Write-Host ""

switch ($choice) {
    "1" {
        Write-Host "🔬 Starting Analysis Worker..." -ForegroundColor Cyan
        Write-Host "   Queues: ai_processing_queue" -ForegroundColor Gray
        Write-Host "   Actions: MAP_CLO_PLO, COMPARE_VERSIONS" -ForegroundColor Gray
        Write-Host ""
        python -m app.workers.analysis_worker
    }
    "2" {
        Write-Host "📝 Starting Summarize Worker..." -ForegroundColor Yellow
        Write-Host "   Queues: ai_summarize_queue" -ForegroundColor Gray
        Write-Host "   Actions: SUMMARIZE_SYLLABUS" -ForegroundColor Gray
        Write-Host ""
        python -m app.workers.summarize_worker
    }
    "3" {
        Write-Host "🚀 Starting both workers in separate terminals..." -ForegroundColor Green
        Write-Host ""
        
        # Start analysis worker in new terminal
        $analysisCmd = "cd '$PSScriptRoot\backend\ai-service'; .\venv\Scripts\Activate.ps1; Write-Host '🔬 ANALYSIS WORKER' -ForegroundColor Cyan; Write-Host 'Queues: ai_processing_queue' -ForegroundColor Gray; Write-Host ''; python -m app.workers.analysis_worker"
        Start-Process powershell -ArgumentList "-NoExit", "-Command", $analysisCmd
        
        Start-Sleep -Seconds 2
        
        # Start summarize worker in new terminal  
        $summarizeCmd = "cd '$PSScriptRoot\backend\ai-service'; .\venv\Scripts\Activate.ps1; Write-Host '📝 SUMMARIZE WORKER' -ForegroundColor Yellow; Write-Host 'Queues: ai_summarize_queue' -ForegroundColor Gray; Write-Host ''; python -m app.workers.summarize_worker"
        Start-Process powershell -ArgumentList "-NoExit", "-Command", $summarizeCmd
        
        Write-Host "✅ Both workers started in separate terminals" -ForegroundColor Green
        Write-Host "   - Analysis Worker: So sánh phiên bản, CLO-PLO mapping" -ForegroundColor Gray
        Write-Host "   - Summarize Worker: Tóm tắt đề cương bằng AI" -ForegroundColor Gray
        Write-Host ""
        Write-Host "Press Ctrl+C in each terminal to stop workers" -ForegroundColor Yellow
        exit 0
    }
    default {
        Write-Host "❌ Invalid choice!" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Worker is running!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Listening for tasks from RabbitMQ..." -ForegroundColor White
Write-Host "Press Ctrl+C to stop" -ForegroundColor Yellow
Write-Host ""

# Cleanup on exit
Pop-Location
Write-Host ""
Write-Host "AI Service stopped." -ForegroundColor Yellow
