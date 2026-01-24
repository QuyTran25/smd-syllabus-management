# Quick Start AI Workers
# Script nhanh để chạy cả 2 workers

Write-Host "🚀 Starting AI Workers..." -ForegroundColor Cyan
Write-Host ""

# Kiểm tra RabbitMQ
$rabbitmq = docker ps --filter "name=smd-rabbitmq" --format "{{.Status}}"
if (!$rabbitmq) {
    Write-Host "❌ RabbitMQ is not running!" -ForegroundColor Red
    Write-Host "Please run: docker-compose up -d rabbitmq" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ RabbitMQ: $rabbitmq" -ForegroundColor Green
Write-Host ""

# Navigate to ai-service
Push-Location "$PSScriptRoot\backend\ai-service"

# Activate venv
if (Test-Path ".\venv\Scripts\Activate.ps1") {
    & ".\venv\Scripts\Activate.ps1"
} else {
    Write-Host "❌ Virtual environment not found!" -ForegroundColor Red
    Write-Host "Please run: .\start-ai-service.ps1 first" -ForegroundColor Yellow
    Pop-Location
    exit 1
}

Write-Host "🔬 Starting Analysis Worker..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", `
    "cd '$PWD'; .\venv\Scripts\Activate.ps1; `
    Write-Host '🔬 ANALYSIS WORKER (So sánh, CLO-PLO)' -ForegroundColor Cyan; `
    python -m app.workers.analysis_worker"

Start-Sleep -Seconds 2

Write-Host "📝 Starting Summarize Worker..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", `
    "cd '$PWD'; .\venv\Scripts\Activate.ps1; `
    Write-Host '📝 SUMMARIZE WORKER (Tóm tắt AI)' -ForegroundColor Yellow; `
    python -m app.workers.summarize_worker"

Pop-Location

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ AI Workers Started!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Two terminal windows opened:" -ForegroundColor White
Write-Host "  1. Analysis Worker (ai_processing_queue)" -ForegroundColor Gray
Write-Host "  2. Summarize Worker (ai_summarize_queue)" -ForegroundColor Gray
Write-Host ""
Write-Host "To stop: Close the worker terminals or press Ctrl+C" -ForegroundColor Yellow
