# Detailed 8-Week Implementation Plan

## 📅 Week 1: Environment Setup and Foundation

### Day 1-2: Project Preparation

#### Task Checklist
- [ ] **Environment Setup**
  - Install Java 17 JDK
  - Install Maven (or use IDE built-in)
  - Install IDE (recommended: IntelliJ IDEA)
  - Install Docker Desktop
  - Verify installation: `java -version`, `mvn -version`, `docker --version`

- [ ] **Project Initialization**
  - Create project using Spring Initializr (or start from template)
  - Configure `pom.xml` to add dependencies
  - Create basic package structure
  - Run application to confirm environment is working

- [ ] **Understand Project**
  - Read README.md
  - Read SYSTEM_DESIGN.md
  - Understand system architecture diagrams
  - Clarify project goals

#### Learning Objectives
- Master Spring Boot project creation
- Understand Maven dependency management
- Familiarize with development environment

#### Deliverables
- ✅ Runnable Spring Boot project
- ✅ Complete configuration files
- ✅ Understand project requirements

---

### Day 3-4: Data Model Design

#### Task Checklist
- [ ] **Design Order Entity**
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

- [ ] **Design OrderItem Entity**
- [ ] **Design InventoryItem Entity**
- [ ] **Create Repository Interfaces**
- [ ] **Configure Database Connection**
- [ ] **Test Database Operations**

#### Learning Objectives
- Master JPA entity design
- Understand entity relationship mapping
- Master Repository usage

#### Deliverables
- ✅ Complete entity class definitions
- ✅ Repository interfaces
- ✅ Database tables can be created automatically
- ✅ Can perform basic CRUD operations

---

### Day 5-7: RabbitMQ Integration

#### Task Checklist
- [ ] **Install RabbitMQ**
  ```bash
  docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
  ```

- [ ] **Configure RabbitMQ Connection**
  ```yaml
  spring:
    rabbitmq:
      host: localhost
      port: 5672
      username: guest
      password: guest
  ```

- [ ] **Create Configuration Class**
  - Exchange (Topic Exchange)
  - Queue (3 queues)
  - Binding (binding relationships)

- [ ] **Implement Simple Message Send/Receive**
  - Create a test Controller
  - Send messages
  - Receive messages
  - Verify message delivery

#### Learning Objectives
- Understand message queue concepts
- Master RabbitMQ basic usage
- Understand Exchange, Queue, Binding

#### Deliverables
- ✅ RabbitMQ running normally
- ✅ Can send and receive messages
- ✅ Understand message queue working principles

---

## 📅 Week 2: Core Functionality Implementation

### Day 8-10: Order Manager Implementation

#### Task Checklist
- [ ] **Create Message Classes**
  - `OrderReceivedMessage`
  - `OrderProcessedMessage`
  - `InventoryUpdateMessage`

- [ ] **Implement OrderManager Service**
  - `handleOrderReceived()` - Receive orders
  - `createOrderFromMessage()` - Create order entity
  - `checkAndReserveInventory()` - Check inventory
  - `processOrder()` - Process orders

- [ ] **Implement Order Status Management**
  - PENDING → RECEIVED → PROCESSING → COMPLETED
  - Failure case: CANCELLED

- [ ] **Test Functionality**
  - Manually send test messages
  - Verify order creation
  - Verify status updates

#### Key Code Hints
```java
@RabbitListener(queues = "sim.order.received")
public void handleOrderReceived(OrderReceivedMessage message) {
    // 1. Create order
    Order order = createOrderFromMessage(message);
    orderRepository.save(order);
    
    // 2. Check inventory (TODO: implement in next phase)
    // 3. Process order
}
```

#### Deliverables
- ✅ OrderManager basic functionality implemented
- ✅ Can receive and process order messages
- ✅ Order data correctly saved

---

### Day 11-13: Inventory Manager Implementation

#### Task Checklist
- [ ] **Implement InventoryManager Service**
  - `handleInventoryUpdate()` - Handle inventory update messages
  - `getOrCreateInventoryItem()` - Get or create inventory item
  - `reserveInventory()` - Reserve inventory
  - `deductInventory()` - Deduct inventory
  - `replenishInventory()` - Replenish inventory
  - `checkAndReplenish()` - Auto-replenishment check

