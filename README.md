# Inventory Management Simulator

An inventory management simulation system based on Spring Boot and RabbitMQ, designed to simulate the complete workflow of order processing, inventory management, and order fulfillment.

The system uses **long-format CSV** as a replayable order source. On startup, the system aggregates CSV data into an order event stream and drives event injection through a **simulation clock system** with configurable time ranges (start time/end time/speed factor), enabling reproducible real-world order arrival patterns and validation of inventory consistency and processing workflows.

## Features

- ✅ **SimulationClock**: Manages simulation time with configurable time ranges and speed acceleration
- ✅ **Order Injector**: Reads orders from CSV files and injects them sequentially based on simulation time
- ✅ **Order Manager**: Receives orders, checks inventory, and processes orders
- ✅ **Inventory Manager**: Manages inventory, reservations, deductions, and automatic replenishment
- ✅ **Monitoring & Visualization**: Integrated Prometheus + Grafana for real-time monitoring of order processing status, success rates, processing times, and other metrics
- ✅ **Log Management**: Integrated Grafana Loki for collecting and querying order processing logs with structured log query support

## Technology Stack

- Spring Boot 3.2.0
- RabbitMQ (Spring AMQP)
- H2 Database (In-memory) / PostgreSQL (Production, Week 6 Extension)
- Spring Data JPA
- OpenCSV (CSV file reading)
- Spring Boot Actuator (Metrics)
- Prometheus (Metrics Collection)
- Grafana (Monitoring Dashboard)
- Grafana Loki (Log Management)
- Maven
- Java 17

## Extensible Features

The project design supports the following extensible features for advanced development:

- 🔄 **Database Migration** (Week 6): Migrate from H2 in-memory database to PostgreSQL, improving system scalability and data persistence capabilities
- 📊 **Monitoring Enhancement** (Week 5): Integrated Prometheus + Grafana + Loki monitoring stack
- 🔍 **Log Analysis** (Week 5): Structured log querying and analysis using Grafana Loki
- 🧪 **Testing & Documentation** (Week 7): Unit tests, integration tests, Swagger API documentation
- ⚡ **Cache Optimization** (Week 7): Integrate Redis caching to improve query performance
- 🐳 **Containerization** (Week 8): Docker containerization, Docker Compose orchestration
- 🔄 **CI/CD Pipeline** (Week 8): Automated build, test, and deployment workflows
- 🚀 **Performance Optimization** (Week 8): Performance testing, bottleneck analysis, optimization implementation
- 🔐 **Security Enhancement** (Optional): Add authentication, authorization, API security, etc.

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+ (Optional, not required if using IDE)
- Docker (for running RabbitMQ)

> **Note**: If Maven is not installed, you can use an IDE (IntelliJ IDEA/Eclipse) to run the project directly.  
> For detailed setup instructions, refer to: [SETUP.md](SETUP.md)

### Startup Steps

1. **Start RabbitMQ**
   ```bash
   docker-compose up -d rabbitmq
   ```
   or
   ```bash
   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   ```

2. **Prepare Order CSV File**
   - Edit `src/main/resources/data/orders_sample.csv`
   - Format reference: [docs/CSV_ORDER_FORMAT.md](docs/CSV_ORDER_FORMAT.md)

3. **Configure Simulation Parameters** (Optional)
   Edit `src/main/resources/application.yml`:
   ```yaml
   inventory:
     simulation:
       sim-start-time: "2024-01-13T08:00:00"  # Simulation start time
       sim-end-time: "2024-01-13T18:00:00"    # Simulation end time
       tick-seconds: 1                        # Seconds to advance per tick
       speed-factor: 1.0                      # Speed factor (1.0 = normal speed)
   ```

4. **Run Application**
   ```bash
   mvn spring-boot:run
   ```
   Or run `InventorySimulatorApplication` using your IDE

5. **View Runtime Logs**
   - Log format: `[HH:mm:ss] ord-000001 completed successfully`
   - Simulation clock runs within the configured time range
   - Orders are processed in the sequence specified in the CSV file

6. **Access Services**
   - Application: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console
   - RabbitMQ Management: http://localhost:15672 (guest/guest)
   - Prometheus Metrics: http://localhost:8080/actuator/prometheus
   - Prometheus UI: http://localhost:9090
   - Grafana Dashboard: http://localhost:3000 (admin/admin)

