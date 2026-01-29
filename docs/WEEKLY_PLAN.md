# 四周完成计划详细版

## 📅 Week 1: 环境搭建与基础架构

### Day 1-2: 项目准备

#### 任务清单
- [ ] **环境搭建**
  - 安装 Java 17 JDK
  - 安装 Maven（或使用 IDE 内置）
  - 安装 IDE（推荐 IntelliJ IDEA）
  - 安装 Docker Desktop
  - 验证安装：`java -version`, `mvn -version`, `docker --version`

- [ ] **项目初始化**
  - 使用 Spring Initializr 创建项目（或从模板开始）
  - 配置 `pom.xml` 添加依赖
  - 创建基础包结构
  - 运行应用，确认环境正常

- [ ] **理解项目**
  - 阅读 README.md
  - 阅读 SYSTEM_DESIGN.md
  - 理解系统架构图
  - 明确项目目标

#### 学习目标
- 掌握 Spring Boot 项目创建
- 理解 Maven 依赖管理
- 熟悉开发环境

#### 交付物
- ✅ 可以运行的 Spring Boot 项目
- ✅ 配置文件完整
- ✅ 理解项目需求

---

### Day 3-4: 数据模型设计

#### 任务清单
- [ ] **设计 Order 实体**
  ```java
  @Entity
  public class Order {
      @Id
      @GeneratedValue
      private Long id;
      private String orderId;
      private OrderType orderType;
      private OrderStatus status;
      private LocalDateTime orderPlacedTime;
      private LocalDateTime orderDueTime;
      @OneToMany
      private List<OrderItem> items;
      // ... getters/setters
  }
  ```

- [ ] **设计 OrderItem 实体**
- [ ] **设计 InventoryItem 实体**
- [ ] **创建 Repository 接口**
- [ ] **配置数据库连接**
- [ ] **测试数据库操作**

#### 学习目标
- 掌握 JPA 实体设计
- 理解实体关系映射
- 掌握 Repository 使用

#### 交付物
- ✅ 完整的实体类定义
- ✅ Repository 接口
- ✅ 数据库表能够自动创建
- ✅ 能够进行基本的 CRUD 操作

---

### Day 5-7: RabbitMQ 集成

#### 任务清单
- [ ] **安装 RabbitMQ**
  ```bash
  docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
  ```

- [ ] **配置 RabbitMQ 连接**
  ```yaml
  spring:
    rabbitmq:
      host: localhost
      port: 5672
      username: guest
      password: guest
  ```

- [ ] **创建配置类**
  - Exchange（Topic Exchange）
  - Queue（3个队列）
  - Binding（绑定关系）

- [ ] **实现简单的消息发送/接收**
  - 创建一个测试 Controller
  - 发送消息
  - 接收消息
  - 验证消息传递

#### 学习目标
- 理解消息队列概念
- 掌握 RabbitMQ 基本使用
- 理解 Exchange、Queue、Binding

#### 交付物
- ✅ RabbitMQ 正常运行
- ✅ 能够发送和接收消息
- ✅ 理解消息队列工作原理

---

## 📅 Week 2: 核心功能实现

### Day 8-10: Order Manager 实现

#### 任务清单
- [ ] **创建消息类**
  - `OrderReceivedMessage`
  - `OrderProcessedMessage`
  - `InventoryUpdateMessage`

- [ ] **实现 OrderManager 服务**
  - `handleOrderReceived()` - 接收订单
  - `createOrderFromMessage()` - 创建订单实体
  - `checkAndReserveInventory()` - 检查库存
  - `processOrder()` - 处理订单

- [ ] **实现订单状态管理**
  - PENDING → RECEIVED → PROCESSING → COMPLETED
  - 失败情况：CANCELLED

- [ ] **测试功能**
  - 手动发送测试消息
  - 验证订单创建
  - 验证状态更新

#### 关键代码提示
```java
@RabbitListener(queues = "sim.order.received")
public void handleOrderReceived(OrderReceivedMessage message) {
    // 1. 创建订单
    Order order = createOrderFromMessage(message);
    orderRepository.save(order);
    
    // 2. 检查库存（TODO: 下一阶段实现）
    // 3. 处理订单
}
```

