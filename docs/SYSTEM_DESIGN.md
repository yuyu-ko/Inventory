# 库存管理模拟器 - 系统设计文档

## 系统概述

本系统是一个基于 Spring Boot 和 RabbitMQ 的库存管理模拟器，用于模拟订单处理、库存管理和订单处理的完整流程。

系统使用**长格式 CSV** 作为可重放的订单来源，并通过**模拟时钟系统（SimulationClock）**以可配置的时间范围驱动事件注入，从而实现可重现的真实世界订单到达模式。

## 架构设计

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        库存管理模拟器                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
        ┌─────────────────────────────────────────┐
        │         RabbitMQ Message Broker          │
        │     (Topic Exchange: symbotic.simulation)│
        └─────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│ Order Injector│    │Order Manager │    │Inventory     │
│              │    │              │    │Manager       │
│ 订单注入器    │    │ 订单处理器    │    │ 库存管理器    │
└──────────────┘    └──────────────┘    └──────────────┘
        │                     │                     │
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Simulation  │    │  订单数据库   │    │  库存数据库   │
│    Clock     │    │  (H2/PostgreSQL)│  │  (H2/PostgreSQL)│
│  模拟时钟    │    └──────────────┘    └──────────────┘
└──────────────┘
        │
        ▼
┌──────────────┐
│   CSV File   │
│  订单数据源   │
└──────────────┘
```

## 核心组件

### 1. SimulationClock (模拟时钟)

**职责：**
- 管理模拟时间系统
- 提供时间范围控制和加速运行
- 作为系统的时间基准

**主要功能：**
- `initialize()`: 初始化模拟时钟（设置开始/结束时间）
- `tick()`: 推进模拟时间（根据 tickSeconds 和 speedFactor）
- `getCurrentTime()`: 获取当前模拟时间
- `isRunning()`: 检查模拟是否在运行
- `isTimeInRange()`: 检查时间是否在模拟范围内

**配置参数：**
- `sim-start-time`: 模拟开始时间（ISO 8601 格式）
- `sim-end-time`: 模拟结束时间（ISO 8601 格式）
- `tick-seconds`: 每次 tick 增加的秒数（默认 1）
- `speed-factor`: 加速因子（1.0 = 正常速度，2.0 = 2倍速）

### 2. SimulationRunner (模拟运行器)

**职责：**
- 定时调用 SimulationClock.tick()
- 控制模拟时钟的推进频率

**主要功能：**
- `runSimulationTick()`: 定时任务，定期 tick 模拟时钟

**配置参数：**
- `tick-interval-ms`: tick 间隔（毫秒，默认 1000ms）

### 3. OrderCSVReader (CSV 订单读取器)

**职责：**
- 从 CSV 文件读取订单数据
- 支持长格式 CSV（每行一个订单项）

**主要功能：**
- `readOrdersFromCSV()`: 读取并解析 CSV 文件

### 4. Order Injector (订单注入器)

**职责：**
- 从 CSV 文件加载订单
- 根据模拟时钟时间顺序注入订单到消息队列
- 仅加载模拟时间范围内的订单

**主要功能：**
- `initialize()`: 启动时从 CSV 加载订单
- `loadOrdersFromCSV()`: 加载并过滤订单（仅保留模拟时间范围内的订单）
- `injectOrders()`: 定时检查并发送到期的订单
- `publishOrder()`: 将订单发布到 RabbitMQ

**消息发布：**
- Exchange: `symbotic.simulation`
- Routing Key: `sim.order.received`
- Message Type: `OrderReceivedMessage`

**订单过滤逻辑：**
- 仅加载 `ORDER_PLACED_TIME` 在 `[simStartTime, simEndTime]` 范围内的订单
- 根据当前模拟时间发送订单（`ORDER_PLACED_TIME <= currentSimTime`）

### 5. Order Manager (订单处理器)

**职责：**
- 接收订单消息
- 检查库存可用性
- 处理订单（预留库存、扣除库存）
- 更新订单状态
- 发布订单处理结果

**主要功能：**
- `handleOrderReceived()`: 监听订单接收消息
- `checkAndReserveInventory()`: 检查并预留库存
- `processOrder()`: 处理订单（扣除库存、更新状态）

**消息监听：**
- Queue: `sim.order.received`
- Message Type: `OrderReceivedMessage`

**消息发布：**
- Exchange: `symbotic.simulation`
- Routing Key: `sim.inventory.update` (库存更新)
- Routing Key: `sim.order.processed` (订单处理完成)

**日志输出格式：**
- 成功：`[HH:mm:ss] ord-000001 completed successfully`
- 失败：`[HH:mm:ss] ord-000001 failed - insufficient inventory`

### 6. Monitoring System (监控系统)

**职责：**
- 收集应用指标（订单处理数量、成功率、处理时间等）
- 提供可视化 Dashboard
- 实时监控系统运行状况

**组件：**

1. **Spring Boot Actuator**
   - 暴露 `/actuator/prometheus` endpoint
   - 提供应用健康状态、指标数据

2. **Prometheus**
   - 定期抓取应用指标（scrape interval: 5s）
   - 存储时间序列数据
   - 提供 PromQL 查询接口

3. **Grafana**
   - 连接 Prometheus 数据源
   - 创建可视化 Dashboard
   - 监控关键指标：
     - 订单接收总数 (`orders_received_total`)
     - 订单处理总数（按状态：SUCCESS/FAILED/ERROR）
     - 订单处理成功率
     - 平均订单处理时间

**关键 Metrics：**
- `orders_received_total`: Counter，订单接收总数
- `orders_processed_total{status="SUCCESS|FAILED|ERROR"}`: Counter，按状态分类的订单处理总数
- `orders_processing_time_seconds`: Timer，订单处理时间（包含 count、sum、max）

**配置：**
- Prometheus 配置文件: `monitoring/prometheus.yml`
- Grafana 默认账号: admin/admin
- Prometheus 端口: 9090
- Grafana 端口: 3000

### 7. Inventory Manager (库存管理器)

**职责：**
- 管理库存数据
- 处理库存预留、释放、扣除、补货操作
- 自动检测低库存并触发补货
- 维护库存状态

**主要功能：**
- `handleInventoryUpdate()`: 监听库存更新消息
- `reserveInventory()`: 预留库存
- `releaseInventory()`: 释放预留库存
- `deductInventory()`: 扣除库存
- `replenishInventory()`: 补货
- `checkAndReplenish()`: 自动检测并补货

**消息监听：**
- Queue: `sim.inventory.update`
- Message Type: `InventoryUpdateMessage`

**操作类型：**
- `RESERVE`: 预留库存
- `RELEASE`: 释放预留库存
- `DEDUCT`: 扣除库存
- `REPLENISH`: 补货

## 消息流程

### 订单处理流程

```
1. SimulationRunner
   └─> 定期调用 SimulationClock.tick()
   └─> 推进模拟时间

