# Inventory Management Simulator - Project Guide

## ⚠️ Important Notice

**This is a project that requires your own implementation!**

- ❌ The project **does not provide complete code**
- ✅ The project **provides architecture design, guidance, and code templates**
- 📚 You need to **implement all functionality** based on the documentation
- 🎯 This is a practical project for learning Spring Boot and message-driven architecture

---

## 📚 Project Overview

Welcome to the **Inventory Management Simulator** project! This is a complete system development project based on Spring Boot and RabbitMQ.

**You will learn through this project:**
- How to design and implement a complete system
- Practical applications of Spring Boot
- Design and implementation of message-driven architecture
- Development of asynchronous systems

### Project Goals

You will learn how to build an **Inventory Management System** with **Message-Driven Architecture** that can:
- Read order data from CSV files
- Control time using a simulation clock system
- Process orders asynchronously through message queues
- Manage inventory (reserve, deduct, auto-replenish)
- Provide REST API query functionality

### Why This Project?

1. **Learn Message-Driven Architecture**: Understand how microservices communicate through message queues for decoupling
2. **Master Spring Boot**: Deep dive into practical applications of Spring Boot
3. **Understand Asynchronous Processing**: Learn how to design and implement asynchronous systems
4. **Real Business Scenarios**: Simulate real-world inventory management workflows
5. **Full-Stack Development Experience**: Complete development process from design to implementation

### Technology Stack

- **Backend Framework**: Spring Boot 3.2.0
- **Message Queue**: RabbitMQ (Spring AMQP)
- **Database**: H2 (In-memory, easy for development) / PostgreSQL (Week 6 Extension, production-ready)
- **ORM**: Spring Data JPA
- **CSV Processing**: OpenCSV
- **Monitoring**: Spring Boot Actuator + Micrometer Prometheus
- **Metrics Collection**: Prometheus
- **Visualization**: Grafana
- **Log Management**: Grafana Loki
- **Build Tool**: Maven
- **Language**: Java 17

### Prerequisites

**Required Knowledge:**
- Java programming basics (OOP, collections, exception handling)
- Spring framework basics (IoC, dependency injection)
- Basic SQL knowledge
- HTTP/REST API basic concepts

**Bonus (Optional):**
- Spring Boot basics
- RabbitMQ basic concepts
- Message queue concepts

**Don't worry if you don't know these!** We'll learn step by step.

---

## 🎯 Learning Objectives

After completing this project, you will be able to:

1. ✅ Set up Spring Boot projects and configure dependencies
2. ✅ Use Spring Data JPA for data persistence
3. ✅ Integrate RabbitMQ for message queue communication
4. ✅ Design and implement message-driven system architecture
5. ✅ Process CSV file data
6. ✅ Implement time simulation systems
7. ✅ Develop REST API interfaces
8. ✅ Integrate monitoring systems (Prometheus + Grafana)
9. ✅ Implement custom business metrics
10. ✅ Write clear project documentation

---

## 📅 8-Week Implementation Plan

### Week 1: Environment Setup and Foundation

**Goal**: Set up project framework and understand system architecture

#### Day 1-2: Project Preparation
- [ ] Read project documentation and architecture design
- [ ] Set up development environment (Java 17, Maven, IDE, Docker)
- [ ] Create Spring Boot project skeleton
- [ ] Configure `pom.xml` dependencies (Spring Boot, RabbitMQ, JPA, H2, Lombok)
- [ ] Configure `application.yml`
- [ ] Run Hello World to confirm environment is working

**Learning Resources:**
- Spring Boot Official Documentation: https://spring.io/projects/spring-boot
- Maven Tutorial: https://maven.apache.org/guides/getting-started/

**Acceptance Criteria:**
- ✅ Project compiles and runs successfully
- ✅ Can access http://localhost:8080
- ✅ Understand project structure

#### Day 3-4: Data Model Design
- [ ] Design `Order` entity class
- [ ] Design `OrderItem` entity class
- [ ] Design `InventoryItem` entity class
- [ ] Create Repository interfaces (OrderRepository, InventoryItemRepository)
- [ ] Configure H2 database connection
- [ ] Test database connection and table creation

