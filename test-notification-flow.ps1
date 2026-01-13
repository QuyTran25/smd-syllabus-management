#!/usr/bin/env pwsh
# Test Notification Flow Script

$API_URL = "http://localhost:8080/api"

Write-Host "🔍 TEST NOTIFICATION FLOW" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan
Write-Host ""

# 1. Get token (Student)
Write-Host "1️⃣  Getting student token..." -ForegroundColor Yellow
$studentLoginResponse = Invoke-RestMethod -Uri "$API_URL/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"student@example.com","password":"password123"}'
$studentToken = $studentLoginResponse.data.token
Write-Host "✅ Student token: $($studentToken.Substring(0, 20))..." -ForegroundColor Green
Write-Host ""

# 2. Get first syllabus ID
Write-Host "2️⃣  Getting first syllabus..." -ForegroundColor Yellow
$syllabusResponse = Invoke-RestMethod -Uri "$API_URL/syllabi?page=0&size=1" -Method Get -Headers @{"Authorization" = "Bearer $studentToken"}
$syllabusId = $syllabusResponse.data.content[0].id
$subjectCode = $syllabusResponse.data.content[0].subjectCode
Write-Host "✅ Found syllabus: $subjectCode (ID: $syllabusId)" -ForegroundColor Green
Write-Host ""

# 3. Follow the syllabus
Write-Host "3️⃣  Following syllabus..." -ForegroundColor Yellow
$followResponse = Invoke-RestMethod -Uri "$API_URL/student/syllabi/$syllabusId/track" -Method Post -Headers @{"Authorization" = "Bearer $studentToken"}
Write-Host "✅ Followed!" -ForegroundColor Green
Write-Host ""

# 4. Check tracker created
Write-Host "4️⃣  Checking StudentSyllabusTracker in database..." -ForegroundColor Yellow
Write-Host "   Query: SELECT * FROM student_syllabus_tracker WHERE syllabus_id = '$syllabusId'" -ForegroundColor Gray
Write-Host ""

# 5. Get admin token
Write-Host "5️⃣  Getting admin token..." -ForegroundColor Yellow
$adminLoginResponse = Invoke-RestMethod -Uri "$API_URL/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"admin@example.com","password":"password123"}'
$adminToken = $adminLoginResponse.data.token
Write-Host "✅ Admin token: $($adminToken.Substring(0, 20))..." -ForegroundColor Green
Write-Host ""

# 6. Publish syllabus
Write-Host "6️⃣  Publishing syllabus..." -ForegroundColor Yellow
$publishResponse = Invoke-RestMethod -Uri "$API_URL/admin/syllabi/$syllabusId/publish" -Method Patch -Headers @{"Authorization" = "Bearer $adminToken"} -ContentType "application/json" -Body '{"effectiveDate":"2026-01-15"}'
Write-Host "✅ Syllabus published!" -ForegroundColor Green
Write-Host ""

# 7. Wait a bit for notifications to process
Write-Host "7️⃣  Waiting for notifications to process..." -ForegroundColor Yellow
Start-Sleep -Seconds 2
Write-Host ""

# 8. Check notifications
Write-Host "8️⃣  Checking notifications for student..." -ForegroundColor Yellow
$notificationsResponse = Invoke-RestMethod -Uri "$API_URL/notifications" -Method Get -Headers @{"Authorization" = "Bearer $studentToken"}
Write-Host "✅ Got $($notificationsResponse.data.Count) notifications" -ForegroundColor Green
Write-Host ""

if ($notificationsResponse.data.Count -gt 0) {
    Write-Host "📬 Latest notifications:" -ForegroundColor Cyan
    $notificationsResponse.data | Select-Object -First 3 | ForEach-Object {
        Write-Host "   • $($_.title)" -ForegroundColor Green
        Write-Host "     Message: $($_.message)" -ForegroundColor Gray
        Write-Host "     Type: $($_.type)" -ForegroundColor Gray
        Write-Host ""
    }
} else {
    Write-Host "❌ NO NOTIFICATIONS FOUND!" -ForegroundColor Red
}

Write-Host "✅ TEST COMPLETED" -ForegroundColor Green