7. **Start Monitoring System** (Optional)
   ```bash
   docker-compose up -d prometheus grafana
   ```
   Then configure Prometheus data source in Grafana (URL: `http://prometheus:9090`)

## Troubleshooting

If you encounter issues connecting to `localhost:8080`, refer to [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for detailed troubleshooting guidance.

**Quick Checks:**
1. Confirm application is running (check console logs)
2. Confirm RabbitMQ container is running: `docker ps`
3. Access health check: `http://localhost:8080/api/health`

## API Usage

### Health Check
```bash
# Check application and RabbitMQ connection status
curl http://localhost:8080/api/health
```

### Query Orders
```bash
# Get all orders
curl http://localhost:8080/api/orders

# Get specific order
curl http://localhost:8080/api/orders/ORD-000001
```

### Query Inventory
```bash
# Get inventory for specific SKU
curl http://localhost:8080/api/inventory/SKU-001

# Initialize inventory
curl -X POST "http://localhost:8080/api/inventory/initialize?sku=SKU-001&quantity=1000&temperatureZone=AMBIENT"
```

## System Architecture

```
SimulationClock → Order Injector → RabbitMQ → Order Manager → Inventory Manager
      ↓                  ↓             ↓              ↓                ↓
  Simulation        CSV Reader    Message Queue   Order Processing  Inventory Management
     Clock
```

For detailed design documentation, refer to: [docs/SYSTEM_DESIGN.md](docs/SYSTEM_DESIGN.md)

## Project Structure

```
Inventory/
├── src/main/
│   ├── java/com/inventory/
│   │   ├── InventorySimulatorApplication.java  # Main application class
│   │   ├── config/
│   │   │   └── RabbitMQConfig.java             # RabbitMQ configuration
│   │   ├── controller/
│   │   │   ├── OrderController.java            # Order REST API
│   │   │   ├── InventoryController.java        # Inventory REST API
│   │   │   └── HealthController.java            # Health check API
│   │   ├── message/
│   │   │   ├── OrderReceivedMessage.java       # Order received message
│   │   │   ├── InventoryUpdateMessage.java     # Inventory update message
│   │   │   └── OrderProcessedMessage.java       # Order processed message
│   │   ├── model/
│   │   │   ├── Order.java                      # Order entity
│   │   │   ├── OrderItem.java                  # Order item entity
│   │   │   ├── InventoryItem.java              # Inventory item entity
│   │   │   └── OrderCSVRecord.java             # CSV order record
│   │   ├── repository/
│   │   │   ├── OrderRepository.java            # Order repository
│   │   │   └── InventoryItemRepository.java    # Inventory repository
│   │   └── service/
│   │       ├── SimulationClock.java            # Simulation clock
│   │       ├── SimulationRunner.java           # Simulation runner
│   │       ├── OrderCSVReader.java             # CSV order reader
│   │       ├── OrderInjector.java              # Order injector
│   │       ├── OrderManager.java               # Order processor
│   │       └── InventoryManager.java           # Inventory manager
│   └── resources/
│       ├── application.yml                      # Application configuration
│       ├── logback-spring.xml                   # Logback logging configuration (Loki)
│       └── data/
│           ├── orders_sample.csv                # Order sample data
│           └── inventory_sample.csv             # Inventory sample data
├── monitoring/                                  # Monitoring configuration directory
│   ├── grafana/
│   │   └── provisioning/
│   │       └── datasources/
│   │           └── loki.yml                    # Grafana Loki data source configuration
│   ├── loki-config.yaml                        # Loki configuration file
│   └── prometheus.yml                          # Prometheus configuration file
├── docs/                                        # Documentation directory
│   ├── PROJECT_GUIDE.md                         # Project guide
│   ├── WEEKLY_PLAN.md                          # Weekly plan (Week 1-8)
│   ├── QUICKSTART.md                           # Quick start guide
│   ├── IMPLEMENTATION_GUIDE.md                 # Implementation guide
│   ├── CODE_TEMPLATES.md                       # Code templates
│   ├── SYSTEM_DESIGN.md                        # System design documentation
│   └── ...                                      # Other documentation
├── docker-compose.yml                           # Docker Compose configuration
├── pom.xml                                      # Maven project configuration
├── README.md                                    # Project documentation
└── backups/                                     # Grafana backup directory (.gitignore)
```

## Configuration

Main configuration in `application.yml`:

```yaml
inventory:
  # Simulation clock configuration
  simulation:
    sim-start-time: "2024-01-13T08:00:00"  # Simulation start time
    sim-end-time: "2024-01-13T18:00:00"    # Simulation end time
    tick-seconds: 1                        # Seconds to advance per tick
    tick-interval-ms: 1000                 # Tick interval (milliseconds)
    speed-factor: 1.0                      # Speed factor (1.0 = normal, 2.0 = 2x speed)
  
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

## Simulation Clock System

The system uses **SimulationClock** to manage simulation time:

- **sim-start-time**: Simulation start time (ISO 8601 format)
- **sim-end-time**: Simulation end time (ISO 8601 format)
- **tick-seconds**: Seconds to advance per clock tick (default 1 second)
- **speed-factor**: Speed factor (1.0 = normal speed, 2.0 = 2x speed)

The simulation clock runs within the configured time range, and orders are compared with the current simulation time based on the `ORDER_PLACED_TIME` in the CSV file to determine when to send them.

## CSV Order Format

Order data uses **long-format CSV** (one order item per line):

```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-001,2,AMBIENT
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-003,1,CHILLED
```

For detailed format specifications, refer to: [docs/CSV_ORDER_FORMAT.md](docs/CSV_ORDER_FORMAT.md)

## Message Flow

1. **SimulationClock** advances simulation time
2. **Order Injector** reads CSV file and sends orders to `sim.order.received` queue based on simulation time
3. **Order Manager** receives orders, checks inventory, and publishes inventory reservation requests to `sim.inventory.update` queue
4. **Inventory Manager** processes inventory operations (reserve/deduct/replenish)
5. **Order Manager** completes order processing and publishes results to `sim.order.processed` queue

## Log Output

The system uses optimized log format for clear and concise output:

```
[08:00:00] ord-000001 received
[08:00:01] ord-000001 completed successfully
[08:00:02] ord-000002 received
[08:00:03] ord-000002 completed successfully
```

### Structured Logging

The system also outputs structured logs to Grafana Loki for querying and analysis:

- **Order Received**: `ORDER_RECEIVED | orderId=ORD-000001 | orderType=PICKUP | ...`
- **Order Processing**: `ORDER_PROCESSING | orderId=ord-000001 | status=PROCESSING | ...`
- **Order Completed**: `ORDER_COMPLETED | orderId=ord-000001 | items=[SKU-001:2] | ...`
- **Order Failed**: `ORDER_FAILED | orderId=ord-000002 | reason=INSUFFICIENT_INVENTORY | ...`

### LogQL Query Examples

Use LogQL queries in Grafana to query order statistics:

**Successful Orders Count (within time range):**
```logql
sum(
  count_over_time(
    {application="inventory-simulator"} 
    |= "ORDER_COMPLETED" 
    [5m]
  )
)
```

**Failed Orders Count (within time range):**
```logql
sum(
  count_over_time(
    {application="inventory-simulator"} 
    |= "ORDER_FAILED" 
    [5m]
  )
)
```

**Total Orders Count (successful + failed):**
```logql
sum(
  count_over_time(
    {application="inventory-simulator"} 
    |~ "ORDER_COMPLETED|ORDER_FAILED" 
    [5m]
  )
)
```

**Success Rate Percentage:**
```logql
(
  sum(count_over_time({application="inventory-simulator"} |= "ORDER_COMPLETED" [5m]))
  /
  sum(count_over_time({application="inventory-simulator"} |~ "ORDER_COMPLETED|ORDER_FAILED" [5m]))
) * 100
```

> Note: The time range `[5m]` will automatically adjust based on Grafana's time selector

Hibernate SQL logging is disabled to reduce log noise.

## Development Notes

### Adding New Message Types

1. Create a new message class in the `message/` package
2. Add queue and binding configuration in `RabbitMQConfig.java`
3. Add `@RabbitListener` methods in the corresponding Service

### Adding New Business Logic

1. Add methods in the corresponding Service class
2. Use `RabbitTemplate` to publish messages
3. Use `@RabbitListener` to listen to messages

### Customizing Simulation Parameters

Modify the `inventory.simulation` configuration items in `application.yml` to adjust simulation behavior.

## License

MIT License
