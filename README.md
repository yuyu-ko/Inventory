# 库存管理模拟器

基于 Spring Boot 和 RabbitMQ 的库存管理模拟系统，用于模拟订单处理、库存管理和订单处理的完整流程。

系统使用 **长格式 CSV** 作为可重放的订单来源。系统在启动时将 CSV 聚合成订单事件流，并通过**模拟时钟系统**以可配置的时间范围（开始时间/结束时间/加速因子）驱动事件注入，从而可重现真实世界的订单到达模式并验证库存一致性与处理流程。

> **📚 学生项目指南**: 这是一个适合学生学习的项目。  
> ⚠️ **注意**：这是需要**学生自己实现**的项目，项目提供架构设计和实现指南，但不提供完整代码。  
> 📖 详细的学习指南请查看：[docs/HOW_TO_START.md](docs/HOW_TO_START.md) 和 [docs/STUDENT_GUIDE.md](docs/STUDENT_GUIDE.md)

## 功能特性

- ✅ **SimulationClock (模拟时钟)**: 管理模拟时间，支持时间范围配置和加速运行
- ✅ **Order Injector (订单注入器)**: 从 CSV 文件读取订单，根据模拟时间顺序注入
- ✅ **Order Manager (订单处理器)**: 接收订单、检查库存、处理订单
- ✅ **Inventory Manager (库存管理器)**: 管理库存、预留、扣除、自动补货

## 技术栈

- Spring Boot 3.2.0
- RabbitMQ (Spring AMQP)
- H2 Database (内存数据库)
- Spring Data JPA
- OpenCSV (CSV 文件读取)
- Maven
- Java 17

## 快速开始

### 前置要求

- Java 17+
- Maven 3.6+ (可选，如果使用 IDE 可以不需要)
- Docker (用于运行 RabbitMQ)

> **注意**: 如果没有安装 Maven，可以使用 IDE (IntelliJ IDEA/Eclipse) 直接运行项目。  
> 详细设置说明请参考: [SETUP.md](SETUP.md)

### 启动步骤

1. **启动 RabbitMQ**
   ```bash
   docker-compose up -d rabbitmq
   ```
   或
   ```bash
   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   ```

2. **准备订单 CSV 文件**
   - 编辑 `src/main/resources/data/orders_sample.csv`
   - 格式说明参考: [docs/CSV_ORDER_FORMAT.md](docs/CSV_ORDER_FORMAT.md)

3. **配置模拟参数**（可选）
   编辑 `src/main/resources/application.yml`:
   ```yaml
   inventory:
     simulation:
       sim-start-time: "2024-01-13T08:00:00"  # 模拟开始时间
       sim-end-time: "2024-01-13T18:00:00"    # 模拟结束时间
       tick-seconds: 1                        # 每次 tick 增加的秒数
       speed-factor: 1.0                      # 加速因子（1.0 = 正常速度）
   ```

4. **运行应用**
   ```bash
   mvn spring-boot:run
   ```
   或使用 IDE 运行 `InventorySimulatorApplication`

5. **查看运行日志**
   - 日志格式：`[HH:mm:ss] ord-000001 completed successfully`
   - 模拟时钟会在配置的时间范围内运行
   - 订单会按照 CSV 文件中的时间顺序处理

6. **访问服务**
   - 应用: http://localhost:8080
   - H2 控制台: http://localhost:8080/h2-console
   - RabbitMQ 管理界面: http://localhost:15672 (guest/guest)

## 故障排除

如果遇到 `localhost:8080 无法连接` 的问题，请参考 [TROUBLESHOOTING.md](TROUBLESHOOTING.md) 获取详细的故障排除指南。

**快速检查：**
1. 确认应用正在运行（查看控制台日志）
2. 确认 RabbitMQ 容器运行中：`docker ps`
3. 访问健康检查：`http://localhost:8080/api/health`

## API 使用

### 健康检查
```bash
# 检查应用和 RabbitMQ 连接状态
curl http://localhost:8080/api/health
```

### 查询订单
```bash
# 获取所有订单
curl http://localhost:8080/api/orders

# 获取指定订单
curl http://localhost:8080/api/orders/ORD-000001
```

### 查询库存
```bash
# 获取指定SKU的库存
curl http://localhost:8080/api/inventory/SKU-001

# 初始化库存
curl -X POST "http://localhost:8080/api/inventory/initialize?sku=SKU-001&quantity=1000&temperatureZone=AMBIENT"
```

## 系统架构

```
SimulationClock → Order Injector → RabbitMQ → Order Manager → Inventory Manager
      ↓                  ↓             ↓              ↓                ↓
   模拟时钟          CSV 读取        消息队列       订单处理         库存管理
```

