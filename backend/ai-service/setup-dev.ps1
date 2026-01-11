# ========================================
# SETUP AI SERVICE - DEVELOPMENT
# Script cài đặt ban đầu cho AI Service
# ========================================

Write-Host "🤖 SMD AI Service - Development Setup" -ForegroundColor Cyan
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""

# 1. Kiểm tra Python
Write-Host "Step 1/5: Checking Python..." -ForegroundColor Yellow
$pythonVersion = python --version 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Python not found!" -ForegroundColor Red
    Write-Host "   Please install Python 3.11+ from https://www.python.org/" -ForegroundColor Yellow
    exit 1
}
Write-Host "✅ $pythonVersion" -ForegroundColor Green
Write-Host ""

# 2. Tạo Virtual Environment
Write-Host "Step 2/5: Creating virtual environment..." -ForegroundColor Yellow
if (Test-Path ".\venv") {
    Write-Host "⚠️ Virtual environment already exists. Skipping..." -ForegroundColor Yellow
} else {
    python -m venv venv
    Write-Host "✅ Virtual environment created" -ForegroundColor Green
}
Write-Host ""

# 3. Activate và Install Dependencies
Write-Host "Step 3/5: Installing dependencies..." -ForegroundColor Yellow
& ".\venv\Scripts\Activate.ps1"
pip install --upgrade pip
pip install -r requirements.txt
Write-Host "✅ Dependencies installed" -ForegroundColor Green
Write-Host ""

# 4. Setup .env file
Write-Host "Step 4/5: Setting up .env file..." -ForegroundColor Yellow
if (Test-Path ".env") {
    Write-Host "⚠️ .env file already exists. Skipping..." -ForegroundColor Yellow
} else {
    Copy-Item .env.example .env
    Write-Host "✅ .env file created from template" -ForegroundColor Green
}
Write-Host ""

# 5. Test RabbitMQ connection
Write-Host "Step 5/5: Testing RabbitMQ connection..." -ForegroundColor Yellow
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
    print('✅ RabbitMQ connection successful')
    sys.exit(0)
except Exception as e:
    print(f'⚠️ RabbitMQ connection failed: {e}')
    print('   Make sure RabbitMQ is running: docker-compose up -d rabbitmq')
    sys.exit(1)
"@

$testScript | python
Write-Host ""

# Summary
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host "✅ AI Service Setup Complete!" -ForegroundColor Green
Write-Host "=======================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Ensure RabbitMQ is running:" -ForegroundColor White
Write-Host "   docker-compose up -d rabbitmq" -ForegroundColor Cyan
Write-Host ""
Write-Host "2. Start AI workers:" -ForegroundColor White
Write-Host "   .\start-workers.ps1" -ForegroundColor Cyan
Write-Host ""
Write-Host "3. Test with sample message:" -ForegroundColor White
Write-Host "   python test_send_message.py" -ForegroundColor Cyan
Write-Host ""
Write-Host "For more information, see README.md" -ForegroundColor Gray
Write-Host ""
