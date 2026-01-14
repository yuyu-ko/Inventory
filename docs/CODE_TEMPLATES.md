# 代码模板参考

> **注意**：这些只是**模板和提示**，帮助你理解需要实现什么。**不要直接复制**，你需要理解后自己实现。

## 📋 实体类模板

### Order.java 字段提示

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String orderId;  // ORD-000001
    
    @Enumerated(EnumType.STRING)
    private OrderType orderType;  // PICKUP 或 DELIVERY
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;  // RECEIVED, PROCESSING, COMPLETED, CANCELLED
    
    private LocalDateTime orderPlacedTime;
    private LocalDateTime orderDueTime;
    private String customerId;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
    
    // TODO: 添加 getter/setter 或使用 Lombok
}
```

### OrderItem.java 字段提示

```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String sku;
    private Integer quantity;
    private String temperatureZone;  // AMBIENT, CHILLED, FROZEN
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    // TODO: 添加 getter/setter
}
```

### InventoryItem.java 字段提示

```java
@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String sku;
    
    private String name;
    private Integer quantity;  // 总库存
    private Integer reservedQuantity;  // 预留数量
    private String temperatureZone;
    private Integer lowStockThreshold;
    
    // TODO: 添加方法计算可用库存
    // public Integer getAvailableQuantity() {
    //     return quantity - reservedQuantity;
    // }
}
```

---

## 🔧 Service 类模板

### OrderManager.java 结构提示

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderManager {
    
    private final OrderRepository orderRepository;
    private final InventoryManager inventoryManager;
    private final RabbitTemplate rabbitTemplate;
    private final SimulationClock simulationClock;
    
    // TODO: 实现消息监听
    @RabbitListener(queues = "${spring.rabbitmq.topic.prefix:sim}.order.received")
    public void handleOrderReceived(OrderReceivedMessage message) {
        // 1. 创建订单实体
        // 2. 保存到数据库
        // 3. 检查库存
        // 4. 发送库存预留消息或更新订单状态为失败
    }
    
    // TODO: 实现订单处理方法
    private void processOrder(Order order) {
        // 1. 更新订单状态为 PROCESSING
        // 2. 发送库存扣除消息
        // 3. 更新订单状态为 COMPLETED
        // 4. 发送订单处理完成消息
    }
    
    // TODO: 实现库存检查方法
    private boolean checkAndReserveInventory(Order order, List<OrderItemDTO> items) {
        // 检查每个商品的库存是否充足
        // 如果充足，发送预留消息
        // 返回 true/false
    }
}
```

### InventoryManager.java 结构提示

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryManager {
    
    private final InventoryItemRepository inventoryRepository;
    
    @RabbitListener(queues = "${spring.rabbitmq.topic.prefix:sim}.inventory.update")
    @Transactional
    public void handleInventoryUpdate(InventoryUpdateMessage message) {
        // TODO: 根据 operation 执行不同操作
        switch (message.getOperation()) {
            case "RESERVE":
                // 预留库存
                break;
            case "DEDUCT":
                // 扣除库存
                break;
            case "REPLENISH":
                // 补货
                break;
            case "RELEASE":
                // 释放预留
                break;
        }
        
        // 检查是否需要自动补货
        checkAndReplenish(item);
    }
    
    // TODO: 实现预留库存
    private void reserveInventory(InventoryItem item, Integer quantity) {
        // 检查可用库存
        // 如果充足，增加预留数量
    }
    
    // TODO: 实现扣除库存
    private void deductInventory(InventoryItem item, Integer quantity) {
        // 先扣除预留的，不够再从总库存扣除
    }
    
    // TODO: 实现自动补货检查
    private void checkAndReplenish(InventoryItem item) {
        // 如果库存 <= 阈值，自动补货
    }
}
```

### SimulationClock.java 结构提示

```java
@Component
@Getter
@Slf4j
public class SimulationClock {
    
    @Value("${inventory.simulation.sim-start-time:2024-01-13T08:00:00}")
    private String simStartTimeStr;
    
    @Value("${inventory.simulation.sim-end-time:2024-01-13T18:00:00}")
    private String simEndTimeStr;
    
