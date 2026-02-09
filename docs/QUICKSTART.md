# Quick Start Guide

## 🚀 5-Minute Quick Start

### Step 1: Environment Check

```bash
# Check Java version (requires 17+)
java -version

# Check Maven (if using)
mvn -version

# Check Docker (for RabbitMQ)
docker --version
```

### Step 2: Clone/Download Project

```bash
# If you have Git
git clone <repository-url>
cd inventory

# Or directly download project files
```

### Step 3: Start RabbitMQ

```bash
# Using Docker Compose (recommended)
docker-compose up -d

# Or using Docker command
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### Step 4: Run Project

**Method 1: Using IDE (Recommended)**
1. Open project with IntelliJ IDEA or Eclipse
2. Wait for dependencies to download
3. Run `InventorySimulatorApplication` main class

**Method 2: Using Maven**
```bash
mvn spring-boot:run
```

### Step 5: Verify Running

1. **Check Logs**: Should see simulation clock initialization information
2. **Access Health Check**: http://localhost:8080/api/health
3. **View Orders**: http://localhost:8080/api/orders
4. **RabbitMQ Management Interface**: http://localhost:15672 (guest/guest)

### Step 6: Start Monitoring System (Optional, Week 5 Content)

```bash
# Start Prometheus + Grafana
docker-compose up -d prometheus grafana
```

Then access:
- **Prometheus UI**: http://localhost:9090
- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Metrics Endpoint**: http://localhost:8080/actuator/prometheus

Configure Prometheus data source in Grafana (URL: `http://prometheus:9090`)

---

## 📚 Learning Path

### Beginner Path (If You're Not Familiar with Spring Boot)

1. **Day 1**: Just run the project and see the results
2. **Day 2**: Understand project structure
3. **Day 3**: Read code and understand what each class does
4. **Day 4 onwards**: Start implementing according to WEEKLY_PLAN.md

### Experienced Path (If You're Familiar with Spring Boot)

Start directly according to the 8-week plan in PROJECT_GUIDE.md

---

## 🎯 Week 1 Must-Complete Tasks

### Must Complete (Otherwise Cannot Continue)

1. ✅ **Environment Setup** (Day 1-2)
   - Java 17 installation
   - IDE configuration
   - Docker installation
   - Project can run

2. ✅ **Data Model** (Day 3-4)
   - Order entity class
   - InventoryItem entity class
   - Repository interfaces
   - Database connection works

3. ✅ **RabbitMQ Basics** (Day 5-7)
   - RabbitMQ running
   - Can send messages
   - Can receive messages

**If these aren't completed, Week 2 will be very difficult!**

---

## 🔧 Quick Problem Resolution

### Problem 1: Wrong Java Version
```
Error: Requires Java 17, but current is Java 11
Solution: Install Java 17, configure JAVA_HOME
```

### Problem 2: Maven Dependency Download Failed
```
Solution: 
1. Check network connection
2. Configure Maven mirror (use domestic mirror)
3. Or use IDE's Maven settings
```

### Problem 3: RabbitMQ Connection Failed
```
Solution:
1. Confirm Docker is running
2. Confirm RabbitMQ container is running: docker ps
3. Wait for RabbitMQ to fully start (30-60 seconds)
```

### Problem 4: Port Already in Use
```
Solution:
1. Find and close the process using the port
2. Or modify port in application.yml
```

---

## 📖 Recommended Learning Order

### Week 1: Foundation (Must Master)

1. **Spring Boot Basics**
   - What is Spring Boot?
   - How to create Spring Boot project?
   - How to use configuration files?

2. **Spring Data JPA**
   - How to define entity classes?
   - How to use Repository?
   - How to configure database?

3. **RabbitMQ Basics**
   - What is message queue?
   - What are Exchange and Queue?
   - How to send and receive messages?

### Week 2: Application (Hands-on Practice)

1. **Implement Order Manager**
   - Understand business logic
   - Implement message listening
   - Implement order processing

2. **Implement Inventory Manager**
   - Implement inventory operations
   - Understand transaction management
   - Implement auto-replenishment

### Week 3: Enhancement (Deep Learning)

1. **CSV Processing**
   - File reading
   - Data parsing
   - Data transformation

2. **Time Simulation**
   - Time management
   - Scheduled tasks
   - System design

### Week 4: Refinement (Project Completion)

1. **REST API**
   - API design
   - Error handling
   - Testing

2. **Documentation and Summary**
   - Code comments
   - Documentation writing
   - Project summary

### Week 5: Monitoring System (Advanced, Optional)

1. **Spring Boot Actuator**
   - Add dependencies
   - Configure endpoints
   - Expose metrics

2. **Custom Metrics**
   - Record order processing metrics
   - Record processing time
   - Record success/failure counts

3. **Prometheus + Grafana**
   - Deploy monitoring services
   - Configure data source
   - Create Dashboard

---

## 🎯 Learning Recommendations

### 1. Don't Rush
- Complete that day's tasks each day
- Understanding is more important than completion
- Think more when encountering problems

### 2. Practice More
- Writing code is better than reading code
- Implement each feature yourself
- Test and debug more

### 3. Communicate More
- Discuss with team members
- Ask mentors questions
- Share learning insights

### 4. Record Problems
- Record problems encountered
- Record solutions
- Build your own knowledge base

---

## ✅ Week 1 Checklist

After completing Week 1, you should be able to:

- [ ] Project can run successfully
- [ ] Understand project structure
- [ ] Understand data model design
- [ ] RabbitMQ can be used normally
- [ ] Can send and receive messages
- [ ] Database operations work normally

**If all these are completed, congratulations! You can start Week 2! 🎉**

## ✅ Week 5 Checklist (Advanced)

After completing Week 5, you should be able to:

- [ ] Spring Boot Actuator configuration completed
- [ ] Custom Metrics implementation completed
- [ ] Prometheus running normally and collecting metrics
- [ ] Grafana Dashboard creation completed
- [ ] Can visualize order processing metrics
- [ ] Understand basic PromQL queries

**If all these are completed, congratulations! Your project is very professional! 🚀**

---

## 📞 Need Help?

1. **Check Documentation**
   - README.md
   - PROJECT_GUIDE.md
   - WEEKLY_PLAN.md

2. **Check Code**
   - Code comments
   - Existing implementation

3. **Ask for Help**
   - Mentors
   - Teaching assistants
   - Team members

**Remember: Everyone encounters problems, the key is to solve them!**