详细设计文档请参考: [docs/SYSTEM_DESIGN.md](docs/SYSTEM_DESIGN.md)

## 项目结构

```
src/main/java/com/inventory/
├── InventorySimulatorApplication.java  # 主应用类
├── config/
│   └── RabbitMQConfig.java             # RabbitMQ 配置
├── controller/
│   ├── OrderController.java            # 订单 REST API
│   ├── InventoryController.java        # 库存 REST API
│   └── HealthController.java           # 健康检查 API
├── message/
│   ├── OrderReceivedMessage.java       # 订单接收消息
│   ├── InventoryUpdateMessage.java     # 库存更新消息
│   └── OrderProcessedMessage.java      # 订单处理消息
├── model/
│   ├── Order.java                      # 订单实体
│   ├── OrderItem.java                  # 订单项实体
│   ├── InventoryItem.java              # 库存项实体
│   └── OrderCSVRecord.java             # CSV 订单记录
├── repository/
│   ├── OrderRepository.java            # 订单仓库
│   └── InventoryItemRepository.java    # 库存仓库
└── service/
    ├── SimulationClock.java            # 模拟时钟
    ├── SimulationRunner.java           # 模拟运行器
    ├── OrderCSVReader.java             # CSV 订单读取器
    ├── OrderInjector.java              # 订单注入器
    ├── OrderManager.java               # 订单处理器
    └── InventoryManager.java           # 库存管理器
```

## 配置说明

主要配置在 `application.yml`:

```yaml
inventory:
  # 模拟时钟配置
  simulation:
    sim-start-time: "2024-01-13T08:00:00"  # 模拟开始时间
    sim-end-time: "2024-01-13T18:00:00"    # 模拟结束时间
    tick-seconds: 1                        # 每次 tick 增加的秒数
    tick-interval-ms: 1000                 # tick 间隔（毫秒）
    speed-factor: 1.0                      # 加速因子（1.0 = 正常，2.0 = 2倍速）
  
  # 订单注入器配置
  order-injector:
    use-csv: true                          # 使用 CSV 文件
    csv-file: data/orders_sample.csv       # CSV 文件路径
  
  # 库存配置
  inventory:
    initial-stock: 1000                    # 初始库存
    low-stock-threshold: 100               # 低库存阈值
    replenishment-quantity: 500            # 补货数量
```

## 模拟时钟系统

系统使用 **SimulationClock** 管理模拟时间：

- **sim-start-time**: 模拟开始时间（ISO 8601 格式）
- **sim-end-time**: 模拟结束时间（ISO 8601 格式）
- **tick-seconds**: 每次时钟 tick 增加的秒数（默认 1 秒）
- **speed-factor**: 加速因子（1.0 = 正常速度，2.0 = 2倍速）

模拟时钟会在配置的时间范围内运行，订单会根据 CSV 文件中的 `ORDER_PLACED_TIME` 和当前模拟时间进行比较，决定是否发送。

## CSV 订单格式

订单数据使用**长格式 CSV**（每行一个订单项）：

```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-001,2,AMBIENT
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-003,1,CHILLED
```

详细格式说明请参考: [docs/CSV_ORDER_FORMAT.md](docs/CSV_ORDER_FORMAT.md)

## 消息流程

1. **SimulationClock** 推进模拟时间
2. **Order Injector** 读取 CSV 文件，根据模拟时间发送订单到 `sim.order.received` 队列
3. **Order Manager** 接收订单，检查库存，发布库存预留请求到 `sim.inventory.update` 队列
4. **Inventory Manager** 处理库存操作（预留/扣除/补货）
5. **Order Manager** 完成订单处理，发布处理结果到 `sim.order.processed` 队列

## 日志输出

系统使用优化的日志格式，输出简洁清晰：

```
[08:00:00] ord-000001 received
[08:00:01] ord-000001 completed successfully
[08:00:02] ord-000002 received
[08:00:03] ord-000002 completed successfully
```

Hibernate SQL 日志已关闭，减少日志噪音。

## 开发说明

### 添加新的消息类型

1. 在 `message/` 包下创建新的消息类
2. 在 `RabbitMQConfig.java` 中添加队列和绑定配置
3. 在相应的 Service 中添加 `@RabbitListener` 方法

### 添加新的业务逻辑

1. 在相应的 Service 类中添加方法
2. 使用 `RabbitTemplate` 发布消息
3. 使用 `@RabbitListener` 监听消息

### 自定义模拟参数

在 `application.yml` 中修改 `inventory.simulation` 配置项即可调整模拟行为。

## 许可证

MIT License