2. Order Injector
   └─> 根据模拟时间检查订单
   └─> 发送到期的订单
   └─> 发布 OrderReceivedMessage
       └─> Exchange: symbotic.simulation
       └─> Routing Key: sim.order.received

3. Order Manager
   └─> 接收 OrderReceivedMessage
   └─> 创建订单实体
   └─> 检查库存
   └─> 发布 InventoryUpdateMessage (RESERVE)
       └─> Exchange: symbotic.simulation
       └─> Routing Key: sim.inventory.update

4. Inventory Manager
   └─> 接收 InventoryUpdateMessage
   └─> 预留库存
   └─> 检查是否需要补货

5. Order Manager
   └─> 处理订单
   └─> 发布 InventoryUpdateMessage (DEDUCT)
       └─> Exchange: symbotic.simulation
       └─> Routing Key: sim.inventory.update

6. Inventory Manager
   └─> 接收 InventoryUpdateMessage
   └─> 扣除库存
   └─> 检查是否需要补货

7. Order Manager
   └─> 更新订单状态为 COMPLETED
   └─> 发布 OrderProcessedMessage
       └─> Exchange: symbotic.simulation
       └─> Routing Key: sim.order.processed
   └─> 输出日志: [HH:mm:ss] ord-000001 completed successfully
