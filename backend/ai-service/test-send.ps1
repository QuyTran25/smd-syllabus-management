# Simple PowerShell wrapper to run the Python test script
Write-Host "`n🧪 TEST AI SUMMARIZE (DIRECT)" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Get syllabus ID
Write-Host "📝 Nhập Syllabus ID (Enter = 124001):" -ForegroundColor Yellow
$syllabusId = Read-Host

# Activate venv and run Python script
& .\venv\Scripts\Activate.ps1
python test_direct_send.py $syllabusId

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "Kiểm tra terminal AI Worker để xem kết quả!`n" -ForegroundColor Yellow
