# Quick restart AI worker
Write-Host "[RESTART AI WORKER]" -ForegroundColor Cyan
Write-Host ""

# Kill old workers
Write-Host "Stopping old workers..." -ForegroundColor Yellow
Get-Process python -ErrorAction SilentlyContinue | ForEach-Object {
    $cmdLine = (Get-WmiObject Win32_Process -Filter "ProcessId = $($_.Id)").CommandLine
    if ($cmdLine -like "*ai_worker*" -or $cmdLine -like "*summarize_worker*") {
        Write-Host "  Killing PID $($_.Id)" -ForegroundColor Red
        Stop-Process -Id $_.Id -Force
    }
}

Start-Sleep -Seconds 1

Write-Host ""
Write-Host "Starting new AI worker..." -ForegroundColor Green
Write-Host "NOTE: Keep this window open to see live logs" -ForegroundColor Yellow
Write-Host ""

# Start worker (foreground to see logs)
python -m app.workers.ai_worker