- [ ] **Implement Inventory Operations**
  - RESERVE: Reserve inventory (when order confirmed)
  - DEDUCT: Deduct inventory (when order completed)
  - REPLENISH: Replenish (when inventory insufficient)

- [ ] **Integration Testing**
  - Test reservation functionality
  - Test deduction functionality
  - Test auto-replenishment

#### Key Logic
```java
// Reserve inventory
if (availableQuantity >= requestedQuantity) {
    reservedQuantity += requestedQuantity;
    // Save
}

// Deduct inventory
quantity -= quantityToDeduct;
reservedQuantity -= quantityFromReserved;

// Auto-replenishment
if (quantity <= lowStockThreshold) {
    quantity += replenishmentQuantity;
}
```

#### Deliverables
- ✅ InventoryManager complete implementation
- ✅ Inventory operations work correctly
- ✅ Auto-replenishment works correctly

---

### Day 14: Integration Testing

#### Task Checklist
- [ ] **Complete Flow Testing**
  1. Order Injector sends order (temporarily manual test)
  2. Order Manager receives order
  3. Inventory reservation
  4. Order processing
  5. Inventory deduction
  6. Order completion

- [ ] **Bug Fixes**
  - Fix discovered bugs
  - Optimize code logic
  - Add necessary logs

#### Deliverables
- ✅ Core functionality runs completely
- ✅ Integration tests pass
- ✅ Code quality is good

---

## 📅 Week 3: Order Injection and Simulation Clock

### Day 15-17: CSV Order Reading

#### Task Checklist
- [ ] **Add OpenCSV Dependency**
  ```xml
  <dependency>
      <groupId>com.opencsv</groupId>
      <artifactId>opencsv</artifactId>
      <version>5.9</version>
  </dependency>
  ```

- [ ] **Create OrderCSVRecord Class**
  ```java
  @Data
  public class OrderCSVRecord {
      @CsvBindByName(column = "ORDER_ID")
      private String orderId;
      @CsvBindByName(column = "SKU")
      private String sku;
      // ... other fields
  }
  ```

- [ ] **Implement OrderCSVReader**
  - Read CSV file
  - Parse into OrderCSVRecord list
  - Handle exception cases

- [ ] **Prepare Test CSV File**
  - Create sample order data
  - Verify CSV format is correct

#### CSV Format Example
```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-001,2,AMBIENT
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-003,1,CHILLED
```

#### Deliverables
- ✅ CSV reading functionality implemented
- ✅ Data parsing is correct
- ✅ Test data preparation completed

---

### Day 18-19: SimulationClock Implementation

#### Task Checklist
- [ ] **Create SimulationClock Class**
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

- [ ] **Configuration Parameters**
  - simStartTime
  - simEndTime
  - tickSeconds
  - speedFactor

- [ ] **Create SimulationRunner**
  - Periodically call tick()
  - Control simulation time advancement

- [ ] **Test Functionality**
  - Time initialization is correct
  - Time advancement works normally
  - Speed factor works correctly

#### Deliverables
- ✅ SimulationClock implemented
- ✅ Time advancement functionality works correctly
- ✅ Configuration is flexible

---

### Day 20-21: Order Injector Implementation

#### Task Checklist
- [ ] **Implement OrderInjector**
  - `initialize()` - Load CSV on startup
  - `loadOrdersFromCSV()` - Load orders
  - `convertToOrderMessage()` - Convert order format
  - `injectOrders()` - Periodically inject orders
  - `publishOrder()` - Publish order message

- [ ] **Order Filtering Logic**
  - Only load orders within simulation time range
  - Send orders based on current simulation time

- [ ] **Integration Testing**
  - Orders can be loaded correctly
  - Orders can be sent by time
  - Log output is clear

