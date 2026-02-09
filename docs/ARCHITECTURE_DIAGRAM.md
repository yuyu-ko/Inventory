# System Architecture Diagrams

## Overall Architecture

```mermaid
graph TB
    subgraph "Inventory Management Simulator"
        SC[SimulationClock]
        SR[SimulationRunner]
        OI[Order Injector]
        OM[Order Manager]
        IM[Inventory Manager]
    end
    
    subgraph "Data Source"
        CSV[CSV File<br/>Order Data]
    end
    
    subgraph "RabbitMQ Message Broker"
        EX[Topic Exchange<br/>symbotic.simulation]
        Q1[Queue: sim.order.received]
        Q2[Queue: sim.inventory.update]
        Q3[Queue: sim.order.processed]
    end
    
    subgraph "Data Storage"
        DB1[(Order DB<br/>H2)]
        DB2[(Inventory DB<br/>H2)]
    end
    
    subgraph "REST API"
        API1[Order API]
        API2[Inventory API]
        API3[Health API]
    end
    
    subgraph "Monitoring System"
        ACT[Spring Boot Actuator<br/>/actuator/prometheus]
        PROM[Prometheus<br/>Metrics Collector]
        GRAF[Grafana<br/>Dashboard]
    end
    
    CSV -->|Read| OI
    SR -->|tick| SC
    SC -->|Current Time| OI
    OI -->|Publish Order| EX
    EX -->|Route| Q1
    Q1 -->|Consume| OM
    OM -->|Query/Update| DB1
    OM -->|Publish Inventory Update| EX
    EX -->|Route| Q2
    Q2 -->|Consume| IM
    IM -->|Query/Update| DB2
    OM -->|Publish Result| EX
    EX -->|Route| Q3
    
    API1 --> OM
    API2 --> IM
    API3 --> SC
    
    OM -->|Metrics| ACT
    ACT -->|Scrape| PROM
    PROM -->|Query| GRAF
    
    style SC fill:#e1f5ff
    style SR fill:#e1f5ff
    style OI fill:#fff4e1
    style OM fill:#fff4e1
    style IM fill:#e8f5e9
    style EX fill:#f3e5f5
    style CSV fill:#fce4ec
    style ACT fill:#ff9800
    style PROM fill:#ff9800
    style GRAF fill:#ff5722
```

## Message Flow Diagram

```mermaid
sequenceDiagram
    participant SR as SimulationRunner
    participant SC as SimulationClock
    participant CSV as CSV File
    participant OI as Order Injector
    participant MQ as RabbitMQ
    participant OM as Order Manager
    participant IM as Inventory Manager
    participant DB1 as Order DB
    participant DB2 as Inventory DB
    
    Note over SR: Scheduled tick (every 1000ms)
    SR->>SC: tick()
    SC->>SC: Advance simulation time
    
    Note over OI: Check orders based on simulation time
    SC->>OI: getCurrentTime()
    CSV->>OI: Read order data (on startup)
    OI->>OI: Filter orders (within simulation time range)
    
    alt Order Due (ORDER_PLACED_TIME <= currentSimTime)
        OI->>MQ: Publish OrderReceivedMessage<br/>(sim.order.received)
        
        MQ->>OM: Consume order message
        OM->>DB1: Save order (RECEIVED)
        
        OM->>MQ: Publish InventoryUpdateMessage<br/>(RESERVE, sim.inventory.update)
        
        MQ->>IM: Consume inventory update message
        IM->>DB2: Check inventory
        IM->>DB2: Reserve inventory
        
        alt Sufficient Inventory
            IM-->>OM: Inventory reserved successfully
            OM->>DB1: Update order status (PROCESSING)
            OM->>MQ: Publish InventoryUpdateMessage<br/>(DEDUCT, sim.inventory.update)
            
            MQ->>IM: Consume deduct inventory message
            IM->>DB2: Deduct inventory
            
            alt Inventory Below Threshold
                IM->>DB2: Auto-replenish
            end
            
            OM->>DB1: Update order status (COMPLETED)
            OM->>MQ: Publish OrderProcessedMessage<br/>(sim.order.processed)
            Note over OM: Output log:<br/>[HH:mm:ss] ord-000001<br/>completed successfully
        else Insufficient Inventory
            OM->>DB1: Update order status (CANCELLED)
            OM->>MQ: Publish OrderProcessedMessage<br/>(FAILED)
            Note over OM: Output log:<br/>[HH:mm:ss] ord-000001<br/>failed - insufficient inventory
        end
    end
```

