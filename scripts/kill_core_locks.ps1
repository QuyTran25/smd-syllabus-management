Set-Location 'D:\SMD_F1\smd-syllabus-management\backend\core-service'
$matches = Get-CimInstance Win32_Process | Where-Object { $_.CommandLine -and ($_.CommandLine -match 'core-service' -or $_.CommandLine -match 'target\\classes' -or $_.CommandLine -match '8081') }
if ($matches) {
    $matches | Select-Object ProcessId,Name,CommandLine | Format-List
    foreach ($m in $matches) {
        try {
            Stop-Process -Id $m.ProcessId -Force -ErrorAction Stop
            Write-Output "Stopped PID: $($m.ProcessId) ($($m.Name))"
        } catch {
            Write-Output "Failed to stop PID: $($m.ProcessId) - $($_.Exception.Message)"
        }
    }
} else {
    Write-Output 'No matching java process found'
}