#### 交付物
- ✅ OrderManager 基本功能实现
- ✅ 能够接收和处理订单消息
- ✅ 订单数据正确保存

---

### Day 11-13: Inventory Manager 实现

#### 任务清单
- [ ] **实现 InventoryManager 服务**
  - `handleInventoryUpdate()` - 处理库存更新消息
  - `getOrCreateInventoryItem()` - 获取或创建库存项
  - `reserveInventory()` - 预留库存
  - `deductInventory()` - 扣除库存
  - `replenishInventory()` - 补货
  - `checkAndReplenish()` - 自动补货检查

- [ ] **实现库存操作**
  - RESERVE: 预留库存（订单确认时）
  - DEDUCT: 扣除库存（订单完成时）
  - REPLENISH: 补货（库存不足时）

- [ ] **集成测试**
  - 测试预留功能
  - 测试扣除功能
  - 测试自动补货

#### 关键逻辑
```java
// 预留库存
if (availableQuantity >= requestedQuantity) {
    reservedQuantity += requestedQuantity;
    // 保存
}

// 扣除库存
quantity -= quantityToDeduct;
reservedQuantity -= quantityFromReserved;

// 自动补货
if (quantity <= lowStockThreshold) {
    quantity += replenishmentQuantity;
}
```

#### 交付物
- ✅ InventoryManager 完整实现
- ✅ 库存操作功能正常
- ✅ 自动补货功能正常

---

### Day 14: 集成测试

#### 任务清单
- [ ] **完整流程测试**
  1. Order Injector 发送订单（暂时手动发送测试）
  2. Order Manager 接收订单
  3. 库存预留
  4. 订单处理
  5. 库存扣除
  6. 订单完成

- [ ] **问题修复**
  - 修复发现的 Bug
  - 优化代码逻辑
  - 添加必要的日志

#### 交付物
- ✅ 核心功能完整运行
- ✅ 集成测试通过
- ✅ 代码质量良好

---

## 📅 Week 3: 订单注入与模拟时钟

### Day 15-17: CSV 订单读取

#### 任务清单
- [ ] **添加 OpenCSV 依赖**
  ```xml
  <dependency>
      <groupId>com.opencsv</groupId>
      <artifactId>opencsv</artifactId>
      <version>5.9</version>
  </dependency>
  ```

- [ ] **创建 OrderCSVRecord 类**
  ```java
  @Data
  public class OrderCSVRecord {
      @CsvBindByName(column = "ORDER_ID")
      private String orderId;
      @CsvBindByName(column = "SKU")
      private String sku;
      // ... 其他字段
  }
  ```

- [ ] **实现 OrderCSVReader**
  - 读取 CSV 文件
  - 解析为 OrderCSVRecord 列表
  - 处理异常情况

- [ ] **准备测试 CSV 文件**
  - 创建示例订单数据
  - 验证 CSV 格式正确

#### CSV 格式示例
```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-001,2,AMBIENT
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-003,1,CHILLED
```

#### 交付物
- ✅ CSV 读取功能实现
- ✅ 数据解析正确
- ✅ 测试数据准备完成

---

### Day 18-19: SimulationClock 实现

#### 任务清单
- [ ] **创建 SimulationClock 类**
  ```java
  @Component
  public class SimulationClock {
      private LocalDateTime currentSimTime;
      private LocalDateTime simStartTime;
      private LocalDateTime simEndTime;
      
      public void tick() {
          currentSimTime = currentSimTime.plusSeconds(tickSeconds * speedFactor);
      }
  }
  ```

- [ ] **配置参数**
  - simStartTime
  - simEndTime
  - tickSeconds
  - speedFactor

- [ ] **创建 SimulationRunner**
  - 定时调用 tick()
  - 控制模拟时间推进

- [ ] **测试功能**
  - 时间初始化正确
  - 时间推进正常
  - 加速因子工作正常

