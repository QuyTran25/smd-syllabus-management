@echo off
echo ==========================================
echo DANG KHOI DONG HE THONG MICROSERVICES
echo ==========================================

:: 1. Chạy Postgres (Docker)
echo [1/4] Dang khoi dong Postgres...
docker compose up -d postgres
timeout /t 5

:: 2. Chạy Core Service
echo [2/4] Dang khoi dong Core Service (Port 8081)...
start "CORE-SERVICE" cmd /k "cd backend/core-service && mvn spring-boot:run"
timeout /t 15

:: 3. Chạy Gateway
echo [3/4] Dang khoi dong Gateway (Port 8888)...
start "GATEWAY-SERVICE" cmd /k "cd backend/gateway && mvn spring-boot:run"
timeout /t 15

:: 4. Chạy Frontend
echo [4/4] Dang khoi dong Frontend...
cd frontend && npm run dev:student

pause