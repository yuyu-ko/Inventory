# Implementation Guide

## 🎯 Project Goals

You need to implement an **Inventory Management Simulator** yourself. This document provides guidance, but **you need to write the code yourself**.

## 📋 Components to Implement

### Week 1: Foundation

#### 1. Data Model (Must Implement Yourself)

You need to create the following entity classes:

**Order.java**
- Order ID, order type, status, order placed time, order due time
- Customer ID, order item list (OneToMany)
- Use JPA annotations: `@Entity`, `@Table`, `@Id`, `@OneToMany`

**OrderItem.java**
- SKU, quantity, temperature zone
- Many-to-one relationship with Order
- Use `@ManyToOne`, `@JoinColumn`

**InventoryItem.java**
- SKU (unique), name, total quantity, reserved quantity
- Temperature zone, low stock threshold
- Method to calculate available inventory: `availableQuantity = quantity - reservedQuantity`

**Hints**:
- Reference JPA documentation: https://spring.io/projects/spring-data-jpa
- Entity relationships: Order 1:N OrderItem
- InventoryItem is independent table

---

### Week 2: Core Services

#### 2. OrderManager (Order Processor)

**Methods you need to implement:**

```java
@RabbitListener(queues = "sim.order.received")
public void handleOrderReceived(OrderReceivedMessage message) {
    // TODO: 
    // 1. Create order entity
    // 2. Save to database
    // 3. Check if inventory is sufficient
    // 4. If sufficient, send inventory reservation message
    // 5. If insufficient, update order status to CANCELLED
}
```

**Key Logic:**
- Iterate through all items in the order
- Check available inventory for each SKU
- If all items have sufficient inventory, send `RESERVE` message
- After successful inventory reservation, send `DEDUCT` message to deduct inventory
- Update order status: RECEIVED → PROCESSING → COMPLETED

**Hints**:
- Use `RabbitTemplate.convertAndSend()` to send messages
- Use enum types for order status
- Log: `log.info("[time] ord-XXXXX completed successfully")`

---

#### 3. InventoryManager (Inventory Manager)

**Methods you need to implement:**

```java
@RabbitListener(queues = "sim.inventory.update")
public void handleInventoryUpdate(InventoryUpdateMessage message) {
    // TODO:
    // Execute different operations based on operation type:
    // - RESERVE: Reserve inventory
    // - DEDUCT: Deduct inventory
    // - REPLENISH: Replenish inventory
    // - RELEASE: Release reserved inventory
}
```

**Key Logic:**

**Reserve Inventory (RESERVE)**:
```java
if (availableQuantity >= requestedQuantity) {
    reservedQuantity += requestedQuantity;
    // Save
}
```

**Deduct Inventory (DEDUCT)**:
```java
// First deduct from reserved, then from total inventory if not enough
int deductFromReserved = Math.min(quantity, reservedQuantity);
int deductFromStock = quantity - deductFromReserved;

reservedQuantity -= deductFromReserved;
quantity -= deductFromStock;
```

**Auto-Replenishment**:
```java
if (quantity <= lowStockThreshold) {
    quantity += replenishmentQuantity;
}
```

**Hints**:
- Use `@Transactional` to ensure data consistency
- Check inventory availability: `availableQuantity = quantity - reservedQuantity`
- Handle concurrency (even though this is a simulation, consider logical correctness)

---

### Week 3: Order Injection and Clock

#### 4. CSV Reader

**OrderCSVReader.java**
- Read CSV file
- Parse using OpenCSV library
- Return `List<OrderCSVRecord>`

**Hints**:
- Use `CsvToBeanBuilder` to parse CSV
- CSV file is in `resources/data/` directory
- Handle file reading exceptions

---

#### 5. SimulationClock (Simulation Clock)

**Fields and methods you need to implement:**

```java
private LocalDateTime currentSimTime;
private LocalDateTime simStartTime;
private LocalDateTime simEndTime;
private boolean isRunning;

public void tick() {
    // TODO: 
    // 1. Calculate time increment based on tickSeconds and speedFactor
    // 2. Update currentSimTime
    // 3. Check if end time is reached
}
```

**Key Logic:**
- `tick()` method increments time each time (consider speedFactor)
- `isRunning()` checks if it's running
- `getCurrentTime()` returns current simulation time

**Hints**:
- Use `LocalDateTime.plusSeconds()` to increment time
- Initialize time in `@PostConstruct`
- Use `@Scheduled` to call tick periodically

---

#### 6. OrderInjector (Order Injector)

**Logic you need to implement:**

```java
@PostConstruct
public void initialize() {
    // TODO: Read all orders from CSV
    // TODO: Sort by order time
    // TODO: Filter orders within simulation time range
}

@Scheduled(fixedDelay = 5000)
public void injectOrders() {
    // TODO: 
    // 1. Check if simulation clock is running
    // 2. Iterate through order queue
    // 3. If order time <= current simulation time, send order
    // 4. Remove sent orders from queue
}
```

**Key Logic:**
- Load CSV orders on startup
- Decide whether to send based on `ORDER_PLACED_TIME` and current simulation time
- Use queue to store pending orders
- Avoid sending duplicate orders

