# Troubleshooting Guide

## Issue: Cannot Connect to localhost:8080

### 1. Check if Application is Running

**Windows:**
```powershell
# Check port usage
netstat -ano | findstr :8080

# Check Java processes
tasklist | findstr java
```

**Linux/Mac:**
```bash
# Check port usage
lsof -i :8080
# or
netstat -tuln | grep 8080

# Check Java processes
ps aux | grep java
```

### 2. Check Application Startup Logs

Check console output for the following errors:

#### Common Error 1: RabbitMQ Connection Failed
```
Caused by: java.net.ConnectException: Connection refused
```

**Solution:**
1. Ensure RabbitMQ container is running:
   ```bash
   docker ps
   ```
2. If not running, start it:
   ```bash
   docker-compose up -d rabbitmq
   ```
3. Wait for RabbitMQ to fully start (may take 30-60 seconds):
   ```bash
   docker logs -f inventory-rabbitmq
   ```
   Wait until you see "Server startup complete" message

#### Common Error 2: Port Already in Use
```
Web server failed to start. Port 8080 was already in use.
```

**Solution:**
1. Find and close the process using the port
2. Or modify port in `application.yml`:
   ```yaml
   server:
     port: 8081  # Change to another port
   ```

#### Common Error 3: Maven Dependency Download Failed
```
Could not resolve dependencies
```

**Solution:**
1. Clean and re-download dependencies:
   ```bash
   mvn clean install -U
   ```
2. Or use IDE's Maven refresh feature

### 3. Verify RabbitMQ Status

```bash
# Check container status
docker ps | grep rabbitmq

# Check RabbitMQ logs
docker logs inventory-rabbitmq

# Test connection
docker exec inventory-rabbitmq rabbitmq-diagnostics ping
```

### 4. Manual Startup Steps

If automatic startup script fails, you can start manually:

1. **Start RabbitMQ:**
   ```bash
   docker-compose up -d rabbitmq
   ```

2. **Wait for RabbitMQ to be Ready:**
   ```bash
   # View logs until you see "Server startup complete"
   docker logs -f inventory-rabbitmq
   ```

3. **Start Application:**
   ```bash
   # Using Maven
   mvn spring-boot:run
   
   # Or run InventorySimulatorApplication using IDE
   ```

### 5. Check Application Health Status

After application starts, access health check endpoint:

```bash
curl http://localhost:8080/api/health
```

Should return:
```json
{
  "status": "UP",
  "application": "Inventory Simulator",
  "rabbitmq": "CONNECTED"
}
```

If `rabbitmq` shows `DISCONNECTED`, there's a RabbitMQ connection issue.

### 6. Test RabbitMQ Connection

```bash
# Access RabbitMQ Management Interface
# Open in browser: http://localhost:15672
# Username: guest
# Password: guest

# If cannot access, check if container is running
docker ps
```

### 7. Common Issue Resolution

#### Issue: Application Starts but Exits Immediately

**Possible Causes:**
- RabbitMQ connection failure causing startup failure
- Configuration error

**Solution:**
1. Check complete error message in startup logs
2. Ensure RabbitMQ is fully ready before application starts
3. Check if `application.yml` configuration is correct

#### Issue: Application Starts Successfully but Cannot Access API

**Possible Causes:**
- Firewall blocking
- Application bound to wrong address

**Solution:**
1. Check application logs to confirm startup port
2. Try accessing: `http://127.0.0.1:8080/api/health`
3. Check firewall settings

#### Issue: RabbitMQ Container Cannot Start

**Possible Causes:**
- Port already in use
- Insufficient Docker resources

**Solution:**
1. Check port usage:
   ```bash
   # Windows
   netstat -ano | findstr :5672
   netstat -ano | findstr :15672
   
   # Linux/Mac
   lsof -i :5672
   lsof -i :15672
   ```

2. Check Docker resources:
   ```bash
   docker system df
   docker stats
   ```

3. Restart Docker Desktop

### 8. Debug Mode Startup

If you need more detailed logs, set log level:

Add to `application.yml`:
```yaml
logging:
  level:
    root: INFO
    com.inventory: DEBUG
    org.springframework.amqp: DEBUG
    org.springframework.boot: DEBUG
```

### 9. Reset Environment

If issues persist, try complete reset:

```bash
# Stop and remove containers
docker-compose down -v

# Clean Maven build
mvn clean

# Restart
docker-compose up -d rabbitmq
# Wait 30 seconds
mvn spring-boot:run
```

### 10. Get Help

If the above steps cannot resolve the issue, please provide the following information:

1. Complete startup logs
2. `docker ps` output
3. `docker logs inventory-rabbitmq` output
4. `curl http://localhost:8080/api/health` output
5. Operating system and Java version