#### 交付物
- ✅ SimulationClock 实现
- ✅ 时间推进功能正常
- ✅ 配置灵活

---

### Day 20-21: Order Injector 实现

#### 任务清单
- [ ] **实现 OrderInjector**
  - `initialize()` - 启动时加载 CSV
  - `loadOrdersFromCSV()` - 加载订单
  - `convertToOrderMessage()` - 转换订单格式
  - `injectOrders()` - 定时注入订单
  - `publishOrder()` - 发布订单消息

- [ ] **订单过滤逻辑**
  - 仅加载模拟时间范围内的订单
  - 根据当前模拟时间发送订单

- [ ] **集成测试**
  - 订单能够正确加载
  - 订单能够按时间发送
  - 日志输出清晰

#### 关键逻辑
```java
// 加载订单时过滤
orders.stream()
    .filter(order -> simulationClock.isTimeInRange(order.getOrderPlacedTime()))
    .collect(Collectors.toList());

// 发送订单时检查
if (!order.getOrderPlacedTime().isAfter(simulationClock.getCurrentTime())) {
    publishOrder(order);
}
```

#### 交付物
- ✅ OrderInjector 完整实现
- ✅ 订单注入功能正常
- ✅ 与模拟时钟集成良好

---

## 📅 Week 4: 完善与优化

### Day 22-24: REST API 实现

#### 任务清单
- [ ] **OrderController**
  ```java
  @RestController
  @RequestMapping("/api/orders")
  public class OrderController {
      @GetMapping
      public List<Order> getAllOrders() { }
      
      @GetMapping("/{orderId}")
      public Order getOrder(@PathVariable String orderId) { }
  }
  ```

- [ ] **InventoryController**
  - GET /api/inventory/{sku}
  - POST /api/inventory/initialize

- [ ] **HealthController**
  - GET /api/health

- [ ] **API 测试**
  - 使用 Postman 或 curl 测试
  - 验证返回数据格式
  - 测试错误处理

#### 交付物
- ✅ 所有 API 端点实现
- ✅ API 测试通过
- ✅ 返回格式正确

---

### Day 25-26: 日志优化与测试

#### 任务清单
- [ ] **优化日志配置**
  ```yaml
  logging:
    level:
      org.hibernate: WARN
      com.inventory: INFO
    pattern:
      console: "[%d{yyyy-MM-dd HH:mm:ss}] %msg%n"
  ```

- [ ] **优化日志输出**
  - 订单处理日志：`[HH:mm:ss] ord-000001 completed successfully`
  - 减少不必要的日志
  - 关键操作添加日志

- [ ] **完整功能测试**
  - 端到端测试
  - 性能测试（可选）
  - 边界情况测试

- [ ] **Bug 修复**
  - 修复发现的问题
  - 代码优化
  - 代码审查

#### 交付物
- ✅ 日志输出清晰
- ✅ 功能测试通过
- ✅ 代码质量良好

---

### Day 27-28: 文档编写与项目总结

#### 任务清单
- [ ] **更新文档**
  - README.md
  - 代码注释
  - API 文档

- [ ] **项目总结**
  - 功能演示
  - 遇到的问题和解决方案
  - 学习收获

- [ ] **代码整理**
  - 代码格式统一
  - 删除调试代码
  - 最终代码审查

- [ ] **项目演示准备**
  - 准备演示脚本
  - 准备演示数据
  - 准备 PPT（可选）

#### 交付物
- ✅ 文档完整
- ✅ 代码整理完成
- ✅ 能够进行演示
- ✅ 项目报告完成

---

## 📅 Week 5: 监控与可视化（进阶）

### Day 29-31: Spring Boot Actuator 集成