## Component Interaction Diagram

```mermaid
graph LR
    subgraph "SimulationClock Component"
        SC1[Initialize<br/>simStartTime/simEndTime]
        SC2[Tick<br/>Advance Time]
        SC3[Get Current Time<br/>getCurrentTime]
    end
    
    subgraph "Order Injector Component"
        OI1[Load CSV<br/>@PostConstruct]
        OI2[Filter Orders<br/>Simulation Time Range]
        OI3[Check Order Time<br/>ORDER_PLACED_TIME <= currentSimTime]
        OI4[Publish Order]
    end
    
    subgraph "Order Manager Component"
        OM1[Listen Order Messages<br/>@RabbitListener]
        OM2[Create Order Entity]
        OM3[Check Inventory]
        OM4[Process Order]
        OM5[Update Order Status]
    end
    
    subgraph "Inventory Manager Component"
        IM1[Listen Inventory Messages<br/>@RabbitListener]
        IM2[Reserve Inventory]
        IM3[Deduct Inventory]
        IM4[Auto-Replenish]
    end
    
    SC1 --> SC2
    SC2 --> SC3
    SC3 --> OI3
    OI1 --> OI2
    OI2 --> OI3
    OI3 --> OI4
    OI4 --> OM1
    OM1 --> OM2
    OM2 --> OM3
    OM3 --> IM1
    IM1 --> IM2
    IM2 --> OM4
    OM4 --> IM1
    IM1 --> IM3
    IM3 --> IM4
    IM4 --> OM5
    
    style SC1 fill:#e1f5ff
    style SC2 fill:#e1f5ff
    style SC3 fill:#e1f5ff
    style OI1 fill:#fff4e1
    style OM1 fill:#fff4e1
    style IM1 fill:#e8f5e9
```

## Data Model Relationship Diagram

```mermaid
erDiagram
    Order ||--o{ OrderItem : contains
    Order {
        Long id PK
        String orderId UK
        OrderType orderType
        OrderStatus status
        LocalDateTime orderPlacedTime
        LocalDateTime orderDueTime
        String customerId
    }
    
    OrderItem {
        String sku
        Integer quantity
        String temperatureZone
    }
    
    InventoryItem {
        Long id PK
        String sku UK
        String name
        Integer quantity
        Integer reservedQuantity
        String temperatureZone
        Integer lowStockThreshold
    }
    
    OrderItem }o--|| InventoryItem : references
    
    OrderCSVRecord {
        String orderId
        String orderType
        String orderPlacedTime
        String orderDueTime
        String customerId
        String sku
        Integer quantity
        String temperatureZone
    }
    
    OrderCSVRecord ||--o{ Order : "converts to"
```

## Simulation Clock Flow Diagram

```mermaid
flowchart TD
    Start([System Startup]) --> Init[SimulationClock.initialize]
    Init --> SetTime[Set simStartTime/simEndTime<br/>currentSimTime = simStartTime]
    SetTime --> Running{Simulation Running?}
    
    Running -->|Yes| Tick[SimulationRunner.tick]
    Tick --> Calc[Calculate Time Increment<br/>secondsToAdd = tickSeconds × speedFactor]
    Calc --> Update[Update currentSimTime]
    Update --> Check{Reached End Time?}
    
    Check -->|No| Running
    Check -->|Yes| Stop[Stop Simulation<br/>isRunning = false]
    Stop --> End([Simulation End])
    
    Running -->|No| End
    
    style Start fill:#e1f5ff
    style End fill:#ffccbc
    style Tick fill:#fff4e1
    style Stop fill:#ffccbc
```

## Order Processing Flow Diagram