**Learning Resources:**
- Spring Data JPA Documentation: https://spring.io/projects/spring-data-jpa
- JPA Annotation Guide: `@Entity`, `@Table`, `@Id`, `@Column`, `@OneToMany`

**Acceptance Criteria:**
- ✅ Entity classes are complete
- ✅ Database tables can be created automatically
- ✅ Can perform basic CRUD operations through Repository

#### Day 5-7: RabbitMQ Integration
- [ ] Install and start RabbitMQ (using Docker)
- [ ] Configure RabbitMQ connection
- [ ] Create Exchange, Queue, Binding
- [ ] Implement simple message sending and receiving
- [ ] Test message queue functionality

**Learning Resources:**
- RabbitMQ Official Tutorial: https://www.rabbitmq.com/getstarted.html
- Spring AMQP Documentation: https://spring.io/projects/spring-amqp

**Acceptance Criteria:**
- ✅ RabbitMQ container runs normally
- ✅ Can send and receive messages
- ✅ Understand concepts of Exchange, Queue, Routing Key

---

### Week 2: Core Functionality Implementation

**Goal**: Implement order processing and inventory management core functionality

#### Day 8-10: Order Manager Implementation
- [ ] Create `OrderReceivedMessage` message class
- [ ] Implement `OrderManager` service class
- [ ] Implement order reception processing logic (`handleOrderReceived`)
- [ ] Implement order status management
- [ ] Test order reception and storage

**Key Code Example:**
```java
@RabbitListener(queues = "sim.order.received")
public void handleOrderReceived(OrderReceivedMessage message) {
    // 1. Create order entity
    // 2. Save to database
    // 3. Check inventory
    // 4. Update order status
}
```

**Acceptance Criteria:**
- ✅ Can receive order messages
- ✅ Order data is correctly saved to database
- ✅ Order status can be updated correctly

#### Day 11-13: Inventory Manager Implementation
- [ ] Create `InventoryUpdateMessage` message class
- [ ] Implement `InventoryManager` service class
- [ ] Implement inventory reservation functionality (RESERVE)
- [ ] Implement inventory deduction functionality (DEDUCT)
- [ ] Implement auto-replenishment functionality
- [ ] Test inventory operations

**Key Features:**
- Reserve inventory (when order is confirmed)
- Deduct inventory (when order is completed)
- Auto-replenish (when inventory falls below threshold)

**Acceptance Criteria:**
- ✅ Inventory reservation works correctly
- ✅ Inventory deduction works correctly
- ✅ Auto-replenishment works correctly
- ✅ Inventory data consistency is correct

#### Day 14: Integration Testing
- [ ] Test integration of Order Manager and Inventory Manager
- [ ] Verify complete order processing workflow
- [ ] Fix discovered issues

---

### Week 3: Order Injection and Simulation Clock

**Goal**: Implement order injector and simulation clock system

#### Day 15-17: CSV Order Reading
- [ ] Understand CSV file format (long format)
- [ ] Add OpenCSV dependency
- [ ] Create `OrderCSVRecord` model class
- [ ] Implement `OrderCSVReader` service class
- [ ] Implement CSV data parsing
- [ ] Prepare sample CSV file

**CSV Format:**
```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-001,2,AMBIENT
```

**Acceptance Criteria:**
- ✅ Can correctly read CSV files
- ✅ Data parsing is correct
- ✅ Supports multiple order items (multiple lines per order)

#### Day 18-19: SimulationClock Implementation
- [ ] Create `SimulationClock` class
- [ ] Implement time initialization (simStartTime, simEndTime)
- [ ] Implement tick functionality (advance time)
- [ ] Implement speed factor (speedFactor)
- [ ] Create `SimulationRunner` scheduled task
- [ ] Test time advancement functionality

**Key Features:**
- Simulation time management
- Time range control
- Speed acceleration support

**Acceptance Criteria:**
- ✅ Simulation clock can advance normally
- ✅ Time range control is correct
- ✅ Speed factor functionality works correctly

