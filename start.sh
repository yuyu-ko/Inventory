#!/bin/bash

echo "=========================================="
echo "启动库存管理模拟器"
echo "=========================================="

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "错误: Docker 未运行，请先启动 Docker"
    exit 1
fi

# 启动 RabbitMQ
echo "1. 启动 RabbitMQ..."
docker-compose up -d rabbitmq

# 等待 RabbitMQ 就绪
echo "2. 等待 RabbitMQ 就绪..."
sleep 10

# 检查 RabbitMQ 是否就绪
until docker exec inventory-rabbitmq rabbitmq-diagnostics ping > /dev/null 2>&1; do
    echo "   等待 RabbitMQ 启动..."
    sleep 2
done

echo "3. RabbitMQ 已就绪！"
echo ""
echo "=========================================="
echo "启动 Spring Boot 应用..."
echo "=========================================="

# 启动 Spring Boot 应用
mvn spring-boot:run
