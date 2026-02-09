# Inventory Management Simulator - System Design Documentation

## System Overview

This system is an inventory management simulator based on Spring Boot and RabbitMQ, designed to simulate the complete workflow of order processing, inventory management, and order fulfillment.

The system uses **long-format CSV** as a replayable order source and drives event injection through a **simulation clock system (SimulationClock)** with configurable time ranges, enabling reproducible real-world order arrival patterns.

## Architecture Design

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    Inventory Management Simulator                 │
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
└──────────────┘    └──────────────┘    └──────────────┘
        │                     │                     │
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Simulation  │    │  Order DB    │    │ Inventory DB │
│    Clock     │    │  (H2/PostgreSQL)│  │  (H2/PostgreSQL)│
└──────────────┘    └──────────────┘    └──────────────┘
        │
        ▼
┌──────────────┐
│   CSV File   │
│  Order Data  │
└──────────────┘
```

## Core Components

### 1. SimulationClock (Simulation Clock)

**Responsibilities:**
- Manage simulation time system
- Provide time range control and speed acceleration
- Serve as the system's time reference

**Main Functions:**
- `initialize()`: Initialize simulation clock (set start/end time)
- `tick()`: Advance simulation time (based on tickSeconds and speedFactor)
- `getCurrentTime()`: Get current simulation time
- `isRunning()`: Check if simulation is running
- `isTimeInRange()`: Check if time is within simulation range

**Configuration Parameters:**
- `sim-start-time`: Simulation start time (ISO 8601 format)
- `sim-end-time`: Simulation end time (ISO 8601 format)
- `tick-seconds`: Seconds to advance per tick (default 1)
- `speed-factor`: Speed factor (1.0 = normal speed, 2.0 = 2x speed)

### 2. SimulationRunner (Simulation Runner)

**Responsibilities:**
- Periodically call SimulationClock.tick()
- Control simulation clock advancement frequency

**Main Functions:**
- `runSimulationTick()`: Scheduled task to periodically tick simulation clock

**Configuration Parameters:**
- `tick-interval-ms`: Tick interval (milliseconds, default 1000ms)

### 3. OrderCSVReader (CSV Order Reader)

**Responsibilities:**
- Read order data from CSV files
- Support long-format CSV (one order item per line)

**Main Functions:**
- `readOrdersFromCSV()`: Read and parse CSV file

### 4. Order Injector (Order Injector)

**Responsibilities:**
- Load orders from CSV files
- Inject orders into message queue sequentially based on simulation clock time
- Only load orders within simulation time range

**Main Functions:**
- `initialize()`: Load orders from CSV on startup
- `loadOrdersFromCSV()`: Load and filter orders (only keep orders within simulation time range)
- `injectOrders()`: Periodically check and send due orders
- `publishOrder()`: Publish order to RabbitMQ

**Message Publishing:**
- Exchange: `symbotic.simulation`
- Routing Key: `sim.order.received`
- Message Type: `OrderReceivedMessage`

**Order Filtering Logic:**
- Only load orders where `ORDER_PLACED_TIME` is within `[simStartTime, simEndTime]` range
- Send orders based on current simulation time (`ORDER_PLACED_TIME <= currentSimTime`)

### 5. Order Manager (Order Processor)

**Responsibilities:**
- Receive order messages
- Check inventory availability
- Process orders (reserve inventory, deduct inventory)
- Update order status
- Publish order processing results

**Main Functions:**
- `handleOrderReceived()`: Listen to order received messages
- `checkAndReserveInventory()`: Check and reserve inventory
- `processOrder()`: Process order (deduct inventory, update status)

**Message Listening:**
- Queue: `sim.order.received`
- Message Type: `OrderReceivedMessage`

**Message Publishing:**
- Exchange: `symbotic.simulation`
- Routing Key: `sim.inventory.update` (inventory update)
- Routing Key: `sim.order.processed` (order processing completed)

**Log Output Format:**
- Success: `[HH:mm:ss] ord-000001 completed successfully`
- Failure: `[HH:mm:ss] ord-000001 failed - insufficient inventory`

### 6. Monitoring System (Monitoring System)

**Responsibilities:**
- Collect application metrics (order processing count, success rate, processing time, etc.)
- Provide visualization Dashboard
- Monitor system running status in real-time

**Components:**

1. **Spring Boot Actuator**
   - Expose `/actuator/prometheus` endpoint
   - Provide application health status and metrics data

2. **Prometheus**
   - Periodically scrape application metrics (scrape interval: 5s)
   - Store time series data
   - Provide PromQL query interface

3. **Grafana**
   - Connect to Prometheus data source
   - Create visualization Dashboard
   - Monitor key metrics:
     - Total orders received (`orders_received_total`)
     - Total orders processed (by status: SUCCESS/FAILED/ERROR)
     - Order processing success rate
     - Average order processing time

**Key Metrics:**
- `orders_received_total`: Counter, total orders received
- `orders_processed_total{status="SUCCESS|FAILED|ERROR"}`: Counter, total orders processed by status
- `orders_processing_time_seconds`: Timer, order processing time (includes count, sum, max)

**Configuration:**
- Prometheus configuration file: `monitoring/prometheus.yml`
- Grafana default account: admin/admin
- Prometheus port: 9090
- Grafana port: 3000

### 7. Inventory Manager (Inventory Manager)

**Responsibilities:**
- Manage inventory data
- Handle inventory reservation, release, deduction, and replenishment operations
- Automatically detect low inventory and trigger replenishment
- Maintain inventory status

**Main Functions:**
- `handleInventoryUpdate()`: Listen to inventory update messages
- `reserveInventory()`: Reserve inventory
- `releaseInventory()`: Release reserved inventory
- `deductInventory()`: Deduct inventory
- `replenishInventory()`: Replenish inventory
- `checkAndReplenish()`: Automatically detect and replenish

**Message Listening:**
- Queue: `sim.inventory.update`
- Message Type: `InventoryUpdateMessage`

**Operation Types:**
- `RESERVE`: Reserve inventory
- `RELEASE`: Release reserved inventory
- `DEDUCT`: Deduct inventory
- `REPLENISH`: Replenish inventory

## Message Flow

### Order Processing Flow

```
1. SimulationRunner
   └─> Periodically call SimulationClock.tick()
   └─> Advance simulation time