#### 任务清单
- [ ] **添加 Actuator 依赖**
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
  </dependency>
  <dependency>
      <groupId>io.micrometer</groupId>
      <artifactId>micrometer-registry-prometheus</artifactId>
  </dependency>
  ```

- [ ] **配置 Actuator**
  ```yaml
  management:
    endpoints:
      web:
        exposure:
          include: "health,info,prometheus"
    endpoint:
      prometheus:
        enabled: true
    metrics:
      tags:
        application: ${spring.application.name}
  ```

- [ ] **验证 Metrics 端点**
  - 访问 http://localhost:8080/actuator/prometheus
  - 确认能够看到 metrics 输出

#### 学习目标
- 理解 Spring Boot Actuator 的作用
- 掌握 Metrics 的暴露方式
- 理解 Prometheus 格式的 metrics

#### 交付物
- ✅ Actuator 配置完成
- ✅ Prometheus metrics 端点可用
- ✅ 能够查看应用指标

---

### Day 32-33: 自定义 Metrics 实现

#### 任务清单
- [ ] **在 OrderManager 中添加 Metrics**
  - 注入 `MeterRegistry`
  - 记录订单接收总数 (`orders_received_total`)
  - 记录订单处理总数（按状态：SUCCESS/FAILED/ERROR）
  - 记录订单处理时间 (`orders_processing_time_seconds`)

- [ ] **实现 Metrics 记录**
  ```java
  @Service
  @RequiredArgsConstructor
  public class OrderManager {
      private final MeterRegistry meterRegistry;
      
      public void handleOrderReceived(...) {
          meterRegistry.counter("orders_received_total").increment();
          Timer.Sample sample = Timer.start(meterRegistry);
          
          try {
              // 处理订单
              meterRegistry.counter("orders_processed_total", 
                  "status", "SUCCESS").increment();
          } catch (Exception e) {
              meterRegistry.counter("orders_processed_total", 
                  "status", "ERROR").increment();
          } finally {
              sample.stop(Timer.builder("orders_processing_time")
                  .register(meterRegistry));
          }
      }
  }
  ```

- [ ] **测试 Metrics**
  - 处理一些订单
  - 查看 `/actuator/prometheus` 端点
  - 确认 metrics 值正确更新

#### 学习目标
- 掌握 Micrometer 的使用
- 理解 Counter、Timer 等 metric 类型
- 学会自定义业务指标

#### 交付物
- ✅ 自定义 metrics 实现完成
- ✅ Metrics 数据正确记录
- ✅ 能够通过端点查看指标

---

### Day 34-35: Prometheus + Grafana 部署

#### 任务清单
- [ ] **配置 Prometheus**
  - 创建 `monitoring/prometheus.yml` 配置文件
  - 配置 scrape 目标（Spring Boot 应用）
  - 设置 scrape interval

- [ ] **更新 docker-compose.yml**
  ```yaml
  prometheus:
    image: prom/prometheus:latest
    container_name: inventory-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
  
  grafana:
    image: grafana/grafana:latest
    container_name: inventory-grafana
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_USER: admin
      GF_SECURITY_ADMIN_PASSWORD: admin
  ```

- [ ] **启动监控服务**
  ```bash
  docker-compose up -d prometheus grafana
  ```

- [ ] **配置 Grafana**
  - 访问 http://localhost:3000
  - 添加 Prometheus 数据源（URL: `http://prometheus:9090`）
  - 测试连接

#### Prometheus 配置示例
```yaml
global:
  scrape_interval: 5s

scrape_configs:
  - job_name: 'inventory-simulator'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
```

#### 学习目标
- 理解 Prometheus 的工作原理
- 掌握 Prometheus 配置
- 理解 Grafana 与 Prometheus 的集成

#### 交付物
- ✅ Prometheus 正常运行
- ✅ Grafana 正常运行
- ✅ 数据源配置完成

---

### Day 36-37: Grafana Dashboard 创建

#### 任务清单
- [ ] **创建订单处理 Dashboard**
  - 订单接收总数（Stat Panel）- 使用 PromQL 或 LogQL
  - 订单成功/失败数（Time Series）- 使用 PromQL 或 LogQL
  - 订单成功率（Gauge）- 使用 PromQL 或 LogQL
  - 平均订单处理时间（Time Series）- 使用 PromQL
  - 订单日志查看（Logs Panel）- 使用 LogQL