#### Day 20-21: Order Injector Implementation
- [ ] Create `OrderInjector` service class
- [ ] Implement CSV order loading (on startup)
- [ ] Implement order filtering (within simulation time range)
- [ ] Implement order injection logic (based on simulation time)
- [ ] Integrate SimulationClock
- [ ] Test order injection functionality

**Key Logic:**
- Load CSV orders on startup
- Filter orders within simulation time range
- Send orders based on current simulation time

**Acceptance Criteria:**
- ✅ Orders can be loaded correctly
- ✅ Orders can be sent correctly based on time
- ✅ Log output is clear

---

### Week 4: Refinement and Optimization

**Goal**: Refine functionality, optimize code, write documentation

**Note:**
- Week 4 is the completion of the basic project
- Week 5 is advanced monitoring features (optional)
- Week 6 is extension features, migrating H2 to PostgreSQL for scalability (optional)

#### Day 22-24: REST API Implementation
- [ ] Create `OrderController`
  - [ ] `GET /api/orders` - Get all orders
  - [ ] `GET /api/orders/{orderId}` - Get specific order
- [ ] Create `InventoryController`
  - [ ] `GET /api/inventory/{sku}` - Get inventory
  - [ ] `POST /api/inventory/initialize` - Initialize inventory
- [ ] Create `HealthController`
  - [ ] `GET /api/health` - Health check
- [ ] Test all API endpoints

**Acceptance Criteria:**
- ✅ All API endpoints work correctly
- ✅ Return data format is correct
- ✅ Error handling is complete

#### Day 25-26: Log Optimization and Testing
- [ ] Optimize log output format
- [ ] Disable unnecessary logs (Hibernate SQL)
- [ ] Add key operation logs
- [ ] Write unit tests (optional)
- [ ] Perform complete functionality testing
- [ ] Fix discovered bugs

**Log Format Example:**
```
[08:00:00] ord-000001 received
[08:00:01] ord-000001 completed successfully
```

**Acceptance Criteria:**
- ✅ Log output is clear and concise
- ✅ All key operations have log records
- ✅ System runs stably

#### Day 27-28: Documentation and Project Summary
- [ ] Update README.md
- [ ] Write project summary report
- [ ] Prepare project demonstration
- [ ] Code review and optimization
- [ ] Final testing

**Documentation Content:**
- Project feature introduction
- System architecture description
- Usage instructions
- Configuration instructions
- API documentation

**Acceptance Criteria:**
- ✅ Documentation is complete and clear
- ✅ Code comments are sufficient
- ✅ Project can run normally
- ✅ Can perform project demonstration

---

### Week 5: Monitoring and Visualization (Advanced)

**Goal**: Integrate monitoring system, implement metrics collection and visualization

**Note**: This is advanced content. Can be skipped if time is tight, but greatly improves project professionalism.

#### Day 29-31: Spring Boot Actuator Integration
- [ ] Add Actuator and Micrometer Prometheus dependencies
- [ ] Configure Actuator endpoints
- [ ] Verify `/actuator/prometheus` endpoint is available

#### Day 32-33: Custom Metrics
- [ ] Add Metrics recording in OrderManager
- [ ] Implement order received, processed, success/failure counting
- [ ] Implement order processing time recording
- [ ] Test Metrics data accuracy

#### Day 34-35: Prometheus + Grafana Deployment
- [ ] Configure Prometheus (create `prometheus.yml`)
- [ ] Update `docker-compose.yml` to add monitoring services
- [ ] Start Prometheus and Grafana
- [ ] Configure Prometheus data source in Grafana

#### Day 36-37: Grafana Dashboard Creation
- [ ] Create order processing Dashboard
- [ ] Add key metrics visualization:
  - Total orders received
  - Orders succeeded/failed
  - Order success rate
  - Average processing time
- [ ] Learn PromQL query language

#### Day 38: Monitoring System Testing and Documentation
- [ ] End-to-end test monitoring system
- [ ] Update documentation (monitoring section)
- [ ] Final project summary