```

## 数据模型

### Order (订单)
- `orderId`: 订单ID
- `orderType`: 订单类型 (PICKUP/DELIVERY)
- `status`: 订单状态 (PENDING/RECEIVED/PROCESSING/COMPLETED/CANCELLED)
- `orderPlacedTime`: 下单时间
- `orderDueTime`: 到期时间
- `items`: 订单项列表
- `customerId`: 客户ID

### OrderItem (订单项)
- `sku`: 商品SKU
- `quantity`: 数量
- `temperatureZone`: 温度区域 (AMBIENT/CHILLED/FROZEN)

### InventoryItem (库存项)
- `sku`: 商品SKU
- `name`: 商品名称
- `quantity`: 总库存数量
- `reservedQuantity`: 预留数量
- `temperatureZone`: 温度区域
- `lowStockThreshold`: 低库存阈值

### OrderCSVRecord (CSV 订单记录)
- `orderId`: 订单ID
- `orderType`: 订单类型
- `orderPlacedTime`: 下单时间（字符串格式）
- `orderDueTime`: 到期时间（字符串格式）
- `customerId`: 客户ID
- `sku`: 商品SKU
- `quantity`: 数量
- `temperatureZone`: 温度区域

## 消息类型

### OrderReceivedMessage
```json
{
  "orderId": "ORD-000001",
  "orderType": "PICKUP",
  "orderPlacedTime": "2024-01-13T08:00:00",
  "orderDueTime": "2024-01-13T12:00:00",
  "items": [
    {
      "sku": "SKU-001",
      "quantity": 2,
      "temperatureZone": "AMBIENT"
    }
  ],
  "customerId": "CUST-001",
  "senderId": "OrderInjector"
}
```

### InventoryUpdateMessage
```json
{
  "sku": "SKU-001",
  "quantityChange": 5,
  "reservedQuantityChange": 2,
  "operation": "RESERVE",
  "orderId": "ORD-000001"
}
```

### OrderProcessedMessage
```json
{
  "orderId": "ORD-000001",
  "status": "COMPLETED",
  "processedTime": "2024-01-13T08:00:01",
  "message": "Order processed successfully"
}
```

## 技术栈

- **框架**: Spring Boot 3.2.0
- **消息队列**: RabbitMQ (Spring AMQP)
- **数据库**: H2 (内存数据库，开发环境) / PostgreSQL (生产环境，Week 6 扩展)
- **ORM**: Spring Data JPA
- **CSV 处理**: OpenCSV
- **监控**: Spring Boot Actuator + Micrometer Prometheus
- **指标收集**: Prometheus
- **可视化**: Grafana
- **构建工具**: Maven
- **Java版本**: 17

## 配置说明

### application.yml 主要配置

```yaml
inventory:
  # 模拟时钟配置
  simulation:
    sim-start-time: "2024-01-13T08:00:00"  # 模拟开始时间
    sim-end-time: "2024-01-13T18:00:00"    # 模拟结束时间
    tick-seconds: 1                        # 每次 tick 增加的秒数
    tick-interval-ms: 1000                 # tick 间隔（毫秒）
    speed-factor: 1.0                      # 加速因子
  
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

## API 端点

### 订单相关
- `GET /api/orders` - 获取所有订单
- `GET /api/orders/{orderId}` - 获取指定订单

### 库存相关
- `GET /api/inventory/{sku}` - 获取指定SKU的库存
- `POST /api/inventory/initialize?sku=XXX&quantity=100&temperatureZone=AMBIENT` - 初始化库存

### 健康检查
- `GET /api/health` - 检查应用和 RabbitMQ 连接状态

## CSV 订单格式

系统使用**长格式 CSV**（每行一个订单项）：

```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-001,2,AMBIENT
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-003,1,CHILLED
```

详细格式说明请参考: [CSV_ORDER_FORMAT.md](CSV_ORDER_FORMAT.md)

## 运行说明

1. **启动 RabbitMQ**
   ```bash
   docker-compose up -d
   ```

2. **配置模拟参数**（可选）
   编辑 `application.yml` 中的 `inventory.simulation` 配置

3. **准备订单 CSV 文件**
   编辑 `src/main/resources/data/orders_sample.csv`

4. **运行应用**
   ```bash
   mvn spring-boot:run
   ```

5. **查看运行日志**
   - 日志格式：`[HH:mm:ss] ord-000001 completed successfully`
   - 模拟时钟会在配置的时间范围内运行

6. **访问 H2 控制台**
   - URL: http://localhost:8080/h2-console
   - JDBC URL: jdbc:h2:mem:inventorydb
   - Username: sa
   - Password: (空)

7. **访问 RabbitMQ 管理界面**
   - URL: http://localhost:15672
   - Username: guest
   - Password: guest