- [ ] **常用 PromQL 查询（Prometheus 指标）**
  ```promql
  # 订单接收总数（时间范围内）
  sum(increase(orders_received_total[$__range]))
  
  # 订单成功数
  sum(increase(orders_processed_total{status="SUCCESS"}[$__range]))
  
  # 订单失败数
  sum(increase(orders_processed_total{status="FAILED"}[$__range]))
  
  # 订单成功率（百分比）
  sum(rate(orders_processed_total{status="SUCCESS"}[5m])) 
  / sum(rate(orders_processed_total[5m])) * 100
  
  # 平均处理时间（秒）
  sum(increase(orders_processing_time_seconds_sum[$__range])) 
  / sum(increase(orders_processing_time_seconds_count[$__range]))
  ```

- [ ] **常用 LogQL 查询（Loki 日志）**
  ```logql
  # 成功订单数量（时间范围内）
  sum(
    count_over_time(
      {application="inventory-simulator"} 
      |= "ORDER_COMPLETED" 
      [5m]
    )
  )
  
  # 失败订单数量（时间范围内）
  sum(
    count_over_time(
      {application="inventory-simulator"} 
      |= "ORDER_FAILED" 
      [5m]
    )
  )
  
  # 总订单数量（成功+失败）
  sum(
    count_over_time(
      {application="inventory-simulator"} 
      |~ "ORDER_COMPLETED|ORDER_FAILED" 
      [5m]
    )
  )
  
  # 成功率百分比
  (
    sum(count_over_time({application="inventory-simulator"} |= "ORDER_COMPLETED" [5m]))
    /
    sum(count_over_time({application="inventory-simulator"} |~ "ORDER_COMPLETED|ORDER_FAILED" [5m]))
  ) * 100
  
  # 查看所有成功订单日志
  {application="inventory-simulator"} |= "ORDER_COMPLETED"
  
  # 查看所有失败订单日志
  {application="inventory-simulator"} |= "ORDER_FAILED"
  ```
  
  > 注意：时间范围 `[5m]` 会根据 Grafana 的时间选择器自动调整

- [ ] **优化 Dashboard**
  - 设置合适的刷新间隔
  - 配置告警规则（可选）
  - 美化图表样式

#### 学习目标
- 掌握 Grafana Dashboard 创建
- 理解 PromQL 查询语言
- 学会可视化指标数据

#### 交付物
- ✅ Dashboard 创建完成
- ✅ 关键指标可视化
- ✅ Dashboard 美观实用

---

### Day 38: 监控系统测试与文档

#### 任务清单
- [ ] **端到端测试**
  - 运行模拟系统
  - 观察 Grafana Dashboard
  - 验证指标准确性

- [ ] **更新文档**
  - 更新 README.md（监控部分）
  - 更新架构图（加入监控组件）
  - 编写监控使用指南

- [ ] **项目总结**
  - 总结监控系统的作用
  - 记录遇到的问题和解决方案
  - 准备最终演示

#### 交付物
- ✅ 监控系统完整运行
- ✅ 文档更新完成
- ✅ 项目可以完整演示

---

## 📊 进度跟踪表

### Week 1
| 日期 | 任务 | 状态 | 备注 |
|------|------|------|------|
| Day 1-2 | 环境搭建 | ⬜ | |
| Day 3-4 | 数据模型 | ⬜ | |
| Day 5-7 | RabbitMQ | ⬜ | |

### Week 2
| 日期 | 任务 | 状态 | 备注 |
|------|------|------|------|
| Day 8-10 | Order Manager | ⬜ | |
| Day 11-13 | Inventory Manager | ⬜ | |
| Day 14 | 集成测试 | ⬜ | |

### Week 3
| 日期 | 任务 | 状态 | 备注 |
|------|------|------|------|
| Day 15-17 | CSV 读取 | ⬜ | |
| Day 18-19 | SimulationClock | ⬜ | |
| Day 20-21 | Order Injector | ⬜ | |

### Week 4
| 日期 | 任务 | 状态 | 备注 |
|------|------|------|------|
| Day 22-24 | REST API | ⬜ | |
| Day 25-26 | 优化测试 | ⬜ | |
| Day 27-28 | 文档总结 | ⬜ | |