2. Order Injector
   └─> Check orders based on simulation time
   └─> Send due orders
   └─> Publish OrderReceivedMessage
       └─> Exchange: symbotic.simulation
       └─> Routing Key: sim.order.received

3. Order Manager
   └─> Receive OrderReceivedMessage
   └─> Create order entity
   └─> Check inventory
   └─> Publish InventoryUpdateMessage (RESERVE)
       └─> Exchange: symbotic.simulation
       └─> Routing Key: sim.inventory.update

4. Inventory Manager
   └─> Receive InventoryUpdateMessage
   └─> Reserve inventory
   └─> Check if replenishment is needed

5. Order Manager
   └─> Process order
   └─> Publish InventoryUpdateMessage (DEDUCT)
       └─> Exchange: symbotic.simulation
       └─> Routing Key: sim.inventory.update

6. Inventory Manager
   └─> Receive InventoryUpdateMessage
   └─> Deduct inventory
   └─> Check if replenishment is needed

7. Order Manager
   └─> Update order status to COMPLETED
   └─> Publish OrderProcessedMessage
       └─> Exchange: symbotic.simulation
       └─> Routing Key: sim.order.processed
   └─> Output log: [HH:mm:ss] ord-000001 completed successfully
```

## Data Model

### Order (Order)
- `orderId`: Order ID
- `orderType`: Order type (PICKUP/DELIVERY)
- `status`: Order status (PENDING/RECEIVED/PROCESSING/COMPLETED/CANCELLED)
- `orderPlacedTime`: Order placed time
- `orderDueTime`: Order due time
- `items`: Order item list
- `customerId`: Customer ID

### OrderItem (Order Item)
- `sku`: Product SKU
- `quantity`: Quantity
- `temperatureZone`: Temperature zone (AMBIENT/CHILLED/FROZEN)

### InventoryItem (Inventory Item)
- `sku`: Product SKU
- `name`: Product name
- `quantity`: Total inventory quantity
- `reservedQuantity`: Reserved quantity
- `temperatureZone`: Temperature zone
- `lowStockThreshold`: Low stock threshold

### OrderCSVRecord (CSV Order Record)
- `orderId`: Order ID
- `orderType`: Order type
- `orderPlacedTime`: Order placed time (string format)
- `orderDueTime`: Order due time (string format)
- `customerId`: Customer ID
- `sku`: Product SKU
- `quantity`: Quantity
- `temperatureZone`: Temperature zone

## Message Types

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

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Message Queue**: RabbitMQ (Spring AMQP)
- **Database**: H2 (In-memory database, development environment) / PostgreSQL (Production environment, Week 6 extension)
- **ORM**: Spring Data JPA
- **CSV Processing**: OpenCSV
- **Monitoring**: Spring Boot Actuator + Micrometer Prometheus
- **Metrics Collection**: Prometheus
- **Visualization**: Grafana
- **Build Tool**: Maven
- **Java Version**: 17

## Configuration

### Main application.yml Configuration

```yaml
inventory:
  # Simulation clock configuration
  simulation:
    sim-start-time: "2024-01-13T08:00:00"  # Simulation start time
    sim-end-time: "2024-01-13T18:00:00"    # Simulation end time
    tick-seconds: 1                        # Seconds to advance per tick
    tick-interval-ms: 1000                 # Tick interval (milliseconds)
    speed-factor: 1.0                      # Speed factor
  
  # Order injector configuration
  order-injector:
    use-csv: true                          # Use CSV file
    csv-file: data/orders_sample.csv       # CSV file path
  
  # Inventory configuration
  inventory:
    initial-stock: 1000                    # Initial stock
    low-stock-threshold: 100               # Low stock threshold
    replenishment-quantity: 500            # Replenishment quantity
