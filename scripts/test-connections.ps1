# ============================================
# Test Infrastructure Connections (PowerShell)
# ============================================

$ErrorActionPreference = "Continue"

Write-Host "Testing SMD Infrastructure Connections..." -ForegroundColor Cyan
Write-Host ""

# Test PostgreSQL
Write-Host "Testing PostgreSQL connection... " -NoNewline
try {
    docker-compose exec -T postgres pg_isready -U smd_user 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK" -ForegroundColor Green
    } else {
        Write-Host "FAILED" -ForegroundColor Red
    }
} catch {
    Write-Host "FAILED" -ForegroundColor Red
}

# Test Redis
Write-Host "Testing Redis connection... " -NoNewline
try {
    $result = docker-compose exec -T redis redis-cli ping 2>&1
    if ($result -match "PONG") {
        Write-Host "OK" -ForegroundColor Green
    } else {
        Write-Host "FAILED" -ForegroundColor Red
    }
} catch {
    Write-Host "FAILED" -ForegroundColor Red
}

# Test RabbitMQ
Write-Host "Testing RabbitMQ connection... " -NoNewline
try {
    docker-compose exec -T rabbitmq rabbitmq-diagnostics ping 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK" -ForegroundColor Green
    } else {
        Write-Host "FAILED" -ForegroundColor Red
    }
} catch {
    Write-Host "FAILED" -ForegroundColor Red
}

# Test Kafka
Write-Host "Testing Kafka connection... " -NoNewline
try {
    docker-compose exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092 2>&1 | Out-Null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK" -ForegroundColor Green
    } else {
        Write-Host "FAILED" -ForegroundColor Red
    }
} catch {
    Write-Host "FAILED" -ForegroundColor Red
}

Write-Host "`nConnection test completed!" -ForegroundColor Cyan
Write-Host ""

# Show database schemas
Write-Host "Checking PostgreSQL schemas..." -ForegroundColor Yellow
docker-compose exec -T postgres psql -U smd_user -d smd_database -c "\dn"

Write-Host "`nChecking RabbitMQ queues..." -ForegroundColor Yellow
docker-compose exec -T rabbitmq rabbitmqctl list_queues

Write-Host ""
