#!/bin/bash

echo "=========================================="
echo "Starting Inventory Management Simulator"
echo "=========================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Error: Docker is not running, please start Docker first"
    exit 1
fi

# Start RabbitMQ
echo "1. Starting RabbitMQ..."
docker-compose up -d rabbitmq

# Wait for RabbitMQ to be ready
echo "2. Waiting for RabbitMQ to be ready..."
sleep 10

# Check if RabbitMQ is ready
until docker exec inventory-rabbitmq rabbitmq-diagnostics ping > /dev/null 2>&1; do
    echo "   Waiting for RabbitMQ to start..."
    sleep 2
done

echo "3. RabbitMQ is ready!"
echo ""
echo "=========================================="
echo "Starting Spring Boot application..."
echo "=========================================="

# Start Spring Boot application
mvn spring-boot:run