```

## API Endpoints

### Order Related
- `GET /api/orders` - Get all orders
- `GET /api/orders/{orderId}` - Get specific order

### Inventory Related
- `GET /api/inventory/{sku}` - Get inventory for specific SKU
- `POST /api/inventory/initialize?sku=XXX&quantity=100&temperatureZone=AMBIENT` - Initialize inventory

### Health Check
- `GET /api/health` - Check application and RabbitMQ connection status

## CSV Order Format

The system uses **long-format CSV** (one order item per line):

```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-001,2,AMBIENT
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-003,1,CHILLED
```

For detailed format specifications, refer to: [CSV_ORDER_FORMAT.md](CSV_ORDER_FORMAT.md)

## Running Instructions

1. **Start RabbitMQ**
   ```bash
   docker-compose up -d
   ```

2. **Configure Simulation Parameters** (Optional)
   Edit `inventory.simulation` configuration in `application.yml`

3. **Prepare Order CSV File**
   Edit `src/main/resources/data/orders_sample.csv`

4. **Run Application**
   ```bash
   mvn spring-boot:run
   ```

5. **View Runtime Logs**
   - Log format: `[HH:mm:ss] ord-000001 completed successfully`
   - Simulation clock runs within configured time range

6. **Access H2 Console**
   - URL: http://localhost:8080/h2-console
   - JDBC URL: jdbc:h2:mem:inventorydb
   - Username: sa
   - Password: (empty)

7. **Access RabbitMQ Management Interface**
   - URL: http://localhost:15672
   - Username: guest
   - Password: guest

8. **Access Monitoring System**
   - Prometheus UI: http://localhost:9090
   - Grafana Dashboard: http://localhost:3000
   - Default account: admin/admin
   - Add Prometheus data source in Grafana (URL: `http://prometheus:9090`)

## Extension Features (Week 6 - Optional)