### Week 5（进阶）
| 日期 | 任务 | 状态 | 备注 |
|------|------|------|------|
| Day 29-31 | Actuator 集成 | ⬜ | |
| Day 32-33 | 自定义 Metrics | ⬜ | |
| Day 34-35 | Prometheus + Grafana | ⬜ | |
| Day 36-37 | Dashboard 创建 | ⬜ | |
| Day 38 | 测试与文档 | ⬜ | |

### Week 6（扩展功能）
| 日期 | 任务 | 状态 | 备注 |
|------|------|------|------|
| Day 39-40 | 数据库迁移到 PostgreSQL | ⬜ | 提升扩展性 |
| Day 41-42 | 性能优化 | ⬜ | 可选 |
| Day 43-44 | 安全增强 | ⬜ | 可选 |

### Week 7（测试与API文档）
| 日期 | 任务 | 状态 | 备注 |
|------|------|------|------|
| Day 45-46 | 单元测试与集成测试 | ⬜ | 提升代码质量 |
| Day 47-48 | API 文档（Swagger/OpenAPI） | ⬜ | 提升API可用性 |
| Day 49-50 | 缓存优化（Redis） | ⬜ | 可选 |

### Week 8（部署与优化）
| 日期 | 任务 | 状态 | 备注 |
|------|------|------|------|
| Day 51-52 | Docker 容器化应用 | ⬜ | 提升部署能力 |
| Day 53-54 | CI/CD 流程 | ⬜ | 可选 |
| Day 55-56 | 性能测试与优化 | ⬜ | 可选 |

---

## 📅 Week 6: 扩展功能与优化（可选）

### Day 39-40: 数据库迁移到 PostgreSQL

#### 任务清单
- [ ] **添加 PostgreSQL 依赖**
  ```xml
  <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
  </dependency>
  ```

- [ ] **配置 PostgreSQL 连接**
  ```yaml
  spring:
    datasource:
      url: jdbc:postgresql://localhost:5432/inventorydb
      driver-class-name: org.postgresql.Driver
      username: inventory
      password: inventory
    jpa:
      database-platform: org.hibernate.dialect.PostgreSQLDialect
      hibernate:
        ddl-auto: update
  ```

- [ ] **在 docker-compose.yml 中添加 PostgreSQL**
  ```yaml
  postgres:
    image: postgres:15-alpine
    container_name: inventory-postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: inventorydb
      POSTGRES_USER: inventory
      POSTGRES_PASSWORD: inventory
    volumes:
      - postgres_data:/var/lib/postgresql/data
  ```

- [ ] **测试数据持久化**
  - 重启应用，确认数据保留
  - 验证表结构正确创建
  - 测试数据查询性能

#### 学习目标
- 理解生产环境数据库选择
- 掌握数据库迁移方法
- 理解数据持久化的重要性
- 学习 PostgreSQL 基本配置

#### 优势说明
- ✅ **数据持久化**：数据不会因应用重启而丢失
- ✅ **扩展性**：支持更大数据量和并发
- ✅ **生产就绪**：适合部署到生产环境
- ✅ **性能优化**：支持索引、查询优化等高级功能

#### 交付物
- ✅ PostgreSQL 配置完成
- ✅ 数据迁移成功
- ✅ 数据持久化验证通过

---

### Day 41-42: 性能优化（可选）

#### 任务清单
- [ ] **连接池优化**
  - 配置 HikariCP 连接池参数
  - 优化连接数配置

- [ ] **查询优化**
  - 添加数据库索引
  - 优化 JPA 查询

- [ ] **批量处理优化**
  - 实现批量插入/更新
  - 优化 CSV 数据加载

#### 交付物
- ✅ 性能优化完成
- ✅ 性能测试通过

---

### Day 43-44: 安全增强（可选）

#### 任务清单
- [ ] **API 安全**
  - 添加 API 认证
  - 实现权限控制