**Hints**:
- Use `PriorityQueue` to sort by time
- Or use `List` and then sort
- Log: Order sent successfully

---

### Week 4: API and Refinement

#### 7. REST API Controllers

**OrderController.java**
```java
@GetMapping("/api/orders")
public List<Order> getAllOrders() {
    // TODO: Query all orders
}

@GetMapping("/api/orders/{orderId}")
public Order getOrder(@PathVariable String orderId) {
    // TODO: Query specific order
}
```

**InventoryController.java**
```java
@GetMapping("/api/inventory/{sku}")
public InventoryItem getInventory(@PathVariable String sku) {
    // TODO: Query inventory
}
```

**Hints**:
- Use `ResponseEntity` to return data
- Handle case when order doesn't exist (404)
- Use `@RestController` and `@RequestMapping`

---

## 🛠️ Implementation Step Suggestions

### Step 1: Understand Requirements
1. Carefully read `PROJECT_GUIDE.md`
2. Understand system architecture (check `ARCHITECTURE_DIAGRAM.md`)
3. Understand message flow

### Step 2: Build Skeleton
1. Create entity classes (fields and method signatures)
2. Create Repository interfaces
3. Create Service classes (empty methods)

### Step 3: Implement One by One
1. **Start with the simplest** (like entity classes, Repository)
2. **Then implement message listening** (first be able to receive messages)
3. **Finally implement business logic** (gradually improve)

### Step 4: Test and Verify
1. Test after implementing each feature
2. Use Postman to test APIs
3. Check RabbitMQ management interface
4. Check log output

---

## 💡 Implementation Tips

### 1. Get It Running First, Then Refine
- First make code compile
- Then gradually implement features
- Finally optimize code

### 2. Write More Logs
```java
log.info("Processing order: {}", orderId);
log.debug("Current inventory: {}", inventory);
log.warn("Insufficient stock for SKU: {}", sku);
```

### 3. Handle Exceptions
```java
try {
    // Your code
} catch (Exception e) {
    log.error("Error occurred: {}", e.getMessage(), e);
    // Handle exception
}
```

### 4. Reference Documentation
- Spring Boot documentation
- RabbitMQ documentation
- Spring Data JPA documentation
- OpenCSV documentation

---

## ❓ Frequently Asked Questions

### Q: I don't know where to start?
**A**: Follow the daily plan in `WEEKLY_PLAN.md`, complete one task per day.

### Q: How to design entity relationships?
**A**: 
- Order 1:N OrderItem (one-to-many)
- InventoryItem is independent (no relationship needed)
- Use `@OneToMany` and `@ManyToOne`

### Q: How to send and receive messages?
**A**:
- Send: Use `RabbitTemplate.convertAndSend(exchange, routingKey, message)`
- Receive: Use `@RabbitListener(queues = "queue.name")`

### Q: How to implement simulation clock?
**A**:
- Use `LocalDateTime` to store time
- Use `@Scheduled` to call `tick()` periodically
- Each tick increments time by a certain amount (consider speedFactor)

### Q: How to read CSV?
**A**:
- Use OpenCSV library's `CsvToBeanBuilder`
- File is in `resources/data/` directory
- Use `ClassPathResource` to read

---

## ✅ Completion Checklist

### Week 1 Checkpoint
- [ ] Entity classes created
- [ ] Repository interfaces created
- [ ] Database tables can be created automatically
- [ ] RabbitMQ can send and receive messages

### Week 2 Checkpoint
- [ ] OrderManager can receive order messages
- [ ] Orders can be saved to database
- [ ] InventoryManager can handle inventory operations
- [ ] Order status can be updated correctly

### Week 3 Checkpoint
- [ ] CSV orders can be read correctly
- [ ] Simulation clock works correctly
- [ ] Orders can be sent based on time
- [ ] Order injection functionality works

### Week 4 Checkpoint
- [ ] All API endpoints implemented
- [ ] Log output is clear
- [ ] System can run completely
- [ ] Documentation completed

---

## 📚 Reference Resources

### Official Documentation
- Spring Boot: https://spring.io/projects/spring-boot
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- RabbitMQ: https://www.rabbitmq.com/documentation.html
- OpenCSV: http://opencsv.sourceforge.net/

### Code Example Websites
- Baeldung: https://www.baeldung.com/
- Spring Guides: https://spring.io/guides

### Video Tutorials
- Spring Boot practical tutorials (Bilibili/YouTube)
- RabbitMQ getting started tutorials

---

## 🎯 Learning Recommendations

1. **Don't copy-paste code**: Understand first, then write yourself
2. **Think first when encountering problems**: Check documentation, logs, error messages
3. **Practice more**: Write code, test, debug
4. **Record problems**: Record problems encountered and solutions
5. **Communicate with others**: Discuss with team members, ask mentors questions

---

## 🚀 Start Implementation

Now you have direction, start implementing!

Remember:
- **You need to write the code yourself**
- **Encountering problems is normal, solve them**
- **Make progress little by little each day**

**Good luck! 💪**