#### Key Logic
```java
// Filter orders when loading
orders.stream()
    .filter(order -> simulationClock.isTimeInRange(order.getOrderPlacedTime()))
    .collect(Collectors.toList());

// Check when sending orders
if (!order.getOrderPlacedTime().isAfter(simulationClock.getCurrentTime())) {
    publishOrder(order);
}
```

#### Deliverables
- ✅ OrderInjector complete implementation
- ✅ Order injection functionality works correctly
- ✅ Good integration with simulation clock

---

## 📅 Week 4: Refinement and Optimization

### Day 22-24: REST API Implementation

#### Task Checklist
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

- [ ] **API Testing**
  - Test using Postman or curl
  - Verify return data format
  - Test error handling

#### Deliverables
- ✅ All API endpoints implemented
- ✅ API tests pass
- ✅ Return format is correct

---

### Day 25-26: Log Optimization and Testing

#### Task Checklist
- [ ] **Optimize Log Configuration**
  ```yaml
  logging:
    level:
      org.hibernate: WARN
      com.inventory: INFO
    pattern:
      console: "[%d{yyyy-MM-dd HH:mm:ss}] %msg%n"
  ```

- [ ] **Optimize Log Output**
  - Order processing logs: `[HH:mm:ss] ord-000001 completed successfully`
  - Reduce unnecessary logs
  - Add logs for key operations

- [ ] **Complete Functionality Testing**
  - End-to-end testing
  - Performance testing (optional)
  - Edge case testing

- [ ] **Bug Fixes**
  - Fix discovered issues
  - Code optimization
  - Code review

#### Deliverables
- ✅ Log output is clear
- ✅ Functionality tests pass
- ✅ Code quality is good

---

### Day 27-28: Documentation and Project Summary

#### Task Checklist
- [ ] **Update Documentation**
  - README.md
  - Code comments
  - API documentation

- [ ] **Project Summary**
  - Feature demonstration
  - Problems encountered and solutions
  - Learning outcomes

- [ ] **Code Organization**
  - Unified code formatting
  - Remove debug code
  - Final code review

- [ ] **Project Demo Preparation**
  - Prepare demo script
  - Prepare demo data
  - Prepare PPT (optional)

#### Deliverables
- ✅ Documentation is complete
- ✅ Code organization completed
- ✅ Can perform demonstration
- ✅ Project report completed

---

## 📅 Week 5: Monitoring and Visualization (Advanced)

### Day 29-31: Spring Boot Actuator Integration

#### Task Checklist
- [ ] **Add Actuator Dependencies**
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

- [ ] **Configure Actuator**
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

- [ ] **Verify Metrics Endpoint**
  - Access http://localhost:8080/actuator/prometheus
  - Confirm metrics output is visible

#### Learning Objectives
- Understand Spring Boot Actuator's role
- Master metrics exposure methods
- Understand Prometheus format metrics

#### Deliverables
- ✅ Actuator configuration completed
- ✅ Prometheus metrics endpoint available
- ✅ Can view application metrics

---

### Day 32-33: Custom Metrics Implementation

#### Task Checklist
- [ ] **Add Metrics in OrderManager**
  - Inject `MeterRegistry`
  - Record total orders received (`orders_received_total`)
  - Record total orders processed (by status: SUCCESS/FAILED/ERROR)
  - Record order processing time (`orders_processing_time_seconds`)

- [ ] **Implement Metrics Recording**
  ```java
  @Service
  @RequiredArgsConstructor
  public class OrderManager {
      private final MeterRegistry meterRegistry;
      
      public void handleOrderReceived(...) {
          meterRegistry.counter("orders_received_total").increment();
          Timer.Sample sample = Timer.start(meterRegistry);
          
          try {
              // Process order
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

- [ ] **Test Metrics**
  - Process some orders
  - Check `/actuator/prometheus` endpoint
  - Confirm metrics values update correctly

#### Learning Objectives
- Master Micrometer usage
- Understand Counter, Timer and other metric types
- Learn to customize business metrics

#### Deliverables
- ✅ Custom metrics implementation completed
- ✅ Metrics data recorded correctly
- ✅ Can view metrics through endpoint

---

### Day 34-35: Prometheus + Grafana Deployment

#### Task Checklist
- [ ] **Configure Prometheus**
  - Create `monitoring/prometheus.yml` configuration file
  - Configure scrape target (Spring Boot application)
  - Set scrape interval

- [ ] **Update docker-compose.yml**
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

- [ ] **Start Monitoring Services**
  ```bash
  docker-compose up -d prometheus grafana
  ```

- [ ] **Configure Grafana**
  - Access http://localhost:3000
  - Add Prometheus data source (URL: `http://prometheus:9090`)
  - Test connection

