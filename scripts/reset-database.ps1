# Script để reset database - xóa và tạo lại database để chạy migration từ đầu

Write-Host "Resetting database..." -ForegroundColor Yellow

# Database connection info (từ docker-compose.yml)
$containerName = "smd-postgres"
$dbUser = "smd_user"
$dbPassword = "smd_password"
$dbName = "smd_database"

# Kiểm tra container có đang chạy không
Write-Host "Checking if PostgreSQL container is running..." -ForegroundColor Cyan
$containerStatus = docker ps --filter "name=$containerName" --format "{{.Status}}"
if (-not $containerStatus) {
    Write-Host "Error: PostgreSQL container '$containerName' is not running!" -ForegroundColor Red
    Write-Host "Please start infrastructure first: .\scripts\start-infrastructure.ps1" -ForegroundColor Yellow
    exit 1
}

Write-Host "Container is running. Proceeding with database reset..." -ForegroundColor Green

# Drop các schema (không drop database vì Flyway tự quản lý)
Write-Host "Dropping schemas core_service and ai_service..." -ForegroundColor Red
docker exec -it $containerName psql -U $dbUser -d $dbName -c "DROP SCHEMA IF EXISTS core_service CASCADE;"
docker exec -it $containerName psql -U $dbUser -d $dbName -c "DROP SCHEMA IF EXISTS ai_service CASCADE;"

# Tạo lại các schema
Write-Host "Creating schemas..." -ForegroundColor Green
docker exec -it $containerName psql -U $dbUser -d $dbName -c "CREATE SCHEMA IF NOT EXISTS core_service;"
docker exec -it $containerName psql -U $dbUser -d $dbName -c "CREATE SCHEMA IF NOT EXISTS ai_service;"

Write-Host ""
Write-Host "Database reset complete! Flyway will run migrations from scratch when you start the services." -ForegroundColor Green
Write-Host "Start core-service now to run migrations." -ForegroundColor Cyan