**Learning Resources:**
- Spring Boot Actuator: https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html
- Micrometer: https://micrometer.io/docs
- Prometheus: https://prometheus.io/docs/
- Grafana: https://grafana.com/docs/

**Acceptance Criteria:**
- ✅ Prometheus can collect metrics
- ✅ Grafana Dashboard can display data
- ✅ Key business metrics are visualized
- ✅ Monitoring documentation is complete

---

### Week 6: Extension Features (Optional)

**Goal**: Migrate system from H2 to PostgreSQL, improve system scalability and production readiness

**Note**: This is extension content, suitable for those wanting to learn production environment configuration in depth.

#### Day 39-40: Database Migration to PostgreSQL

**Why Migrate?**

The project initially uses **H2 in-memory database**, suitable for rapid development:
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

**Task List:**
- [ ] Add PostgreSQL dependency to `pom.xml`
- [ ] Add PostgreSQL service to `docker-compose.yml`
- [ ] Update `application.yml` database configuration
- [ ] Configure database connection pool (HikariCP)
- [ ] Test data persistence functionality
- [ ] Verify table structure is created correctly

**Learning Resources:**
- PostgreSQL Official Documentation: https://www.postgresql.org/docs/
- Spring Data JPA Database Configuration: https://spring.io/guides/gs/accessing-data-jpa/

**Acceptance Criteria:**
- ✅ PostgreSQL container runs normally
- ✅ Application can connect to PostgreSQL
- ✅ Data persistence verification passed (data retained after restart)
- ✅ Table structure created correctly

---

## 📖 Weekly Learning Focus

### Week 1 Focus: Foundation
- Spring Boot project structure
- Spring Data JPA usage
- RabbitMQ basic concepts

### Week 2 Focus: Business Logic
- Message-driven architecture design
- Asynchronous processing patterns
- Transaction management

### Week 3 Focus: Advanced Features
- CSV file processing
- Time simulation system
- Scheduled tasks

### Week 4 Focus: Refinement and Optimization
- REST API design
- Log management
- Project documentation

### Week 5 Focus: Monitoring System (Advanced)
- Spring Boot Actuator
- Metrics collection
- Grafana Dashboard

### Week 6 Focus: Extension Features (Optional)
- Database migration (H2 → PostgreSQL)
- Production environment configuration
- Data persistence

### Week 7 Focus: Testing and API Documentation (Optional)
- Unit tests and integration tests
- Swagger/OpenAPI documentation
- Redis cache optimization

### Week 8 Focus: Deployment and Optimization (Optional)
- Docker containerization
- CI/CD pipeline
- Performance testing and optimization

---

## 🛠️ Development Recommendations

### 1. Code Standards
- Use meaningful variable and function names
- Add necessary comments
- Follow Java coding standards
- Use Lombok to simplify code

### 2. Testing Strategy
- Test immediately after implementing each feature
- Use Postman or curl to test APIs
- Check RabbitMQ management interface to confirm messages
- Check database to confirm data correctness

### 3. Debugging Tips
- Use IDE debugging features
- Check log output
- Use H2 console to view data
- Use RabbitMQ management interface to view messages

### 4. What to Do When Encountering Problems?
1. **Read Error Messages**: Most problems have clear error prompts
2. **Check Logs**: Logs usually tell you what happened
3. **Search Documentation**: Spring Boot and RabbitMQ have detailed official documentation
4. **Ask for Help**: Discussion can help solve problems quickly
5. **Check Example Code**: Reference existing code structure

---

## ✅ Project Acceptance Criteria

### Functional Completeness (60%)
- [ ] Orders can be correctly loaded from CSV files
- [ ] Orders can be correctly injected based on simulation time
- [ ] Orders can be correctly processed
- [ ] Inventory management functions work correctly (reserve, deduct, replenish)
- [ ] REST API functions work correctly
- [ ] System can run stably
- [ ] (Advanced) Monitoring system integration completed