#### Prometheus Configuration Example
```yaml
global:
  scrape_interval: 5s

scrape_configs:
  - job_name: 'inventory-simulator'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['host.docker.internal:8080']
```

#### Learning Objectives
- Understand Prometheus working principles
- Master Prometheus configuration
- Understand Grafana and Prometheus integration

#### Deliverables
- ✅ Prometheus running normally
- ✅ Grafana running normally
- ✅ Data source configuration completed

---

### Day 36-37: Grafana Dashboard Creation

#### Task Checklist
- [ ] **Create Order Processing Dashboard**
  - Total orders received (Stat Panel) - Use PromQL or LogQL
  - Orders succeeded/failed (Time Series) - Use PromQL or LogQL
  - Order success rate (Gauge) - Use PromQL or LogQL
  - Average order processing time (Time Series) - Use PromQL
  - Order log viewing (Logs Panel) - Use LogQL

- [ ] **Common PromQL Queries (Prometheus Metrics)**
  ```promql
  # Total orders received (within time range)
  sum(increase(orders_received_total[$__range]))
  
  # Orders succeeded
  sum(increase(orders_processed_total{status="SUCCESS"}[$__range]))
  
  # Orders failed
  sum(increase(orders_processed_total{status="FAILED"}[$__range]))
  
  # Order success rate (percentage)
  sum(rate(orders_processed_total{status="SUCCESS"}[5m])) 
  / sum(rate(orders_processed_total[5m])) * 100
  
  # Average processing time (seconds)
  sum(increase(orders_processing_time_seconds_sum[$__range])) 
  / sum(increase(orders_processing_time_seconds_count[$__range]))
  ```

- [ ] **Common LogQL Queries (Loki Logs)**
  ```logql
  # Successful orders count (within time range)
  sum(
    count_over_time(
      {application="inventory-simulator"} 
      |= "ORDER_COMPLETED" 
      [5m]
    )
  )
  
  # Failed orders count (within time range)
  sum(
    count_over_time(
      {application="inventory-simulator"} 
      |= "ORDER_FAILED" 
      [5m]
    )
  )
  
  # Total orders count (successful + failed)
  sum(
    count_over_time(
      {application="inventory-simulator"} 
      |~ "ORDER_COMPLETED|ORDER_FAILED" 
      [5m]
    )
  )
  
  # Success rate percentage
  (
    sum(count_over_time({application="inventory-simulator"} |= "ORDER_COMPLETED" [5m]))
    /
    sum(count_over_time({application="inventory-simulator"} |~ "ORDER_COMPLETED|ORDER_FAILED" [5m]))
  ) * 100
  
  # View all successful order logs
  {application="inventory-simulator"} |= "ORDER_COMPLETED"
  
  # View all failed order logs
  {application="inventory-simulator"} |= "ORDER_FAILED"
  ```
  
  > Note: Time range `[5m]` will automatically adjust based on Grafana's time selector

- [ ] **Optimize Dashboard**
  - Set appropriate refresh interval
  - Configure alert rules (optional)
  - Beautify chart styles

#### Learning Objectives
- Master Grafana Dashboard creation
- Understand PromQL query language
- Learn to visualize metrics data

#### Deliverables
- ✅ Dashboard creation completed
- ✅ Key metrics visualized
- ✅ Dashboard is beautiful and practical

---

### Day 38: Monitoring System Testing and Documentation

#### Task Checklist
- [ ] **End-to-End Testing**
  - Run simulation system
  - Observe Grafana Dashboard
  - Verify metrics accuracy

