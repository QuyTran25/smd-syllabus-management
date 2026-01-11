# ============================================
# INSTALL AI DEPENDENCIES
# Run this script to install AI libraries
# ============================================

Write-Host "`n📦 AI DEPENDENCIES INSTALLATION" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Check if venv exists
if (-not (Test-Path "venv")) {
    Write-Host "❌ Virtual environment not found!" -ForegroundColor Red
    Write-Host "Run setup-dev.ps1 first" -ForegroundColor Yellow
    exit 1
}

# Activate venv
Write-Host "🔧 Activating virtual environment..." -ForegroundColor Yellow
.\venv\Scripts\Activate.ps1

# Install AI packages
Write-Host "`n📥 Installing AI libraries (this may take 5-10 minutes)..." -ForegroundColor Yellow
Write-Host "   - transformers (8.2 MB)" -ForegroundColor Gray
Write-Host "   - torch (192.3 MB) ⚠️ LARGE DOWNLOAD" -ForegroundColor Gray
Write-Host "   - sentencepiece (977 KB)" -ForegroundColor Gray
Write-Host "   - accelerate (265 KB)" -ForegroundColor Gray
Write-Host "`nPlease wait and DO NOT cancel (Ctrl+C)...`n" -ForegroundColor Red

pip install transformers==4.36.2 torch==2.1.2 sentencepiece==0.1.99 accelerate==0.25.0

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✅ AI dependencies installed successfully!" -ForegroundColor Green
    Write-Host "`n📝 Next steps:" -ForegroundColor Cyan
    Write-Host "   1. Copy .env.example to .env" -ForegroundColor White
    Write-Host "   2. Edit .env: Set MOCK_MODE=false" -ForegroundColor White
    Write-Host "   3. Run: python app\workers\summarize_worker.py" -ForegroundColor White
    Write-Host "   4. Model will auto-download on first run (~892MB)`n" -ForegroundColor White
} else {
    Write-Host "`n❌ Installation failed!" -ForegroundColor Red
    Write-Host "Check internet connection and try again.`n" -ForegroundColor Yellow
}
