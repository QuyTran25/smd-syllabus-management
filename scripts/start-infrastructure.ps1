# ============================================
# Start Infrastructure Services Only (PowerShell)
# Use this to test DB, Redis, RabbitMQ, Kafka
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "Starting SMD Infrastructure Services..." -ForegroundColor Cyan
Write-Host ""

# Check if .env exists
if (-Not (Test-Path .env)) {
    Write-Host "ERROR: .env file not found. Please create it from .env.example" -ForegroundColor Red
    exit 1
}

Write-Host "Step 1: Starting Database (PostgreSQL)..." -ForegroundColor Yellow
docker-compose up -d postgres

Write-Host "Waiting for PostgreSQL to be ready..." -ForegroundColor Gray
Start-Sleep -Seconds 10

Write-Host "`nStep 2: Starting Cache (Redis)..." -ForegroundColor Yellow
docker-compose up -d redis

Write-Host "`nStep 3: Starting Message Queue (RabbitMQ)..." -ForegroundColor Yellow
docker-compose up -d rabbitmq

Write-Host "Waiting for RabbitMQ to be ready..." -ForegroundColor Gray
Start-Sleep -Seconds 10

Write-Host "`nStep 4: Starting Event Streaming (Kafka + Zookeeper)..." -ForegroundColor Yellow
docker-compose up -d zookeeper kafka

Write-Host "Waiting for Kafka to be ready..." -ForegroundColor Gray
Start-Sleep -Seconds 15

Write-Host "`nSUCCESS: All infrastructure services started!" -ForegroundColor Green
Write-Host "`nServices status:" -ForegroundColor Cyan
docker-compose ps postgres redis rabbitmq zookeeper kafka

Write-Host "`nAccess points:" -ForegroundColor Cyan
Write-Host "  - PostgreSQL: localhost:5432 (user: smd_user, password: check .env)"
Write-Host "  - Redis: localhost:6379"
Write-Host "  - RabbitMQ UI: http://localhost:15672 (guest/guest)"
Write-Host "  - Kafka: localhost:9092"
Write-Host ""
Write-Host "To check logs: docker-compose logs -f <service_name>" -ForegroundColor Gray
Write-Host "To stop all: docker-compose down" -ForegroundColor Gray
Write-Host ""
