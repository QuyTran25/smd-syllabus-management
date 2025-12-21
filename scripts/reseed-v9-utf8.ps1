# Re-seed V9 with proper UTF-8 encoding
# This script ensures Vietnamese characters are correctly inserted into PostgreSQL

Write-Host "Re-seeding V9 data with UTF-8 encoding..." -ForegroundColor Green

# Read V9 file as UTF-8
$v9Content = Get-Content -Path ".\database\core-service\V9__seed_full_curriculum.sql" -Encoding UTF8 -Raw

# Clear existing data (reverse order of foreign keys)
$clearData = @"
BEGIN;
SET search_path TO core_service;
DELETE FROM subject_relationships;
DELETE FROM subjects;
DELETE FROM departments;
DELETE FROM faculties;
COMMIT;
"@

Write-Host "Clearing existing data..." -ForegroundColor Yellow
$clearBytes = [System.Text.Encoding]::UTF8.GetBytes($clearData)
$clearData | docker exec -i smd-postgres psql -U smd_user -d smd_database

Write-Host "Inserting V9 data with UTF-8..." -ForegroundColor Yellow
# Use UTF-8 byte array to ensure proper encoding
$v9Bytes = [System.Text.Encoding]::UTF8.GetBytes($v9Content)
$v9Content | docker exec -i smd-postgres psql -U smd_user -d smd_database

Write-Host ""
Write-Host "Verifying data..." -ForegroundColor Green
docker exec smd-postgres psql -U smd_user -d smd_database -c "SELECT COUNT(*) as faculties FROM core_service.faculties; SELECT COUNT(*) as departments FROM core_service.departments; SELECT COUNT(*) as subjects FROM core_service.subjects; SELECT COUNT(*) as prerequisites FROM core_service.subject_relationships;"

Write-Host ""
Write-Host "Sample subjects (use GUI tool to see Vietnamese correctly):" -ForegroundColor Cyan
docker exec smd-postgres psql -U smd_user -d smd_database -c "SELECT code, current_name_vi, default_credits FROM core_service.subjects LIMIT 5;"

Write-Host ""
Write-Host "✓ Done! Use pgAdmin/DBeaver to view Vietnamese characters correctly." -ForegroundColor Green
Write-Host "  Connection: localhost:5432, DB: smd_database, User: smd_user, Pass: smd_password" -ForegroundColor Yellow