- [ ] **Update Documentation**
  - Update README.md (monitoring section)
  - Update architecture diagrams (add monitoring components)
  - Write monitoring usage guide

- [ ] **Project Summary**
  - Summarize monitoring system's role
  - Record problems encountered and solutions
  - Prepare final demonstration

#### Deliverables
- ✅ Monitoring system runs completely
- ✅ Documentation updated
- ✅ Project can be fully demonstrated

---

## 📊 Progress Tracking Table

### Week 1
| Date | Task | Status | Notes |
|------|------|--------|-------|
| Day 1-2 | Environment Setup | ⬜ | |
| Day 3-4 | Data Model | ⬜ | |
| Day 5-7 | RabbitMQ | ⬜ | |

### Week 2
| Date | Task | Status | Notes |
|------|------|--------|-------|
| Day 8-10 | Order Manager | ⬜ | |
| Day 11-13 | Inventory Manager | ⬜ | |
| Day 14 | Integration Testing | ⬜ | |

### Week 3
| Date | Task | Status | Notes |
|------|------|--------|-------|
| Day 15-17 | CSV Reading | ⬜ | |
| Day 18-19 | SimulationClock | ⬜ | |
| Day 20-21 | Order Injector | ⬜ | |

### Week 4
| Date | Task | Status | Notes |
|------|------|--------|-------|
| Day 22-24 | REST API | ⬜ | |
| Day 25-26 | Optimization & Testing | ⬜ | |
| Day 27-28 | Documentation & Summary | ⬜ | |

### Week 5 (Advanced)
| Date | Task | Status | Notes |
|------|------|--------|-------|
| Day 29-31 | Actuator Integration | ⬜ | |
| Day 32-33 | Custom Metrics | ⬜ | |
| Day 34-35 | Prometheus + Grafana | ⬜ | |
| Day 36-37 | Dashboard Creation | ⬜ | |
| Day 38 | Testing & Documentation | ⬜ | |

### Week 6 (Extension Features)
| Date | Task | Status | Notes |
|------|------|--------|-------|
| Day 39-40 | Database Migration to PostgreSQL | ⬜ | Improve scalability |
| Day 41-42 | Performance Optimization | ⬜ | Optional |
| Day 43-44 | Security Enhancement | ⬜ | Optional |

### Week 7 (Testing & API Documentation)
| Date | Task | Status | Notes |
|------|------|--------|-------|
| Day 45-46 | Unit Tests & Integration Tests | ⬜ | Improve code quality |
| Day 47-48 | API Documentation (Swagger/OpenAPI) | ⬜ | Improve API usability |
| Day 49-50 | Cache Optimization (Redis) | ⬜ | Optional |

### Week 8 (Deployment & Optimization)
| Date | Task | Status | Notes |
|------|------|--------|-------|
| Day 51-52 | Docker Containerization | ⬜ | Improve deployment capability |
| Day 53-54 | CI/CD Pipeline | ⬜ | Optional |
| Day 55-56 | Performance Testing & Optimization | ⬜ | Optional |

---

## 📅 Week 6: Extension Features and Optimization (Optional)

### Day 39-40: Database Migration to PostgreSQL

#### Task Checklist
- [ ] **Add PostgreSQL Dependency**
  ```xml
  <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
  </dependency>
  ```

- [ ] **Configure PostgreSQL Connection**
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

- [ ] **Add PostgreSQL to docker-compose.yml**
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

- [ ] **Test Data Persistence**
  - Restart application, confirm data retained
  - Verify table structure created correctly
  - Test data query performance

#### Learning Objectives
- Understand production environment database selection
- Master database migration methods
- Understand importance of data persistence
- Learn PostgreSQL basic configuration

#### Advantages
- ✅ **Data Persistence**: Data won't be lost on application restart
- ✅ **Scalability**: Supports larger data volumes and concurrency
- ✅ **Production Ready**: Suitable for production deployment
- ✅ **Performance Optimization**: Supports advanced features like indexes, query optimization

#### Deliverables
- ✅ PostgreSQL configuration completed
- ✅ Data migration successful
- ✅ Data persistence verification passed

