@echo off
echo Starting all services...

echo.
echo Starting Core Service...
start "Core Service (8081)" cmd /k "cd backend\core-service && mvn spring-boot:run"

timeout /t 5 /nobreak

echo Starting Gateway...
start "Gateway (8080)" cmd /k "cd backend\gateway && mvn spring-boot:run"

timeout /t 5 /nobreak

echo Starting Frontend...
start "Frontend (3000)" cmd /k "cd frontend && npm run dev"

echo.
echo All services started!
echo Core Service: http://localhost:8081
echo Gateway: http://localhost:8080
echo Frontend: http://localhost:3000
