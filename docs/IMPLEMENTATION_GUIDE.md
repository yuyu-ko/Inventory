# 实现指南 - 学生版

## 🎯 项目目标

你需要自己实现一个**库存管理模拟器**。本文档会给你方向，但**代码需要你自己写**。

## 📋 需要实现的组件

### 第一周：基础架构

#### 1. 数据模型（必须自己实现）

你需要创建以下实体类：

**Order.java**
- 订单ID、订单类型、状态、下单时间、到期时间
- 客户ID、订单项列表（OneToMany）
- 使用 JPA 注解：`@Entity`, `@Table`, `@Id`, `@OneToMany`

**OrderItem.java**
- SKU、数量、温度区域
- 与 Order 的多对一关系
- 使用 `@ManyToOne`, `@JoinColumn`

**InventoryItem.java**
- SKU（唯一）、名称、总数量、预留数量
- 温度区域、低库存阈值
- 计算可用库存的方法：`availableQuantity = quantity - reservedQuantity`

**提示**：
- 参考 JPA 文档：https://spring.io/projects/spring-data-jpa
- 实体关系：Order 1:N OrderItem
- InventoryItem 独立表

---

### 第二周：核心服务

#### 2. OrderManager（订单处理器）

**你需要实现的方法：**

```java
@RabbitListener(queues = "sim.order.received")
public void handleOrderReceived(OrderReceivedMessage message) {
    // TODO: 
    // 1. 创建订单实体
    // 2. 保存到数据库
    // 3. 检查库存是否充足
    // 4. 如果充足，发送库存预留消息
    // 5. 如果不足，更新订单状态为 CANCELLED
}
```

**关键逻辑：**
- 遍历订单中的所有商品项
- 对每个 SKU 检查可用库存
- 如果所有商品都有足够库存，发送 `RESERVE` 消息
- 库存预留成功后，发送 `DEDUCT` 消息扣除库存
- 更新订单状态：RECEIVED → PROCESSING → COMPLETED

**提示**：
- 使用 `RabbitTemplate.convertAndSend()` 发送消息
- 订单状态使用枚举类型
- 记录日志：`log.info("[时间] ord-XXXXX completed successfully")`

---

#### 3. InventoryManager（库存管理器）

**你需要实现的方法：**

```java
@RabbitListener(queues = "sim.inventory.update")
public void handleInventoryUpdate(InventoryUpdateMessage message) {
    // TODO:
    // 根据 operation 类型执行不同操作：
    // - RESERVE: 预留库存
    // - DEDUCT: 扣除库存
    // - REPLENISH: 补货
    // - RELEASE: 释放预留库存
}
```

**关键逻辑：**

**预留库存 (RESERVE)**：
```java
if (availableQuantity >= requestedQuantity) {
    reservedQuantity += requestedQuantity;
    // 保存
}
```

**扣除库存 (DEDUCT)**：
```java
// 先扣除预留的，不够再从总库存扣除
int deductFromReserved = Math.min(quantity, reservedQuantity);
int deductFromStock = quantity - deductFromReserved;

reservedQuantity -= deductFromReserved;
quantity -= deductFromStock;
```

**自动补货**：
```java
if (quantity <= lowStockThreshold) {
    quantity += replenishmentQuantity;
}
```

**提示**：
- 使用 `@Transactional` 确保数据一致性
- 检查库存可用性：`availableQuantity = quantity - reservedQuantity`
- 处理并发情况（虽然这是模拟，但要考虑逻辑正确性）

---

### 第三周：订单注入与时钟

#### 4. CSV 读取器

**OrderCSVReader.java**
- 读取 CSV 文件
- 使用 OpenCSV 库解析
- 返回 `List<OrderCSVRecord>`

**提示**：
- 使用 `CsvToBeanBuilder` 解析 CSV
- CSV 文件在 `resources/data/` 目录
- 处理文件读取异常

---

#### 5. SimulationClock（模拟时钟）

**你需要实现的字段和方法：**

```java
private LocalDateTime currentSimTime;
private LocalDateTime simStartTime;
private LocalDateTime simEndTime;
private boolean isRunning;

public void tick() {
    // TODO: 
    // 1. 根据 tickSeconds 和 speedFactor 计算时间增量
    // 2. 更新 currentSimTime
    // 3. 检查是否到达结束时间
}
```

**关键逻辑：**
- `tick()` 方法每次增加时间（考虑 speedFactor）
- `isRunning()` 检查是否在运行中
- `getCurrentTime()` 返回当前模拟时间

**提示**：
- 使用 `LocalDateTime.plusSeconds()` 增加时间
- 在 `@PostConstruct` 中初始化时间
- 使用 `@Scheduled` 定时调用 tick

---

#### 6. OrderInjector（订单注入器）

**你需要实现的逻辑：**

```java
@PostConstruct
public void initialize() {
    // TODO: 从 CSV 读取所有订单
    // TODO: 按订单时间排序
    // TODO: 过滤出模拟时间范围内的订单
}

@Scheduled(fixedDelay = 5000)
public void injectOrders() {
    // TODO: 
    // 1. 检查模拟时钟是否运行
    // 2. 遍历订单队列
    // 3. 如果订单时间 <= 当前模拟时间，发送订单
    // 4. 从队列中移除已发送的订单
}
```