### Database Migration to PostgreSQL

**Why Migrate?**

The project initially uses **H2 in-memory database**, suitable for rapid development and learning:
- ✅ No installation/configuration needed, fast startup
- ✅ Suitable for learning and prototyping

But H2 limitations:
- ❌ Data not persistent (data lost on application restart)
- ❌ Not suitable for production environment
- ❌ Limited scalability

**Advantages of Migrating to PostgreSQL:**
- ✅ **Data Persistence**: Data won't be lost on application restart
- ✅ **Scalability**: Supports larger data volumes and concurrent access
- ✅ **Production Ready**: Suitable for production deployment
- ✅ **Performance Optimization**: Supports advanced features like indexes, query optimization

**Migration Steps:**
1. Add PostgreSQL dependency to `pom.xml`
2. Add PostgreSQL service to `docker-compose.yml`
3. Update database configuration in `application.yml`
4. Configure database connection pool (HikariCP)
5. Test data persistence functionality

For detailed instructions, refer to Week 6 section in [WEEKLY_PLAN.md](WEEKLY_PLAN.md).

## System Features

1. **Simulation Clock System**: Uses SimulationClock to manage simulation time with configurable time ranges and speed acceleration
2. **CSV Order Source**: Uses long-format CSV files with high scalability, supports any SKU
3. **Message-Driven Architecture**: Uses RabbitMQ to achieve component decoupling
4. **Asynchronous Processing**: Order processing and inventory management are asynchronous
5. **Auto-Replenishment**: Automatically triggers replenishment when inventory falls below threshold
6. **Transaction Support**: Uses Spring transaction management to ensure data consistency
7. **Optimized Logging**: Concise log format, reduces noise
8. **Extensibility**: Easy to add new message handlers and business logic
9. **Monitoring & Visualization**: Integrated Prometheus + Grafana for real-time monitoring of order processing metrics, success rates, processing times, etc.

## Extension Recommendations

### Week 6: Database Migration to PostgreSQL (Recommended)

**Goal**: Migrate system from H2 to PostgreSQL to improve scalability and production readiness

**Advantages**:
- ✅ Data persistence, data not lost on application restart
- ✅ Supports larger data volumes and concurrent access
- ✅ Suitable for production deployment
- ✅ Learn production-level database configuration and usage

**Implementation Steps**:
1. Add PostgreSQL dependency
2. Add PostgreSQL service to docker-compose.yml
3. Update database configuration in application.yml
4. Test data persistence functionality

For detailed instructions, refer to Week 6 section in [WEEKLY_PLAN.md](WEEKLY_PLAN.md).

### Week 7: Testing and API Documentation (Optional)

**Goal**: Improve code quality and API usability

**Task List**:
- Write unit tests and integration tests
- Generate Swagger/OpenAPI API documentation
- Redis cache optimization (optional)

**Learning Value**:
- Understand Test-Driven Development (TDD)
- Master API documentation writing standards
- Learn cache optimization strategies

For detailed instructions, refer to Week 7 section in [WEEKLY_PLAN.md](WEEKLY_PLAN.md).

### Week 8: Deployment and Optimization (Optional)

**Goal**: Achieve production-ready deployment solution

**Task List**:
- Docker containerization of application
- CI/CD pipeline configuration
- Performance testing and optimization

**Learning Value**:
- Understand containerized deployment
- Master CI/CD pipeline
- Learn performance optimization methods

For detailed instructions, refer to Week 8 section in [WEEKLY_PLAN.md](WEEKLY_PLAN.md).

### Other Extension Recommendations (Optional)

1. **Add More Components**: Such as Pick Station, Tote Manager, etc.
2. **Distributed Tracing**: Integrate Zipkin or Jaeger
3. **Security Enhancement**: Add authentication, authorization, API security, etc.
4. **Message Retry Mechanism**: Implement message processing failure retry
5. **Batch Processing Optimization**: Implement batch order processing
6. **Simulation Time Synchronization**: Support simulation time synchronization across multiple instances
