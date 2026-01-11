# ============================================
# TEST AI SUMMARIZE VIA API
# Gọi trực tiếp API Core Service
# ============================================

Write-Host "`n🧪 TEST AI SUMMARIZE VIA API" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Cần có token để gọi API
$token = Read-Host "Nhập JWT token (từ localStorage sau khi login)"

if ([string]::IsNullOrWhiteSpace($token)) {
    Write-Host "❌ Cần token để gọi API!" -ForegroundColor Red
    Write-Host "   1. Login vào Frontend" -ForegroundColor Yellow
    Write-Host "   2. Mở DevTools > Console" -ForegroundColor Yellow
    Write-Host "   3. Chạy: localStorage.getItem('token')" -ForegroundColor Yellow
    Write-Host "   4. Copy token và paste vào đây`n" -ForegroundColor Yellow
    exit 1
}

# Nhập syllabus ID
$syllabusId = Read-Host "Nhập Syllabus ID (ví dụ: 1, 2, 3...)"

if ([string]::IsNullOrWhiteSpace($syllabusId)) {
    Write-Host "❌ Cần Syllabus ID!" -ForegroundColor Red
    exit 1
}

Write-Host "`n📤 Sending request..." -ForegroundColor Yellow
Write-Host "   API: POST http://localhost:8081/api/ai/syllabus/$syllabusId/summarize" -ForegroundColor Gray
Write-Host "   Token: ${token.Substring(0, [Math]::Min(20, $token.Length))}..." -ForegroundColor Gray

# Gọi API
try {
    $response = Invoke-RestMethod `
        -Uri "http://localhost:8081/api/ai/syllabus/$syllabusId/summarize" `
        -Method Post `
        -Headers @{
            "Authorization" = "Bearer $token"
            "Content-Type" = "application/json"
        } `
        -TimeoutSec 30

    Write-Host "`n✅ API Response:" -ForegroundColor Green
    Write-Host ($response | ConvertTo-Json -Depth 10) -ForegroundColor White

    if ($response.task_id) {
        Write-Host "`n📊 Task ID: $($response.task_id)" -ForegroundColor Cyan
        Write-Host "Check AI Worker terminal for processing logs...`n" -ForegroundColor Yellow
    }

} catch {
    Write-Host "`n❌ API Error:" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Yellow
    
    if ($_.ErrorDetails.Message) {
        Write-Host "`nError Details:" -ForegroundColor Red
        Write-Host $_.ErrorDetails.Message -ForegroundColor White
    }
}

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "Lưu ý:" -ForegroundColor Yellow
Write-Host "  - Kiểm tra terminal AI Worker để xem processing logs" -ForegroundColor Gray
Write-Host "  - AI xử lý mất ~14 giây" -ForegroundColor Gray
Write-Host "  - Kết quả sẽ được gửi về qua RabbitMQ`n" -ForegroundColor Gray
