# 快速启动指南

## ✅ 当前状态检查

### 1. RabbitMQ 状态 ✅
从你的终端输出可以看到：
```
CONTAINER ID   IMAGE                   STATUS
6df4edc1f538   rabbitmq:3-management   Up 3 minutes (healthy)
```

**RabbitMQ 已正常运行！** ✅

### 2. 下一步：启动 Spring Boot 应用

你有几个选择：

#### 方式 A: 使用启动脚本（如果已安装 Maven）
```powershell
.\start.bat
```

#### 方式 B: 使用 Maven 命令（如果已安装 Maven）
```powershell
mvn spring-boot:run
```

#### 方式 C: 使用 IDE（推荐，无需 Maven）
1. 打开 IntelliJ IDEA 或 Eclipse
2. 打开项目文件夹
3. 找到 `src/main/java/com/inventory/InventorySimulatorApplication.java`
4. 右键 → Run 'InventorySimulatorApplication'

### 3. 验证应用启动

启动后，你应该在控制台看到类似输出：
```
Started InventorySimulatorApplication in X.XXX seconds
✅ RabbitMQ connection established
```

### 4. 测试应用

应用启动后，打开浏览器访问：

**健康检查：**
```
http://localhost:8080/api/health
```

应该返回：
```json
{
  "status": "UP",
  "application": "Inventory Simulator",
  "rabbitmq": "CONNECTED"
}
```

**订单列表：**
```
http://localhost:8080/api/orders
```

**库存查询：**
```
http://localhost:8080/api/inventory/SKU-001
```

### 5. 查看 RabbitMQ 管理界面

```
http://localhost:15672
```
- 用户名: `guest`
- 密码: `guest`

在这里你可以看到：
- 队列状态
- 消息流量
- 连接信息

## 🔍 如果应用无法启动

### 检查 1: 查看启动日志
查看控制台输出的完整错误信息

### 检查 2: 确认端口未被占用
```powershell
netstat -ano | findstr :8080
```

### 检查 3: 确认 Java 版本
```powershell
java -version
```
应该显示 Java 17 或更高版本

### 检查 4: 如果使用 Maven，确认 Maven 可用
```powershell
mvn -version
```

## 📝 常见问题

**Q: 应用启动但立即退出？**
- 检查控制台日志中的错误信息
- 确保 RabbitMQ 连接成功

**Q: 端口 8080 被占用？**
- 修改 `application.yml` 中的 `server.port` 为其他端口（如 8081）

**Q: 找不到 Maven 命令？**
- 使用 IDE 运行（最简单）
- 或安装 Maven 并添加到 PATH

## 🎯 下一步

一旦应用成功启动：
1. 应用会自动每 5 秒生成订单
2. 订单会被自动处理
3. 库存会被自动管理
4. 可以通过 API 查看订单和库存状态

查看日志可以看到：
- Order Injector 生成订单
- Order Manager 处理订单
- Inventory Manager 更新库存
