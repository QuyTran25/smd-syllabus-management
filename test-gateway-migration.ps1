# ============================================
# Gateway Migration Test Script
# ============================================
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Gateway Migration Verification" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorCount = 0
$SuccessCount = 0

# ============================================
# Test 1: Verify Files Modified
# ============================================
Write-Host "[Test 1/8] Verifying modified files..." -ForegroundColor Yellow

$filesToCheck = @(
    "backend\gateway\src\main\resources\application.yml",
    "backend\core-service\src\main\resources\application.properties",
    "frontend\src\constants\index.ts",
    "frontend\src\services\aiService.ts",
    "frontend\src\student\api\http.ts",
    "docker-compose.yml",
    "frontend\.env.example",
    "frontend\.env.local",
    "frontend\.gitignore"
)

foreach ($file in $filesToCheck) {
    if (Test-Path $file) {
        Write-Host "   $file exists" -ForegroundColor Green
        $SuccessCount++
    } else {
        Write-Host "   $file NOT FOUND" -ForegroundColor Red
        $ErrorCount++
    }
}

# ============================================
# Test 2: Verify CorsConfig.java Deleted
# ============================================
Write-Host ""
Write-Host "[Test 2/8] Verifying CorsConfig.java deleted..." -ForegroundColor Yellow

if (!(Test-Path "backend\gateway\src\main\java\vn\edu\smd\gateway\config\CorsConfig.java")) {
    Write-Host "   CorsConfig.java successfully deleted (avoid double CORS)" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   CorsConfig.java still exists!" -ForegroundColor Red
    $ErrorCount++
}

# ============================================
# Test 3: Verify Gateway application.yml
# ============================================
Write-Host ""
Write-Host "[Test 3/8] Verifying Gateway configuration..." -ForegroundColor Yellow

$gatewayYml = Get-Content "backend\gateway\src\main\resources\application.yml" -Raw

# Check for Docker profile
if ($gatewayYml -match "activate:\s+on-profile:\s+docker") {
    Write-Host "   Docker profile configured" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   Docker profile NOT found" -ForegroundColor Red
    $ErrorCount++
}

# Check for port 5173
if ($gatewayYml -match "5173") {
    Write-Host "   Port 5173 (Vite) added to CORS" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   Port 5173 NOT found in CORS config" -ForegroundColor Red
    $ErrorCount++
}

# Check for service name routing
if ($gatewayYml -match "http://core-service:8081") {
    Write-Host "   Docker service name routing configured" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   Docker service name routing NOT found" -ForegroundColor Red
    $ErrorCount++
}

# ============================================
# Test 4: Verify Core Service Healthcheck
# ============================================
Write-Host ""
Write-Host "[Test 4/8] Verifying Core Service healthcheck probes..." -ForegroundColor Yellow

$coreProps = Get-Content "backend\core-service\src\main\resources\application.properties" -Raw

if ($coreProps -match "management.health.livenessState.enabled=true") {
    Write-Host "   Liveness probe enabled" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   Liveness probe NOT enabled" -ForegroundColor Red
    $ErrorCount++
}

if ($coreProps -match "management.health.readinessState.enabled=true") {
    Write-Host "   Readiness probe enabled" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   Readiness probe NOT enabled" -ForegroundColor Red
    $ErrorCount++
}

# ============================================
# Test 5: Verify Frontend Config Centralization
# ============================================
Write-Host ""
Write-Host "[Test 5/8] Verifying Frontend configuration..." -ForegroundColor Yellow

$constantsTs = Get-Content "frontend\src\constants\index.ts" -Raw

if ($constantsTs -match "localhost:8888") {
    Write-Host "   constants/index.ts points to Gateway (8888)" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   constants/index.ts NOT pointing to Gateway" -ForegroundColor Red
    $ErrorCount++
}

$aiServiceTs = Get-Content "frontend\src\services\aiService.ts" -Raw

if ($aiServiceTs -match "import.*API_BASE_URL.*from.*@/constants") {
    Write-Host "   aiService.ts imports from centralized constants" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   aiService.ts NOT using centralized config" -ForegroundColor Red
    $ErrorCount++
}