### Code Quality (20%)
- [ ] Code structure is clear
- [ ] Code comments are sufficient
- [ ] Follows coding standards
- [ ] No obvious bugs

### Documentation Completeness (15%)
- [ ] README.md is complete
- [ ] Code comments are sufficient
- [ ] Configuration instructions are clear
- [ ] API documentation is complete

### Innovation and Extensions (5%)
- [ ] Additional feature improvements
- [ ] Performance optimizations
- [ ] Code refactoring

---

## 📚 Recommended Learning Resources

### Implementation Guides
- **[IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md)** - **Detailed Implementation Guide** ⭐
  - What each component needs to implement
  - Key logic hints
  - Implementation step suggestions
  - FAQ

- **[CODE_TEMPLATES.md](CODE_TEMPLATES.md)** - **Code Template Reference**
  - Entity class structure hints
  - Service class structure hints
  - Code examples (reference only, need to implement yourself)

### Official Documentation
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **RabbitMQ**: https://www.rabbitmq.com/documentation.html
- **OpenCSV**: http://opencsv.sourceforge.net/

### Tutorials
- Spring Boot Quick Start: https://spring.io/guides/gs/spring-boot/
- RabbitMQ Getting Started: https://www.rabbitmq.com/tutorials/tutorial-one-java.html
- Java Time Processing: https://docs.oracle.com/javase/tutorial/datetime/

---

## 💡 Frequently Asked Questions

### Q1: I don't know Spring Boot, what should I do?
**A**: Don't worry! Week 1 focuses on learning Spring Boot basics. Follow the tutorials step by step, starting with simple Hello World.

### Q2: RabbitMQ is complex, what if I can't learn it?
**A**: This project only uses RabbitMQ's basic features (sending and receiving messages). Understand the basic concepts first, and you'll become more familiar with actual usage.

### Q3: There's too much code, where do I start?
**A**: Follow the time plan day by day. Each day's goals are clear, just complete that day's tasks.

### Q4: How do I solve bugs?
**A**: 
1. Read error messages carefully
2. Check log output
3. Use IDE debugging features
4. Ask for help
5. Search error messages (Stack Overflow)

### Q5: Can I modify project requirements?
**A**: Yes! After completing basic functionality, you can add your own innovative features. This will add points during acceptance.

### Q6: What if one month isn't enough?
**A**: The time plan is ideal progress. If you encounter difficulties, you can:
- Prioritize core functionality
- Simplify non-critical features
- Seek help
- Appropriately extend completion time for some tasks

---

## 🎯 Learning Outcomes

After completing this project, you will gain:

1. **Real Project Experience**: Complete system development experience
2. **Technical Skills Improvement**: Master mainstream technologies like Spring Boot, RabbitMQ, JPA
3. **Architecture Design Ability**: Understand message-driven architecture design
4. **Problem-Solving Ability**: Improve debugging and problem-solving skills through solving various problems
5. **Project Portfolio**: A project that can be showcased in your portfolio

---

## 📝 Project Report Template

After completing the project, it's recommended to write a project report including:

1. **Project Overview**
   - Project background and goals
   - Technology stack selection rationale

2. **System Design**
   - Architecture design
   - Data model design
   - Message flow design

3. **Implementation Process**
   - Development process records
   - Problems encountered and solutions
   - Key code explanations

4. **Feature Demonstration**
   - System running screenshots
   - Feature demonstration video (optional)

5. **Summary and Reflection**
   - What was learned
   - Problems encountered and solutions
   - Future improvement directions

---

## 🚀 Start Your Project Journey

Now you understand the entire project. Let's start Week 1!

**Step 1**: Carefully read project documentation and code
**Step 2**: Set up development environment
**Step 3**: Create project skeleton
**Step 4**: Start coding!

**Remember**: Complete a little bit each day, and you'll have amazing results after a month!

---

## 📞 Getting Help

If you encounter problems in the project:
1. Check project documentation: `docs/` directory
2. Check code comments
3. Ask for help from mentors or colleagues
4. Discuss with team members
5. Search relevant technical documentation

**Good luck with your project! 🎉**
