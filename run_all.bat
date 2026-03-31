@echo off
REM =============================================================================
REM PrimeBid Forward Auction System - Start All Services (Windows)
REM =============================================================================
REM Prerequisites: Java 17 (uses per-service Maven wrapper)
REM =============================================================================

echo =============================================
echo   PrimeBid - Starting All Services
echo =============================================

if not exist logs mkdir logs
if not exist iam\data mkdir iam\data
if not exist catalogue\data mkdir catalogue\data
if not exist auction\data mkdir auction\data
if not exist payment\data mkdir payment\data
if not exist gateway\data mkdir gateway\data

echo Starting IAM service (port 8081)...
start /B cmd /c "cd iam && mvnw.cmd -q spring-boot:run > ..\logs\iam.log 2>&1"

echo Starting Catalogue service (port 8082)...
start /B cmd /c "cd catalogue && mvnw.cmd -q spring-boot:run > ..\logs\catalogue.log 2>&1"

echo Starting Auction service (port 8083)...
start /B cmd /c "cd auction && mvnw.cmd -q spring-boot:run > ..\logs\auction.log 2>&1"

echo Starting Payment service (port 8084)...
start /B cmd /c "cd payment && mvnw.cmd -q spring-boot:run > ..\logs\payment.log 2>&1"

REM Wait for backend services to start
echo Waiting for backend services to start...
timeout /t 15 /nobreak > nul

echo Starting Gateway service (port 8080)...
start /B cmd /c "cd gateway && mvnw.cmd -q spring-boot:run > ..\logs\gateway.log 2>&1"

echo.
echo =============================================
echo   All services starting...
echo =============================================
echo.
echo Ports:
echo   gateway   8080
echo   iam       8081
echo   catalogue 8082
echo   auction   8083
echo   payment   8084
echo.
echo Try:
echo   curl http://localhost:8080/health
echo.
echo Logs are in .\logs\
echo Press Ctrl+C to stop (then close remaining windows)
echo.
pause