$httpTs = Get-Content "frontend\src\student\api\http.ts" -Raw

if ($httpTs -match "import.*API_BASE_URL.*from.*@/constants") {
    Write-Host "   student/api/http.ts imports from centralized constants" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   student/api/http.ts NOT using centralized config" -ForegroundColor Red
    $ErrorCount++
}

# ============================================
# Test 6: Verify Docker Compose
# ============================================
Write-Host ""
Write-Host "[Test 6/8] Verifying Docker Compose configuration..." -ForegroundColor Yellow

$dockerCompose = Get-Content "docker-compose.yml" -Raw

# Check Gateway dependency
if ($dockerCompose -match "depends_on:[\s\S]*core-service:[\s\S]*condition:\s+service_healthy") {
    Write-Host "   Gateway waits for Core Service healthy" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   Gateway dependency NOT properly configured" -ForegroundColor Red
    $ErrorCount++
}

# Check Core Service healthcheck
if ($dockerCompose -match "healthcheck:[\s\S]*curl.*actuator/health") {
    Write-Host "   Core Service healthcheck configured" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   Core Service healthcheck NOT found" -ForegroundColor Red
    $ErrorCount++
}

# Check Frontend env var
if ($dockerCompose -match "VITE_API_GATEWAY_URL=http://localhost:8888") {
    Write-Host "   Frontend env points to Gateway port 8888" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   Frontend env NOT pointing to correct Gateway port" -ForegroundColor Red
    $ErrorCount++
}

# ============================================
# Test 7: Verify .env.local
# ============================================
Write-Host ""
Write-Host "[Test 7/8] Verifying environment files..." -ForegroundColor Yellow

$envLocal = Get-Content "frontend\.env.local" -Raw

if ($envLocal -match "VITE_API_GATEWAY_URL=http://localhost:8888") {
    Write-Host "   .env.local configured correctly" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   .env.local NOT configured correctly" -ForegroundColor Red
    $ErrorCount++
}

$gitignore = Get-Content "frontend\.gitignore" -Raw

if ($gitignore -match "\.env\.local") {
    Write-Host "   .gitignore includes .env.local" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "   .gitignore does NOT include .env.local" -ForegroundColor Red
    $ErrorCount++
}

# ============================================
# Test 8: Check for Common Issues
# ============================================
Write-Host ""
Write-Host "[Test 8/8] Checking for potential issues..." -ForegroundColor Yellow

# Check if any file still references port 8081 directly
$filesWithOldPort = @()

Get-ChildItem "frontend\src" -Recurse -Include "*.ts","*.tsx" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if ($content -match "localhost:8081" -and $_.Name -ne "api-config.ts") {
        $filesWithOldPort += $_.FullName
    }
}

if ($filesWithOldPort.Count -eq 0) {
    Write-Host "   No hardcoded port 8081 references found" -ForegroundColor Green
    $SuccessCount++
} else {
    Write-Host "    Found files still referencing port 8081:" -ForegroundColor Yellow
    foreach ($file in $filesWithOldPort) {
        Write-Host "      - $file" -ForegroundColor Yellow
    }
}

# ============================================
# Summary
# ============================================
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   Passed: $SuccessCount" -ForegroundColor Green
Write-Host "   Failed: $ErrorCount" -ForegroundColor Red
Write-Host ""

if ($ErrorCount -eq 0) {
    Write-Host " ALL TESTS PASSED! Migration completed successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next Steps:" -ForegroundColor Cyan
    Write-Host "  1. Rebuild backend services:" -ForegroundColor White
    Write-Host "     cd backend/core-service && mvn clean package" -ForegroundColor Gray
    Write-Host "     cd backend/gateway && mvn clean package" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  2. Test locally:" -ForegroundColor White
    Write-Host "     Run: .\start-step-by-step.ps1" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  3. Or test with Docker:" -ForegroundColor White
    Write-Host "     docker-compose down" -ForegroundColor Gray
    Write-Host "     docker-compose build" -ForegroundColor Gray
    Write-Host "     docker-compose up -d" -ForegroundColor Gray
} else {
    Write-Host "  Some tests failed. Please review the errors above." -ForegroundColor Yellow
    exit 1
}