**关键逻辑：**
- 启动时加载 CSV 订单
- 根据 `ORDER_PLACED_TIME` 和当前模拟时间决定是否发送
- 使用队列存储待发送订单
- 避免重复发送订单

**提示**：
- 使用 `PriorityQueue` 按时间排序
- 或者使用 `List` 然后排序
- 记录日志：订单发送成功

---

### 第四周：API 与完善

#### 7. REST API 控制器

**OrderController.java**
```java
@GetMapping("/api/orders")
public List<Order> getAllOrders() {
    // TODO: 查询所有订单
}

@GetMapping("/api/orders/{orderId}")
public Order getOrder(@PathVariable String orderId) {
    // TODO: 查询指定订单
}
```

**InventoryController.java**
```java
@GetMapping("/api/inventory/{sku}")
public InventoryItem getInventory(@PathVariable String sku) {
    // TODO: 查询库存
}
```

**提示**：
- 使用 `ResponseEntity` 返回数据
- 处理订单不存在的情况（404）
- 使用 `@RestController` 和 `@RequestMapping`

---

## 🛠️ 实现步骤建议

### 步骤 1：理解需求
1. 仔细阅读 `STUDENT_GUIDE.md`
2. 理解系统架构（查看 `ARCHITECTURE_DIAGRAM.md`）
3. 理解消息流程

### 步骤 2：搭建骨架
1. 创建实体类（字段和方法签名）
2. 创建 Repository 接口
3. 创建 Service 类（空方法）

### 步骤 3：逐个实现
1. **先实现最简单**的（如实体类、Repository）
2. **再实现消息监听**（先能收到消息）
3. **最后实现业务逻辑**（逐步完善）

### 步骤 4：测试验证
1. 每实现一个功能就测试
2. 使用 Postman 测试 API
3. 查看 RabbitMQ 管理界面
4. 查看日志输出

---

## 💡 实现技巧

### 1. 先跑起来，再完善
- 先让代码能编译通过
- 再逐步实现功能
- 最后优化代码

### 2. 多写日志
```java
log.info("Processing order: {}", orderId);
log.debug("Current inventory: {}", inventory);
log.warn("Insufficient stock for SKU: {}", sku);
```

### 3. 处理异常
```java
try {
    // 你的代码
} catch (Exception e) {
    log.error("Error occurred: {}", e.getMessage(), e);
    // 处理异常
}
```

### 4. 参考文档
- Spring Boot 文档
- RabbitMQ 文档
- Spring Data JPA 文档
- OpenCSV 文档

---

## ❓ 常见问题

### Q: 我不知道从哪里开始？
**A**: 按照 `WEEKLY_PLAN.md` 的每日计划，一天完成一个任务。

### Q: 实体类的关系怎么设计？
**A**: 
- Order 1:N OrderItem（一对多）
- InventoryItem 是独立的（不需要关系）
- 使用 `@OneToMany` 和 `@ManyToOne`

### Q: 消息怎么发送和接收？
**A**:
- 发送：使用 `RabbitTemplate.convertAndSend(exchange, routingKey, message)`
- 接收：使用 `@RabbitListener(queues = "queue.name")`

### Q: 模拟时钟怎么实现？
**A**:
- 使用 `LocalDateTime` 存储时间
- 使用 `@Scheduled` 定时调用 `tick()`
- 每次 tick 增加一定时间（考虑 speedFactor）

### Q: CSV 怎么读取？
**A**:
- 使用 OpenCSV 库的 `CsvToBeanBuilder`
- 文件放在 `resources/data/` 目录
- 使用 `ClassPathResource` 读取

---

## ✅ 完成检查清单

### Week 1 检查点
- [ ] 实体类创建完成
- [ ] Repository 接口创建完成
- [ ] 数据库表能够自动创建
- [ ] RabbitMQ 能够发送和接收消息

### Week 2 检查点
- [ ] OrderManager 能够接收订单消息
- [ ] 订单能够保存到数据库
- [ ] InventoryManager 能够处理库存操作
- [ ] 订单状态能够正确更新

### Week 3 检查点
- [ ] CSV 订单能够正确读取
- [ ] 模拟时钟能够正常工作
- [ ] 订单能够根据时间发送
- [ ] 订单注入功能正常

### Week 4 检查点
- [ ] 所有 API 端点实现完成
- [ ] 日志输出清晰
- [ ] 系统能够完整运行
- [ ] 文档完成

---

## 📚 参考资源

### 官方文档
- Spring Boot: https://spring.io/projects/spring-boot
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- RabbitMQ: https://www.rabbitmq.com/documentation.html
- OpenCSV: http://opencsv.sourceforge.net/

### 代码示例网站
- Baeldung: https://www.baeldung.com/
- Spring Guides: https://spring.io/guides

### 视频教程
- Spring Boot 实战教程（B站/YouTube）
- RabbitMQ 入门教程

---

## 🎓 学习建议

1. **不要复制粘贴代码**：理解后再自己写
2. **遇到问题先思考**：查看文档、日志、错误信息
3. **多实践**：写代码、测试、调试
4. **记录问题**：遇到的问题和解决方案记录下来
5. **与他人交流**：与同学讨论，向老师提问

---

## 🚀 开始实现

现在你有了方向，开始动手实现吧！

记住：
- **代码需要你自己写**
- **遇到问题很正常，解决它**
- **每天进步一点点**

**加油！💪**