    @Value("${inventory.simulation.tick-seconds:1}")
    private int tickSeconds;
    
    @Value("${inventory.simulation.speed-factor:1.0}")
    private double speedFactor;
    
    private LocalDateTime currentSimTime;
    private LocalDateTime simStartTime;
    private LocalDateTime simEndTime;
    private boolean isRunning;
    
    @PostConstruct
    public void initialize() {
        // TODO: 解析时间字符串
        // TODO: 初始化 currentSimTime = simStartTime
        // TODO: 设置 isRunning = true
    }
    
    // TODO: 实现 tick 方法（由 SimulationRunner 调用）
    public void tick() {
        // 计算时间增量（考虑 speedFactor）
        // 更新 currentSimTime
        // 检查是否到达结束时间
    }
    
    // TODO: 实现其他辅助方法
    public boolean isRunning() { ... }
    public LocalDateTime getCurrentTime() { ... }
    public String formatTime(LocalDateTime time) { ... }
}
```

### OrderInjector.java 结构提示

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderInjector {
    
    private final RabbitTemplate rabbitTemplate;
    private final OrderCSVReader csvReader;
    private final SimulationClock simulationClock;
    
    private Queue<OrderReceivedMessage> orderQueue = new PriorityQueue<>(...);
    
    @PostConstruct
    public void initialize() {
        // TODO: 从 CSV 读取订单
        // TODO: 转换为 OrderReceivedMessage
        // TODO: 按时间排序
        // TODO: 过滤出模拟时间范围内的订单
        // TODO: 添加到 orderQueue
    }
    
    @Scheduled(fixedDelayString = "${inventory.order-injector.injection-interval-seconds:5}000")
    public void injectOrders() {
        // TODO: 检查模拟时钟是否运行
        // TODO: 遍历订单队列
        // TODO: 如果订单时间 <= 当前模拟时间，发送订单
        // TODO: 从队列中移除已发送的订单
    }
    
    private void publishOrder(OrderReceivedMessage order) {
        // TODO: 使用 rabbitTemplate 发送消息
        // TODO: 记录日志
    }
}
```

---

## 📨 消息发送示例

### 发送订单消息

```java
String exchange = "symbotic.simulation";
String routingKey = "sim.order.received";
OrderReceivedMessage message = new OrderReceivedMessage();
// ... 设置消息字段

rabbitTemplate.convertAndSend(exchange, routingKey, message);
```

### 发送库存更新消息

```java
InventoryUpdateMessage updateMessage = new InventoryUpdateMessage();
updateMessage.setSku("SKU-001");
updateMessage.setOperation("RESERVE");
updateMessage.setReservedQuantityChange(5);
updateMessage.setOrderId("ORD-000001");

rabbitTemplate.convertAndSend(exchange, "sim.inventory.update", updateMessage);
```

---

## 🔍 Repository 示例

```java
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    List<Order> findByStatus(OrderStatus status);
}

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findBySku(String sku);
}
```

---

## 📝 日志示例

```java
// 信息日志
log.info("[{}] ord-{} received", 
    simulationClock.formatTime(simulationClock.getCurrentTime()), 
    orderId);

log.info("[{}] ord-{} completed successfully", 
    simulationClock.formatTime(simulationClock.getCurrentTime()), 
    orderId);

// 警告日志
log.warn("[{}] ord-{} failed - insufficient inventory", 
    simulationClock.formatTime(simulationClock.getCurrentTime()), 
    orderId);

// 调试日志
log.debug("Processing order: {}, items: {}", orderId, items);
```

---

## ⚠️ 重要提示

1. **这些只是模板**：不要直接复制，理解后自己实现
2. **字段名可能不同**：根据你的设计调整
3. **方法名可能不同**：使用你习惯的命名
4. **逻辑需要自己思考**：这里只提供结构提示
5. **遇到问题查文档**：官方文档是最好的参考

---

## 🎯 下一步

1. 理解这些模板的结构
2. 参考 `IMPLEMENTATION_GUIDE.md` 的详细说明
3. 开始实现你自己的代码
4. 遇到问题及时查看文档或询问

**记住：自己实现的代码才是你真正掌握的！**
