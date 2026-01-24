# ========================================
# SMD AI Service Stop Script
# Dừng tất cả AI Service Workers
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Stopping SMD AI Service Workers" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Find and kill Python processes running AI service
$aiProcesses = Get-Process python -ErrorAction SilentlyContinue | Where-Object {
    $_.CommandLine -like "*ai-service*" -or $_.CommandLine -like "*app.main*"
}

if ($aiProcesses) {
    Write-Host "Found $($aiProcesses.Count) AI service process(es)..." -ForegroundColor Yellow
    foreach ($proc in $aiProcesses) {
        Write-Host "  Stopping process $($proc.Id)..." -ForegroundColor Yellow
        Stop-Process -Id $proc.Id -Force
        Write-Host "  [OK] Process $($proc.Id) stopped" -ForegroundColor Green
    }
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "  AI Service stopped successfully!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
} else {
    Write-Host "  [!] No AI service processes found" -ForegroundColor Yellow
    Write-Host ""
}
