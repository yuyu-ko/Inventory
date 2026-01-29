# 系统架构图

## 整体架构

```mermaid
graph TB
    subgraph "库存管理模拟器"
        SC[SimulationClock<br/>模拟时钟]
        SR[SimulationRunner<br/>模拟运行器]
        OI[Order Injector<br/>订单注入器]
        OM[Order Manager<br/>订单处理器]
        IM[Inventory Manager<br/>库存管理器]
    end
    
    subgraph "数据源"
        CSV[CSV File<br/>订单数据]
    end
    
    subgraph "RabbitMQ Message Broker"
        EX[Topic Exchange<br/>symbotic.simulation]
        Q1[Queue: sim.order.received]
        Q2[Queue: sim.inventory.update]
        Q3[Queue: sim.order.processed]
    end
    
    subgraph "数据存储"
        DB1[(订单数据库<br/>H2)]
        DB2[(库存数据库<br/>H2)]
    end
    
    subgraph "REST API"
        API1[Order API]
        API2[Inventory API]
        API3[Health API]
    end
    
    subgraph "监控系统"
        ACT[Spring Boot Actuator<br/>/actuator/prometheus]
        PROM[Prometheus<br/>Metrics Collector]
        GRAF[Grafana<br/>Dashboard]
    end
    
    CSV -->|读取| OI
    SR -->|tick| SC
    SC -->|当前时间| OI
    OI -->|发布订单| EX
    EX -->|路由| Q1
    Q1 -->|消费| OM
    OM -->|查询/更新| DB1
    OM -->|发布库存更新| EX
    EX -->|路由| Q2
    Q2 -->|消费| IM
    IM -->|查询/更新| DB2
    OM -->|发布处理结果| EX
    EX -->|路由| Q3
    
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

## 消息流程图

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
    
    Note over SR: 定时 tick (每1000ms)
    SR->>SC: tick()
    SC->>SC: 推进模拟时间
    
    Note over OI: 根据模拟时间检查订单
    SC->>OI: getCurrentTime()
    CSV->>OI: 读取订单数据（启动时）
    OI->>OI: 过滤订单（模拟时间范围内）
    
    alt 订单到期 (ORDER_PLACED_TIME <= currentSimTime)
        OI->>MQ: 发布 OrderReceivedMessage<br/>(sim.order.received)
        
        MQ->>OM: 消费订单消息
        OM->>DB1: 保存订单 (RECEIVED)
        
        OM->>MQ: 发布 InventoryUpdateMessage<br/>(RESERVE, sim.inventory.update)
        
        MQ->>IM: 消费库存更新消息
        IM->>DB2: 检查库存
        IM->>DB2: 预留库存
        
        alt 库存充足
            IM-->>OM: 库存预留成功
            OM->>DB1: 更新订单状态 (PROCESSING)
            OM->>MQ: 发布 InventoryUpdateMessage<br/>(DEDUCT, sim.inventory.update)
            
            MQ->>IM: 消费扣除库存消息
            IM->>DB2: 扣除库存
            
            alt 库存低于阈值
                IM->>DB2: 自动补货
            end
            
            OM->>DB1: 更新订单状态 (COMPLETED)
            OM->>MQ: 发布 OrderProcessedMessage<br/>(sim.order.processed)
            Note over OM: 输出日志:<br/>[HH:mm:ss] ord-000001<br/>completed successfully
        else 库存不足
            OM->>DB1: 更新订单状态 (CANCELLED)
            OM->>MQ: 发布 OrderProcessedMessage<br/>(FAILED)
            Note over OM: 输出日志:<br/>[HH:mm:ss] ord-000001<br/>failed - insufficient inventory
        end
    end
```

## 组件交互图

