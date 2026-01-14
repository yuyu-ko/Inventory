# 故障排除指南

## 问题：localhost:8080 无法连接

### 1. 检查应用是否正在运行

**Windows:**
```powershell
# 检查端口占用
netstat -ano | findstr :8080

# 检查 Java 进程
tasklist | findstr java
```

**Linux/Mac:**
```bash
# 检查端口占用
lsof -i :8080
# 或
netstat -tuln | grep 8080

# 检查 Java 进程
ps aux | grep java
```

### 2. 检查应用启动日志

查看控制台输出，查找以下错误：

#### 常见错误 1: RabbitMQ 连接失败
```
Caused by: java.net.ConnectException: Connection refused
```

**解决方案：**
1. 确保 RabbitMQ 容器正在运行：
   ```bash
   docker ps
   ```
2. 如果没有运行，启动它：
   ```bash
   docker-compose up -d rabbitmq
   ```
3. 等待 RabbitMQ 完全启动（可能需要 30-60 秒）：
   ```bash
   docker logs -f inventory-rabbitmq
   ```
   等待看到 "Server startup complete" 消息

#### 常见错误 2: 端口被占用
```
Web server failed to start. Port 8080 was already in use.
```

**解决方案：**
1. 找到占用端口的进程并关闭它
2. 或修改 `application.yml` 中的端口：
   ```yaml
   server:
     port: 8081  # 改为其他端口
   ```

#### 常见错误 3: Maven 依赖下载失败
```
Could not resolve dependencies
```

**解决方案：**
1. 清理并重新下载依赖：
   ```bash
   mvn clean install -U
   ```
2. 或使用 IDE 的 Maven 刷新功能

### 3. 验证 RabbitMQ 状态

```bash
# 检查容器状态
docker ps | grep rabbitmq

# 检查 RabbitMQ 日志
docker logs inventory-rabbitmq

# 测试连接
docker exec inventory-rabbitmq rabbitmq-diagnostics ping
```

### 4. 手动启动步骤

如果自动启动脚本失败，可以手动启动：

1. **启动 RabbitMQ:**
   ```bash
   docker-compose up -d rabbitmq
   ```

2. **等待 RabbitMQ 就绪:**
   ```bash
   # 查看日志直到看到 "Server startup complete"
   docker logs -f inventory-rabbitmq
   ```

3. **启动应用:**
   ```bash
   # 使用 Maven
   mvn spring-boot:run
   
   # 或使用 IDE 运行 InventorySimulatorApplication
   ```

### 5. 检查应用健康状态

应用启动后，访问健康检查端点：

```bash
curl http://localhost:8080/api/health
```

应该返回：
```json
{
  "status": "UP",
  "application": "Inventory Simulator",
  "rabbitmq": "CONNECTED"
}
```

如果 `rabbitmq` 显示 `DISCONNECTED`，说明 RabbitMQ 连接有问题。

### 6. 测试 RabbitMQ 连接

```bash
# 访问 RabbitMQ 管理界面
# 浏览器打开: http://localhost:15672
# 用户名: guest
# 密码: guest

# 如果无法访问，检查容器是否运行
docker ps
```

### 7. 常见问题解决

#### 问题：应用启动但立即退出

**可能原因：**
- RabbitMQ 连接失败导致启动失败
- 配置错误

**解决方案：**
1. 检查启动日志中的完整错误信息
2. 确保 RabbitMQ 在应用启动前已完全就绪
3. 检查 `application.yml` 配置是否正确

#### 问题：应用启动成功但无法访问 API

**可能原因：**
- 防火墙阻止
- 应用绑定到错误的地址

**解决方案：**
1. 检查应用日志确认启动端口
2. 尝试访问：`http://127.0.0.1:8080/api/health`
3. 检查防火墙设置

#### 问题：RabbitMQ 容器无法启动

**可能原因：**
- 端口被占用
- Docker 资源不足

**解决方案：**
1. 检查端口占用：
   ```bash
   # Windows
   netstat -ano | findstr :5672
   netstat -ano | findstr :15672
   
   # Linux/Mac
   lsof -i :5672
   lsof -i :15672
   ```

2. 检查 Docker 资源：
   ```bash
   docker system df
   docker stats
   ```

3. 重启 Docker Desktop

### 8. 调试模式启动

如果需要更详细的日志，可以设置日志级别：

在 `application.yml` 中添加：
```yaml
logging:
  level:
    root: INFO
    com.inventory: DEBUG
    org.springframework.amqp: DEBUG
    org.springframework.boot: DEBUG
```

### 9. 重置环境

如果问题持续，可以尝试完全重置：

```bash
# 停止并删除容器
docker-compose down -v

# 清理 Maven 构建
mvn clean

# 重新启动
docker-compose up -d rabbitmq
# 等待 30 秒
mvn spring-boot:run
```

### 10. 获取帮助

如果以上步骤都无法解决问题，请提供以下信息：

1. 完整的启动日志
2. `docker ps` 输出
3. `docker logs inventory-rabbitmq` 输出
4. `curl http://localhost:8080/api/health` 输出
5. 操作系统和 Java 版本
