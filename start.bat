@echo off
chcp 65001 >nul
echo ==========================================
echo Starting Inventory Management Simulator
echo ==========================================

REM Check if Docker is running
docker info >nul 2>&1
if errorlevel 1 (
    echo Error: Docker is not running, please start Docker first
    pause
    exit /b 1
)

REM Start RabbitMQ
echo 1. Starting RabbitMQ...
docker-compose up -d rabbitmq

REM Wait for RabbitMQ to be ready
echo 2. Waiting for RabbitMQ to be ready...
timeout /t 10 /nobreak >nul

echo 3. RabbitMQ is ready!
echo.
echo ==========================================
echo Starting Spring Boot application...
echo ==========================================

REM Check if Maven is available
where mvn >nul 2>&1
if errorlevel 1 (
    echo.
    echo Error: Maven (mvn) command not found
    echo.
    echo Please choose one of the following options:
    echo 1. Install Maven and add to PATH
    echo    Download: https://maven.apache.org/download.cgi
    echo.
    echo 2. Use IDE (such as IntelliJ IDEA or Eclipse) to run
    echo    Main class: com.inventory.InventorySimulatorApplication
    echo.
    echo 3. If Maven is already installed, ensure it's added to system PATH environment variable
    echo.
    pause
    exit /b 1
)

REM Start Spring Boot application
echo Starting application with Maven...
call mvn spring-boot:run

pause
