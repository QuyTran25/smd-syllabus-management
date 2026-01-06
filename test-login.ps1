# Test login for admin user
$body = @{
    email = "admin@smd.edu.vn"
    password = "123456"
} | ConvertTo-Json

Write-Host "Testing login for admin..." -ForegroundColor Cyan
Write-Host "Request body: $body" -ForegroundColor Gray

try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/auth/login" `
        -Method POST `
        -Headers @{"Content-Type" = "application/json"} `
        -Body $body `
        -TimeoutSec 10 `
        -ErrorAction Stop
    
    Write-Host "SUCCESS" -ForegroundColor Green
    $result = $response.Content | ConvertFrom-Json
    $result | ConvertTo-Json | Write-Host -ForegroundColor Green
    
} catch {
    Write-Host "FAILED" -ForegroundColor Red
    Write-Host "Exception: $($_.Exception.GetType().Name)" -ForegroundColor Red
    Write-Host "Message: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.Value__
        Write-Host "Status: $statusCode" -ForegroundColor Red
        
        try {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = [System.IO.StreamReader]::new($stream)
            $responseBody = $reader.ReadToEnd()
            Write-Host "Response:" -ForegroundColor Red
            Write-Host $responseBody -ForegroundColor Yellow
            $reader.Close()
        } catch {
            Write-Host "Could not read body" -ForegroundColor Gray
        }
    }
}