8. **访问监控系统**
   - Prometheus UI: http://localhost:9090
   - Grafana Dashboard: http://localhost:3000
   - 默认账号: admin/admin
   - 在 Grafana 中添加 Prometheus 数据源（URL: `http://prometheus:9090`）

## 扩展功能（Week 6 - 可选）

### 数据库迁移到 PostgreSQL

**为什么需要迁移？**

项目初始使用 **H2 内存数据库**，适合快速开发和学习：
- ✅ 无需安装配置，启动快速
- ✅ 适合学习和原型开发

但 H2 的限制：
- ❌ 数据不持久化（应用重启数据丢失）
- ❌ 不适合生产环境
- ❌ 扩展性有限

**迁移到 PostgreSQL 的优势**：
- ✅ **数据持久化**：数据不会因应用重启而丢失
- ✅ **扩展性**：支持更大数据量和并发访问
- ✅ **生产就绪**：适合部署到生产环境
- ✅ **性能优化**：支持索引、查询优化等高级功能

**迁移步骤**：
1. 添加 PostgreSQL 依赖到 `pom.xml`
2. 在 `docker-compose.yml` 中添加 PostgreSQL 服务
3. 更新 `application.yml` 数据库配置
4. 配置数据库连接池（HikariCP）
5. 测试数据持久化功能

详细说明请参考 [WEEKLY_PLAN.md](WEEKLY_PLAN.md) 的 Week 6 部分。

## 系统特点

1. **模拟时钟系统**: 使用 SimulationClock 管理模拟时间，支持时间范围配置和加速运行
2. **CSV 订单来源**: 使用长格式 CSV 文件，可扩展性强，支持任意 SKU
3. **消息驱动架构**: 使用 RabbitMQ 实现组件间解耦
4. **异步处理**: 订单处理和库存管理异步进行
5. **自动补货**: 库存低于阈值时自动触发补货
6. **事务支持**: 使用 Spring 事务管理确保数据一致性
7. **优化的日志**: 简洁的日志格式，减少噪音
8. **可扩展性**: 易于添加新的消息处理者和业务逻辑
9. **监控与可视化**: 集成 Prometheus + Grafana，实时监控订单处理指标、成功率、处理时间等

## 扩展建议

### Week 6: 数据库迁移到 PostgreSQL（推荐）

**目标**：将系统从 H2 迁移到 PostgreSQL，提升系统扩展性和生产就绪性

**优势**：
- ✅ 数据持久化，应用重启不丢失数据
- ✅ 支持更大数据量和并发访问
- ✅ 适合部署到生产环境
- ✅ 学习生产级数据库配置和使用

**实施步骤**：
1. 添加 PostgreSQL 依赖
2. 在 docker-compose.yml 中添加 PostgreSQL 服务
3. 更新 application.yml 数据库配置
4. 测试数据持久化功能

详细说明请参考 [WEEKLY_PLAN.md](WEEKLY_PLAN.md) 的 Week 6 部分。

### Week 7: 测试与 API 文档（可选）

**目标**：提升代码质量和 API 可用性

**任务清单**：
- 单元测试和集成测试编写
- Swagger/OpenAPI API 文档生成
- Redis 缓存优化（可选）

**学习价值**：
- 理解测试驱动开发（TDD）
- 掌握 API 文档编写规范
- 学习缓存优化策略

详细说明请参考 [WEEKLY_PLAN.md](WEEKLY_PLAN.md) 的 Week 7 部分。

### Week 8: 部署与优化（可选）

**目标**：实现生产就绪的部署方案

**任务清单**：
- Docker 容器化应用
- CI/CD 流程配置
- 性能测试与优化

**学习价值**：
- 理解容器化部署
- 掌握 CI/CD 流程
- 学习性能优化方法

详细说明请参考 [WEEKLY_PLAN.md](WEEKLY_PLAN.md) 的 Week 8 部分。

### 其他扩展建议（可选）

1. **添加更多组件**: 如 Pick Station、Tote Manager 等
2. **分布式追踪**: 集成 Zipkin 或 Jaeger
3. **安全增强**: 添加认证授权、API 安全等
4. **消息重试机制**: 实现消息处理失败重试
5. **批量处理优化**: 实现批量订单处理
6. **模拟时间同步**: 支持多实例之间的模拟时间同步