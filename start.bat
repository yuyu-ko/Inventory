@echo off
chcp 65001 >nul
echo ==========================================
echo 启动库存管理模拟器
echo ==========================================

REM 检查 Docker 是否运行
docker info >nul 2>&1
if errorlevel 1 (
    echo 错误: Docker 未运行，请先启动 Docker
    pause
    exit /b 1
)

REM 启动 RabbitMQ
echo 1. 启动 RabbitMQ...
docker-compose up -d rabbitmq

REM 等待 RabbitMQ 就绪
echo 2. 等待 RabbitMQ 就绪...
timeout /t 10 /nobreak >nul

echo 3. RabbitMQ 已就绪！
echo.
echo ==========================================
echo 启动 Spring Boot 应用...
echo ==========================================

REM 检查 Maven 是否可用
where mvn >nul 2>&1
if errorlevel 1 (
    echo.
    echo 错误: 未找到 Maven (mvn) 命令
    echo.
    echo 请选择以下方式之一：
    echo 1. 安装 Maven 并添加到 PATH
    echo    下载地址: https://maven.apache.org/download.cgi
    echo.
    echo 2. 使用 IDE (如 IntelliJ IDEA 或 Eclipse) 运行
    echo    主类: com.inventory.InventorySimulatorApplication
    echo.
    echo 3. 如果已安装 Maven，请确保已添加到系统 PATH 环境变量
    echo.
    pause
    exit /b 1
)

REM 启动 Spring Boot 应用
echo 使用 Maven 启动应用...
call mvn spring-boot:run

pause