```mermaid
flowchart TD
    Start([Order Arrives]) --> Load[Order Injector<br/>Load CSV Orders]
    Load --> Filter[Filter Orders<br/>Within Simulation Time Range]
    Filter --> Queue[Enqueue Orders]
    
    Queue --> CheckTime{Simulation Time Check<br/>ORDER_PLACED_TIME <= currentSimTime?}
    
    CheckTime -->|No| Wait[Wait]
    Wait --> CheckTime
    
    CheckTime -->|Yes| Publish[Publish Order Message]
    Publish --> Receive[Order Manager<br/>Receive Order]
    Receive --> Create[Create Order Entity]
    Create --> CheckInv{Check Inventory}
    
    CheckInv -->|Insufficient| Failed[Order Failed<br/>Status: CANCELLED]
    Failed --> LogFail[Log: failed - insufficient inventory]
    LogFail --> End1([End])
    
    CheckInv -->|Sufficient| Reserve[Reserve Inventory]
    Reserve --> Process[Process Order]
    Process --> Deduct[Deduct Inventory]
    Deduct --> Complete[Order Completed<br/>Status: COMPLETED]
    Complete --> LogSuccess[Log: completed successfully]
    LogSuccess --> End2([End])
    
    style Start fill:#e1f5ff
    style End1 fill:#ffccbc
    style End2 fill:#c8e6c9
    style CheckInv fill:#fff4e1
    style LogSuccess fill:#c8e6c9
    style LogFail fill:#ffccbc
```

## Message Type Diagram

```mermaid
classDiagram
    class OrderReceivedMessage {
        +String orderId
        +OrderType orderType
        +LocalDateTime orderPlacedTime
        +LocalDateTime orderDueTime
        +List~OrderItemDTO~ items
        +String customerId
        +String senderId
    }
    
    class InventoryUpdateMessage {
        +String sku
        +Integer quantityChange
        +Integer reservedQuantityChange
        +String operation
        +String orderId
    }
    
    class OrderProcessedMessage {
        +String orderId
        +String status
        +LocalDateTime processedTime
        +String message
    }
    
    class OrderItemDTO {
        +String sku
        +Integer quantity
        +String temperatureZone
    }
    
    OrderReceivedMessage *-- OrderItemDTO
```

## Deployment Architecture Diagram

```mermaid
graph TB
    subgraph "Development Environment"
        APP[Spring Boot Application<br/>Port: 8080<br/>Actuator: /actuator/prometheus]
        RABBIT[RabbitMQ<br/>Port: 5672, 15672]
        H2[H2 Database<br/>In-Memory]
        CSV[CSV File<br/>resources/data/]
        
        subgraph "Monitoring System"
            PROM[Prometheus<br/>Port: 9090]
            GRAF[Grafana<br/>Port: 3000]
        end
    end
    
    subgraph "Production Environment Recommendations"
        APP2[Spring Boot Application<br/>Multiple Instances]
        RABBIT2[RabbitMQ Cluster]
        DB[PostgreSQL/MySQL<br/>Master-Slave Replication]
        CACHE[Redis Cache]
        MONITOR[Monitoring System<br/>Prometheus + Grafana]
        LOG[Log Aggregation<br/>ELK Stack]
    end
    
    CSV --> APP
    APP --> RABBIT
    APP --> H2
    APP -->|Metrics| PROM
    PROM -->|Query| GRAF
    
    APP2 --> RABBIT2
    APP2 --> DB
    APP2 --> CACHE
    APP2 --> MONITOR
    APP2 --> LOG
    MONITOR --> APP2
    MONITOR --> RABBIT2
    
    style APP fill:#e1f5ff
    style APP2 fill:#fff4e1
    style RABBIT fill:#f3e5f5
    style RABBIT2 fill:#f3e5f5
    style CSV fill:#fce4ec
    style PROM fill:#ff9800
    style GRAF fill:#ff5722
```

## Timeline Diagram

```mermaid
gantt
    title Simulation Timeline Example
    dateFormat HH:mm
    axisFormat %H:%M
    
    section Simulation Clock
    08:00 Start           :milestone, m1, 08:00, 0m
    08:00 - 18:00 Running    :active, sim, 08:00, 10h
    18:00 End           :milestone, m2, 18:00, 0m
    
    section Order Processing
    ORD-000001 Process      :ord1, 08:00, 1m
    ORD-000002 Process      :ord2, 08:15, 1m
    ORD-000003 Process      :ord3, 09:00, 1m
    ORD-000004 Process      :ord4, 09:30, 1m
    ORD-000005 Process      :ord5, 10:00, 1m
```
