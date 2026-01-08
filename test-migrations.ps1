# Test all migrations
docker exec smd-postgres psql -U smd_user -d smd_database -c "DROP SCHEMA IF EXISTS core_service CASCADE; CREATE SCHEMA core_service;" | Out-Null

<<<<<<< HEAD
$files = Get-ChildItem "$PSScriptRoot\database\core-service\V*.sql" | Sort-Object { 
=======
$files = Get-ChildItem "d:\smd-syllabus-management\database\core-service\V*.sql" | Sort-Object { 
>>>>>>> 0ec901f2d98f3b75296df3acee6d66b44cbbc3c7
    if ($_.Name -match 'V(\d+)') { [int]$matches[1] } 
}

$pass = 0
$fail = @()

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    $result = $content | docker exec -i smd-postgres psql -U smd_user -d smd_database 2>&1
    
    if ($result -match "ERROR") {
        $errorMsg = ($result | Select-String "ERROR:" | Select-Object -First 1).Line
        $fail += @{Name=$file.Name; Error=$errorMsg}
    } else {
        $pass++
    }
}

Write-Host "`n=== RESULT: $pass/$($files.Count) PASS ===" -ForegroundColor $(if ($fail.Count -eq 0) {"Green"} else {"Yellow"})

if ($fail.Count -gt 0) {
    Write-Host "`nFAILED ($($fail.Count)):" -ForegroundColor Red
    $fail | ForEach-Object { 
        Write-Host "  $($_.Name)" -ForegroundColor Yellow
        Write-Host "    $($_.Error)" -ForegroundColor Gray
    }
}
