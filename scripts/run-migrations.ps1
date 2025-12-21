#!/usr/bin/env pwsh
# Script to run Flyway migrations into Docker PostgreSQL

Write-Host "===========================================`n" -ForegroundColor Cyan
Write-Host "  Running Database Migrations" -ForegroundColor Cyan
Write-Host "===========================================`n" -ForegroundColor Cyan

# Check if PostgreSQL container is running
$containerStatus = docker ps --filter "name=smd-postgres" --format "{{.Status}}"
if (-not $containerStatus) {
    Write-Host "ERROR: PostgreSQL container is not running!" -ForegroundColor Red
    Write-Host "Run: docker-compose up -d postgres`n" -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ PostgreSQL container is running`n" -ForegroundColor Green

# Enable required extensions
Write-Host "Enabling PostgreSQL extensions..." -ForegroundColor Cyan
@'
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "vector";
'@ | docker exec -i smd-postgres psql -U smd_user -d smd_database 2>&1 | Out-Null

Write-Host "✓ Extensions enabled`n" -ForegroundColor Green

# Get migration files
$migrationPath = "d:\smd-syllabus-management\database\core-service"
$migrations = Get-ChildItem -Path $migrationPath -Filter "V*.sql" | 
    Sort-Object { [int]($_.Name -replace 'V(\d+)__.+', '$1') }

Write-Host "Found $($migrations.Count) migrations to run`n" -ForegroundColor Cyan

# Run each migration
foreach ($migration in $migrations) {
    Write-Host "Running $($migration.Name)... " -NoNewline -ForegroundColor Yellow
    
    # Add search_path at the beginning of SQL  
    # Also fix uuid_generate_v4() to use public schema explicitly
    $sqlContent = Get-Content $migration.FullName -Raw
    $sqlContent = $sqlContent -replace 'uuid_generate_v4\(\)', 'public.uuid_generate_v4()'
    
    $sql = @"
SET search_path TO core_service, public;
$sqlContent
"@
    
    # Execute migration
    $result = $sql | docker exec -i smd-postgres psql -U smd_user -d smd_database 2>&1
    
    # Check for errors
    $errors = $result | Select-String -Pattern "^ERROR:" -CaseSensitive
    
    if ($errors) {
        Write-Host "FAILED`n" -ForegroundColor Red
        Write-Host "Errors:" -ForegroundColor Red
        $errors | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        Write-Host ""
        
        # Ask to continue
        $continue = Read-Host "Continue with next migration? (y/N)"
        if ($continue -ne 'y') {
            exit 1
        }
    } else {
        Write-Host "OK" -ForegroundColor Green
    }
}

Write-Host "`n===========================================`n" -ForegroundColor Cyan
Write-Host "  Migration Complete!" -ForegroundColor Green
Write-Host "===========================================`n" -ForegroundColor Cyan

# Verify tables
Write-Host "Verifying tables..." -ForegroundColor Cyan
$tableCount = docker exec smd-postgres psql -U smd_user -d smd_database -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'core_service';"
Write-Host "✓ Found $($tableCount.Trim()) tables in core_service schema`n" -ForegroundColor Green

Write-Host "Done! Database is ready." -ForegroundColor Green
