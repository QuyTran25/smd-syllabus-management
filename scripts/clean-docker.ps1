# ============================================
# Clean Docker Environment Script (PowerShell)
# WARNING: This will remove all containers, volumes, and images
# ============================================

$ErrorActionPreference = "Stop"

Write-Host "SMD Docker Cleanup Script" -ForegroundColor Red
Write-Host "WARNING: This will remove all Docker containers, volumes, and images for SMD project" -ForegroundColor Yellow
Write-Host ""

$confirm = Read-Host "Are you sure you want to continue? (yes/no)"

if ($confirm -ne "yes") {
    Write-Host "Cleanup cancelled" -ForegroundColor Green
    exit 0
}

Write-Host "`nStopping all containers..." -ForegroundColor Yellow
docker-compose down

Write-Host "Removing all volumes (this will DELETE all data)..." -ForegroundColor Red
docker-compose down -v

Write-Host "Removing Docker images..." -ForegroundColor Yellow
docker-compose down --rmi all

Write-Host "Removing orphaned containers..." -ForegroundColor Yellow
docker container prune -f

Write-Host "Removing unused volumes..." -ForegroundColor Yellow
docker volume prune -f

Write-Host "Removing unused networks..." -ForegroundColor Yellow
docker network prune -f

Write-Host "`nSUCCESS: Docker cleanup completed!" -ForegroundColor Green
Write-Host ""
Write-Host "To start fresh, run: .\scripts\setup-dev.ps1" -ForegroundColor Cyan
Write-Host ""
