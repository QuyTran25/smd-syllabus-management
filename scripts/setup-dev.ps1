# ============================================
# SMD Development Environment Setup Script (PowerShell)
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "Setting up SMD Development Environment..." -ForegroundColor Cyan

# Check if Docker is installed
try {
    docker --version | Out-Null
    Write-Host "SUCCESS: Docker is installed" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Docker is not installed. Please install Docker Desktop first." -ForegroundColor Red
    exit 1
}

# Check if Docker Compose is installed
try {
    docker-compose --version | Out-Null
    Write-Host "SUCCESS: Docker Compose is installed" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Docker Compose is not installed. Please install Docker Compose first." -ForegroundColor Red
    exit 1
}

# Create .env file if not exists
if (-Not (Test-Path .env)) {
    Write-Host "Creating .env file from .env.example..." -ForegroundColor Yellow
    Copy-Item .env.example .env
    Write-Host "WARNING: Please edit .env file with your configurations" -ForegroundColor Yellow
} else {
    Write-Host "SUCCESS: .env file already exists" -ForegroundColor Green
}

# Create uploads directory
if (-Not (Test-Path uploads)) {
    New-Item -ItemType Directory -Path uploads | Out-Null
    Write-Host "SUCCESS: Created uploads directory" -ForegroundColor Green
}

# Pull required Docker images
Write-Host "`nPulling Docker images..." -ForegroundColor Cyan
docker-compose pull

# Build custom images
Write-Host "`nBuilding application images..." -ForegroundColor Cyan
docker-compose build

# Start infrastructure services first
Write-Host "`nStarting infrastructure services (Database, Redis, RabbitMQ, Kafka)..." -ForegroundColor Cyan
docker-compose up -d postgres redis rabbitmq zookeeper kafka

# Wait for services to be healthy
Write-Host "`nWaiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 15

# Check PostgreSQL
Write-Host "`nChecking PostgreSQL..." -ForegroundColor Cyan
docker-compose exec -T postgres pg_isready -U smd_user

# Check Redis
Write-Host "Checking Redis..." -ForegroundColor Cyan
docker-compose exec -T redis redis-cli ping

Write-Host "`nSUCCESS: Development environment setup completed!" -ForegroundColor Green
Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "   1. Edit .env file with your configurations (JWT_SECRET, FIREBASE_SERVICE_ACCOUNT_KEY, etc.)"
Write-Host "   2. Start all services: docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d"
Write-Host "   3. View logs: docker-compose logs -f"
Write-Host "   4. Access services:"
Write-Host "      - Frontend: http://localhost:3000"
Write-Host "      - Gateway: http://localhost:8080"
Write-Host "      - Core Service: http://localhost:8081"
Write-Host "      - AI Service: http://localhost:8082/docs"
Write-Host "      - RabbitMQ UI: http://localhost:15672 (guest/guest)"
Write-Host ""