---

### Day 41-42: Performance Optimization (Optional)

#### Task Checklist
- [ ] **Connection Pool Optimization**
  - Configure HikariCP connection pool parameters
  - Optimize connection count configuration

- [ ] **Query Optimization**
  - Add database indexes
  - Optimize JPA queries

- [ ] **Batch Processing Optimization**
  - Implement batch insert/update
  - Optimize CSV data loading

#### Deliverables
- ✅ Performance optimization completed
- ✅ Performance tests pass

---

### Day 43-44: Security Enhancement (Optional)

#### Task Checklist
- [ ] **API Security**
  - Add API authentication
  - Implement access control

- [ ] **Data Security**
  - Encrypt sensitive data
  - SQL injection protection

#### Deliverables
- ✅ Security features implemented
- ✅ Security tests pass

---

## 📅 Week 7: Testing and API Documentation

### Day 45-46: Unit Tests and Integration Tests

#### Task Checklist
- [ ] **Add Test Dependencies**
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

- [ ] **Write Unit Tests**
  - OrderManager tests
  - InventoryManager tests
  - Repository tests
  - Service method tests

- [ ] **Write Integration Tests**
  - Order processing flow tests
  - Inventory management flow tests
  - Message queue integration tests

- [ ] **Test Coverage**
  - Goal: Core business logic coverage > 70%
  - Use JaCoCo to generate coverage report

#### Learning Objectives
- Master Spring Boot testing framework
- Understand difference between unit tests and integration tests
- Learn to use Mockito for Mock testing
- Understand Test-Driven Development (TDD)

#### Deliverables
- ✅ Unit tests written
- ✅ Integration tests written
- ✅ Test coverage report

---

### Day 47-48: API Documentation (Swagger/OpenAPI)

#### Task Checklist
- [ ] **Add Swagger/OpenAPI Dependencies**
  ```xml
  <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.2.0</version>
  </dependency>
  ```

- [ ] **Configure Swagger**
  ```yaml
  springdoc:
    api-docs:
      path: /api-docs
    swagger-ui:
      path: /swagger-ui.html
  ```

- [ ] **Add API Annotations**
  - `@Operation` - Describe API operation
  - `@ApiResponse` - Describe response
  - `@Parameter` - Describe parameters
  - `@Schema` - Describe data model

- [ ] **Test API Documentation**
  - Access http://localhost:8080/swagger-ui.html
  - Verify all API endpoints have documentation
  - Test "Try it out" functionality in API documentation

#### Learning Objectives
- Understand importance of API documentation
- Master OpenAPI/Swagger specifications
- Learn to write clear API documentation

#### Deliverables
- ✅ Swagger UI accessible
- ✅ All API endpoints have documentation
- ✅ API documentation is clear and complete

---

### Day 49-50: Cache Optimization (Redis - Optional)