- [ ] **数据安全**
  - 敏感数据加密
  - SQL 注入防护

#### 交付物
- ✅ 安全功能实现
- ✅ 安全测试通过

---

## 📅 Week 7: 测试与 API 文档

### Day 45-46: 单元测试与集成测试

#### 任务清单
- [ ] **添加测试依赖**
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
  </dependency>
  <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>postgresql</artifactId>
      <scope>test</scope>
  </dependency>
  ```

- [ ] **编写单元测试**
  - OrderManager 测试
  - InventoryManager 测试
  - Repository 测试
  - Service 方法测试

- [ ] **编写集成测试**
  - 订单处理流程测试
  - 库存管理流程测试
  - 消息队列集成测试

- [ ] **测试覆盖率**
  - 目标：核心业务逻辑覆盖率 > 70%
  - 使用 JaCoCo 生成覆盖率报告

#### 学习目标
- 掌握 Spring Boot 测试框架
- 理解单元测试和集成测试的区别
- 学会使用 Mockito 进行 Mock 测试
- 理解测试驱动开发（TDD）

#### 交付物
- ✅ 单元测试编写完成
- ✅ 集成测试编写完成
- ✅ 测试覆盖率报告

---

### Day 47-48: API 文档（Swagger/OpenAPI）

#### 任务清单
- [ ] **添加 Swagger/OpenAPI 依赖**
  ```xml
  <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.2.0</version>
  </dependency>
  ```

- [ ] **配置 Swagger**
  ```yaml
  springdoc:
    api-docs:
      path: /api-docs
    swagger-ui:
      path: /swagger-ui.html
  ```

- [ ] **添加 API 注解**
  - `@Operation` - 描述 API 操作
  - `@ApiResponse` - 描述响应
  - `@Parameter` - 描述参数
  - `@Schema` - 描述数据模型

- [ ] **测试 API 文档**
  - 访问 http://localhost:8080/swagger-ui.html
  - 验证所有 API 端点都有文档
  - 测试 API 文档中的 Try it out 功能

#### 学习目标
- 理解 API 文档的重要性
- 掌握 OpenAPI/Swagger 规范
- 学会编写清晰的 API 文档

#### 交付物
- ✅ Swagger UI 可访问
- ✅ 所有 API 端点都有文档
- ✅ API 文档清晰完整

---

### Day 49-50: 缓存优化（Redis - 可选）

#### 任务清单
- [ ] **添加 Redis 依赖**
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
  </dependency>
  ```

- [ ] **在 docker-compose.yml 中添加 Redis**
  ```yaml
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
  ```

- [ ] **配置 Redis 缓存**
  ```yaml
  spring:
    data:
      redis:
        host: localhost
        port: 6379
    cache:
      type: redis
  ```

- [ ] **实现缓存**
  - 库存查询缓存（`@Cacheable`）
  - 订单查询缓存
  - 缓存更新策略

#### 学习目标
- 理解缓存的作用和优势
- 掌握 Spring Cache 抽象
- 学会 Redis 基本使用

#### 交付物
- ✅ Redis 配置完成
- ✅ 缓存功能实现
- ✅ 性能提升验证

---

## 📅 Week 8: 部署与优化

### Day 51-52: Docker 容器化应用

#### 任务清单
- [ ] **创建 Dockerfile**
  ```dockerfile
  FROM openjdk:17-jdk-slim
  WORKDIR /app
  COPY target/inventory-simulator-*.jar app.jar
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```

- [ ] **创建 .dockerignore**
  ```
  target/
  .git/
  .idea/
  *.iml
  ```

- [ ] **构建 Docker 镜像**
  ```bash
  docker build -t inventory-simulator:latest .
  ```

- [ ] **更新 docker-compose.yml**
  - 添加应用服务
  - 配置服务依赖
  - 配置网络

- [ ] **测试容器化部署**
  ```bash
  docker-compose up -d
  ```

#### 学习目标
- 理解容器化部署的优势
- 掌握 Docker 基本使用
- 学会编写 Dockerfile
- 理解 Docker Compose 多容器编排