```mermaid
graph LR
    subgraph "SimulationClock 组件"
        SC1[初始化<br/>simStartTime/simEndTime]
        SC2[Tick<br/>推进时间]
        SC3[获取当前时间<br/>getCurrentTime]
    end
    
    subgraph "Order Injector 组件"
        OI1[加载 CSV<br/>@PostConstruct]
        OI2[过滤订单<br/>模拟时间范围]
        OI3[检查订单时间<br/>ORDER_PLACED_TIME <= currentSimTime]
        OI4[发布订单]
    end
    
    subgraph "Order Manager 组件"
        OM1[监听订单消息<br/>@RabbitListener]
        OM2[创建订单实体]
        OM3[检查库存]
        OM4[处理订单]
        OM5[更新订单状态]
    end
    
    subgraph "Inventory Manager 组件"
        IM1[监听库存消息<br/>@RabbitListener]
        IM2[预留库存]
        IM3[扣除库存]
        IM4[自动补货]
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

## 数据模型关系图

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

## 模拟时钟流程图

```mermaid
flowchart TD
    Start([系统启动]) --> Init[SimulationClock.initialize]
    Init --> SetTime[设置 simStartTime/simEndTime<br/>currentSimTime = simStartTime]
    SetTime --> Running{模拟运行中?}
    
    Running -->|是| Tick[SimulationRunner.tick]
    Tick --> Calc[计算时间增量<br/>secondsToAdd = tickSeconds × speedFactor]
    Calc --> Update[更新 currentSimTime]
    Update --> Check{到达结束时间?}
    
    Check -->|否| Running
    Check -->|是| Stop[停止模拟<br/>isRunning = false]
    Stop --> End([模拟结束])
    
    Running -->|否| End
    
    style Start fill:#e1f5ff
    style End fill:#ffccbc
    style Tick fill:#fff4e1
    style Stop fill:#ffccbc
```

## 订单处理流程图

```mermaid
flowchart TD
    Start([订单到达]) --> Load[Order Injector<br/>加载 CSV 订单]
    Load --> Filter[过滤订单<br/>模拟时间范围内]
    Filter --> Queue[订单入队]
    
    Queue --> CheckTime{模拟时间检查<br/>ORDER_PLACED_TIME <= currentSimTime?}
    
    CheckTime -->|否| Wait[等待]
    Wait --> CheckTime
    
    CheckTime -->|是| Publish[发布订单消息]
    Publish --> Receive[Order Manager<br/>接收订单]
    Receive --> Create[创建订单实体]
    Create --> CheckInv{检查库存}
    
    CheckInv -->|不足| Failed[订单失败<br/>状态: CANCELLED]
    Failed --> LogFail[日志: failed - insufficient inventory]
    LogFail --> End1([结束])
    
    CheckInv -->|充足| Reserve[预留库存]
    Reserve --> Process[处理订单]
    Process --> Deduct[扣除库存]
    Deduct --> Complete[订单完成<br/>状态: COMPLETED]
    Complete --> LogSuccess[日志: completed successfully]
    LogSuccess --> End2([结束])
    
    style Start fill:#e1f5ff
    style End1 fill:#ffccbc
    style End2 fill:#c8e6c9
    style CheckInv fill:#fff4e1
    style LogSuccess fill:#c8e6c9
    style LogFail fill:#ffccbc
```

## 消息类型图

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

## 部署架构图

```mermaid
graph TB
    subgraph "开发环境"
        APP[Spring Boot Application<br/>Port: 8080<br/>Actuator: /actuator/prometheus]
        RABBIT[RabbitMQ<br/>Port: 5672, 15672]
        H2[H2 Database<br/>In-Memory]
        CSV[CSV File<br/>resources/data/]
        
        subgraph "监控系统"
            PROM[Prometheus<br/>Port: 9090]
            GRAF[Grafana<br/>Port: 3000]
        end
    end
    
    subgraph "生产环境建议"
        APP2[Spring Boot Application<br/>多实例]
        RABBIT2[RabbitMQ Cluster]
        DB[PostgreSQL/MySQL<br/>主从复制]
        CACHE[Redis Cache]
        MONITOR[监控系统<br/>Prometheus + Grafana]
        LOG[日志聚合<br/>ELK Stack]
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

## 时间线图

```mermaid
gantt
    title 模拟时间线示例
    dateFormat HH:mm
    axisFormat %H:%M
    
    section 模拟时钟
    08:00 开始           :milestone, m1, 08:00, 0m
    08:00 - 18:00 运行    :active, sim, 08:00, 10h
    18:00 结束           :milestone, m2, 18:00, 0m
    
    section 订单处理
    ORD-000001 处理      :ord1, 08:00, 1m
    ORD-000002 处理      :ord2, 08:15, 1m
    ORD-000003 处理      :ord3, 09:00, 1m
    ORD-000004 处理      :ord4, 09:30, 1m
    ORD-000005 处理      :ord5, 10:00, 1m
```
