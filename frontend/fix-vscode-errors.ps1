# FIX: Clear all VS Code caches and reload

Write-Host "🧹 Clearing VS Code caches..." -ForegroundColor Yellow

# Delete TypeScript cache
$tsCachePath = "$env:LOCALAPPDATA\Microsoft\TypeScript"
if (Test-Path $tsCachePath) {
    Remove-Item -Path $tsCachePath -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "✅ Cleared TypeScript cache" -ForegroundColor Green
}

# Delete VS Code workspace storage
$vscodePath = "$env:APPDATA\Code\User\workspaceStorage"
if (Test-Path $vscodePath) {
    Get-ChildItem -Path $vscodePath | ForEach-Object {
        if ((Get-Content "$($_.FullName)\workspace.json" -ErrorAction SilentlyContinue) -match "smd-syllabus") {
            Remove-Item -Path $_.FullName -Recurse -Force
            Write-Host "✅ Cleared VS Code workspace cache" -ForegroundColor Green
        }
    }
}

Write-Host ""
Write-Host "🎯 Next steps:" -ForegroundColor Cyan
Write-Host "1. Press Ctrl+Shift+P in VS Code" -ForegroundColor White
Write-Host "2. Type: 'TypeScript: Restart TS Server'" -ForegroundColor White
Write-Host "3. Or press F1 and run: 'Developer: Reload Window'" -ForegroundColor White
Write-Host ""
Write-Host "✅ All caches cleared!" -ForegroundColor Green