#### 交付物
- ✅ Dockerfile 创建完成
- ✅ Docker 镜像构建成功
- ✅ docker-compose.yml 包含所有服务
- ✅ 容器化部署测试通过

---

### Day 53-54: CI/CD 流程（可选）

#### 任务清单
- [ ] **配置 GitHub Actions（或其他 CI/CD）**
  ```yaml
  name: CI/CD Pipeline
  on:
    push:
      branches: [ main ]
  jobs:
    build:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v3
        - name: Set up JDK 17
          uses: actions/setup-java@v3
          with:
            java-version: '17'
        - name: Build with Maven
          run: mvn clean package
        - name: Run tests
          run: mvn test
  ```

- [ ] **自动化测试**
  - 代码提交时自动运行测试
  - 测试失败时阻止合并

- [ ] **自动化构建**
  - 构建 Docker 镜像
  - 推送到镜像仓库（可选）

#### 学习目标
- 理解 CI/CD 的概念和作用
- 掌握 GitHub Actions 基本使用
- 学会自动化构建和测试流程

#### 交付物
- ✅ CI/CD 流程配置完成
- ✅ 自动化测试运行正常
- ✅ 自动化构建成功

---

### Day 55-56: 性能测试与优化

#### 任务清单
- [ ] **性能测试**
  - 使用 JMeter 或 Gatling 进行压力测试
  - 测试订单处理吞吐量
  - 测试并发处理能力

- [ ] **性能分析**
  - 识别性能瓶颈
  - 分析数据库查询性能
  - 分析消息队列处理性能

- [ ] **性能优化**
  - 数据库索引优化
  - 查询优化
  - 连接池优化
  - 批量处理优化

- [ ] **性能报告**
  - 记录优化前后的性能对比
  - 总结优化经验

#### 学习目标
- 理解性能测试的重要性
- 掌握性能测试工具使用
- 学会性能分析和优化方法

#### 交付物
- ✅ 性能测试完成
- ✅ 性能优化实施
- ✅ 性能报告文档

---

## 💡 每日检查清单

每天结束时，检查：
- [ ] 今天的任务完成了吗？
- [ ] 代码可以正常运行吗？
- [ ] 遇到什么问题？解决了吗？
- [ ] 明天要做什么？

---

## 🎯 里程碑

- **Week 1 结束**：基础架构搭建完成
- **Week 2 结束**：核心功能实现完成
- **Week 3 结束**：完整功能实现完成
- **Week 4 结束**：项目完成，可以演示
- **Week 5 结束**（进阶）：监控系统完成，项目全面完善
- **Week 6 结束**（扩展）：数据库迁移完成，系统扩展性提升
- **Week 7 结束**（扩展）：测试与 API 文档完成，代码质量提升
- **Week 8 结束**（扩展）：部署与优化完成，项目生产就绪

---

## 🔄 扩展功能说明

### 为什么需要扩展功能？

项目初始使用 **H2 内存数据库**，适合快速开发和测试：
- ✅ 无需安装配置
- ✅ 启动快速
- ✅ 适合学习和原型开发

但 H2 的限制：
- ❌ 数据不持久化（应用重启数据丢失）
- ❌ 不适合生产环境
- ❌ 扩展性有限

### Week 6: 迁移到 PostgreSQL

**目标**：将系统从 H2 迁移到 PostgreSQL，提升系统扩展性和生产就绪性。

**学习价值**：
- 理解不同数据库的适用场景
- 掌握数据库迁移方法
- 学习生产环境数据库配置
- 理解数据持久化的重要性

**实施步骤**：
1. 添加 PostgreSQL 依赖
2. 配置数据库连接
3. 在 docker-compose.yml 中添加 PostgreSQL 服务
4. 更新 application.yml 配置
5. 测试数据持久化

**预期收益**：
- ✅ 数据持久化，应用重启不丢失
- ✅ 支持更大数据量和并发
- ✅ 适合部署到生产环境
- ✅ 学习生产级数据库使用

---

**记住**：计划是指导，可以根据实际情况调整。重要的是持续进步！