#### Task Checklist
- [ ] **Add Redis Dependency**
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-redis</artifactId>
  </dependency>
  ```

- [ ] **Add Redis to docker-compose.yml**
  ```yaml
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
  ```

- [ ] **Configure Redis Cache**
  ```yaml
  spring:
    data:
      redis:
        host: localhost
        port: 6379
    cache:
      type: redis
  ```

- [ ] **Implement Caching**
  - Inventory query caching (`@Cacheable`)
  - Order query caching
  - Cache update strategy

#### Learning Objectives
- Understand cache role and advantages
- Master Spring Cache abstraction
- Learn Redis basic usage

#### Deliverables
- ✅ Redis configuration completed
- ✅ Cache functionality implemented
- ✅ Performance improvement verified

---

## 📅 Week 8: Deployment and Optimization

### Day 51-52: Docker Containerization

#### Task Checklist
- [ ] **Create Dockerfile**
  ```dockerfile
  FROM openjdk:17-jdk-slim
  WORKDIR /app
  COPY target/inventory-simulator-*.jar app.jar
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```

- [ ] **Create .dockerignore**
  ```
  target/
  .git/
  .idea/
  *.iml
  ```

- [ ] **Build Docker Image**
  ```bash
  docker build -t inventory-simulator:latest .
  ```

- [ ] **Update docker-compose.yml**
  - Add application service
  - Configure service dependencies
  - Configure network

- [ ] **Test Containerized Deployment**
  ```bash
  docker-compose up -d
  ```

#### Learning Objectives
- Understand advantages of containerized deployment
- Master Docker basic usage
- Learn to write Dockerfile
- Understand Docker Compose multi-container orchestration

#### Deliverables
- ✅ Dockerfile created
- ✅ Docker image built successfully
- ✅ docker-compose.yml includes all services
- ✅ Containerized deployment tests pass

---

### Day 53-54: CI/CD Pipeline (Optional)

#### Task Checklist
- [ ] **Configure GitHub Actions (or other CI/CD)**
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

- [ ] **Automated Testing**
  - Automatically run tests on code commit
  - Block merge on test failure

- [ ] **Automated Build**
  - Build Docker image
  - Push to image registry (optional)

#### Learning Objectives
- Understand CI/CD concepts and role
- Master GitHub Actions basic usage
- Learn automated build and test processes

#### Deliverables
- ✅ CI/CD pipeline configured
- ✅ Automated tests run normally
- ✅ Automated build successful

---

### Day 55-56: Performance Testing and Optimization

#### Task Checklist
- [ ] **Performance Testing**
  - Use JMeter or Gatling for stress testing
  - Test order processing throughput
  - Test concurrent processing capability

- [ ] **Performance Analysis**
  - Identify performance bottlenecks
  - Analyze database query performance
  - Analyze message queue processing performance

- [ ] **Performance Optimization**
  - Database index optimization
  - Query optimization
  - Connection pool optimization
  - Batch processing optimization

- [ ] **Performance Report**
  - Record performance comparison before and after optimization
  - Summarize optimization experience

#### Learning Objectives
- Understand importance of performance testing
- Master performance testing tool usage
- Learn performance analysis and optimization methods

#### Deliverables
- ✅ Performance testing completed
- ✅ Performance optimization implemented
- ✅ Performance report documentation

---

## 💡 Daily Checklist

At the end of each day, check:
- [ ] Are today's tasks completed?
- [ ] Can the code run normally?
- [ ] What problems were encountered? Were they solved?
- [ ] What needs to be done tomorrow?

---

## 🎯 Milestones

- **End of Week 1**: Foundation setup completed
- **End of Week 2**: Core functionality implementation completed
- **End of Week 3**: Complete functionality implementation completed
- **End of Week 4**: Project completed, can demonstrate
- **End of Week 5** (Advanced): Monitoring system completed, project comprehensively refined
- **End of Week 6** (Extension): Database migration completed, system scalability improved
- **End of Week 7** (Extension): Testing and API documentation completed, code quality improved
- **End of Week 8** (Extension): Deployment and optimization completed, project production-ready

---

## 🔄 Extension Features Description

### Why Extension Features?

The project initially uses **H2 in-memory database**, suitable for rapid development and testing:
- ✅ No installation/configuration needed
- ✅ Fast startup
- ✅ Suitable for learning and prototyping

But H2 limitations:
- ❌ Data not persistent (data lost on application restart)
- ❌ Not suitable for production environment
- ❌ Limited scalability

### Week 6: Migrate to PostgreSQL

**Goal**: Migrate system from H2 to PostgreSQL to improve system scalability and production readiness.

**Learning Value**:
- Understand different database use cases
- Master database migration methods
- Learn production environment database configuration
- Understand importance of data persistence

**Implementation Steps**:
1. Add PostgreSQL dependency
2. Configure database connection
3. Add PostgreSQL service to docker-compose.yml
4. Update application.yml configuration
5. Test data persistence

**Expected Benefits**:
- ✅ Data persistence, data not lost on restart
- ✅ Supports larger data volumes and concurrency
- ✅ Suitable for production deployment
- ✅ Learn production-level database usage

---

**Remember**: The plan is a guide, can be adjusted based on actual situation. What's important is continuous progress!
