# Environment Setup Guide

## Prerequisites

1. **Java 17+**
   - Download: https://www.oracle.com/java/technologies/downloads/
   - Or use OpenJDK: https://adoptium.net/

2. **Maven 3.6+** (Optional, not required if using IDE)
   - Download: https://maven.apache.org/download.cgi
   - Add to system PATH environment variable after installation

3. **Docker Desktop** (for running RabbitMQ)
   - Download: https://www.docker.com/products/docker-desktop

## Installation Steps

### Method 1: Using Maven (Recommended)

1. **Install Maven**
   - Download and extract Maven
   - Add to system PATH environment variable
   - Verify installation: `mvn -version`

2. **Generate Maven Wrapper** (Optional, but recommended)
   ```bash
   mvn wrapper:wrapper
   ```
   This will generate `mvnw` (Linux/Mac) or `mvnw.cmd` (Windows) files

3. **Run Project**
   ```bash
   # Windows
   start.bat
   
   # Linux/Mac
   ./start.sh
   ```

### Method 2: Using IDE (No Maven Required)

1. **IntelliJ IDEA**
   - Open project folder
   - Wait for Maven dependencies to download automatically
   - Find `InventorySimulatorApplication.java`
   - Right-click → Run 'InventorySimulatorApplication'

2. **Eclipse**
   - File → Import → Existing Maven Projects
   - Select project folder
   - Right-click project → Run As → Spring Boot App

3. **VS Code**
   - Install Java Extension Pack
   - Open project folder
   - Run main class: `com.inventory.InventorySimulatorApplication`

### Method 3: Using Maven Wrapper (If Generated)

If Maven Wrapper has been generated, you can use it directly:

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

## Verification

### Check Java
```bash
java -version
```
Should display Java 17 or higher

### Check Maven (If Using)
```bash
mvn -version
```

### Check Docker
```bash
docker --version
docker-compose --version
```

## Common Issues

### 1. Maven Command Not Found

**Solution:**
- Ensure Maven is installed and added to PATH
- Or use IDE to run project (Maven not required)
- Or generate Maven Wrapper: `mvn wrapper:wrapper`

### 2. Docker Not Running

**Solution:**
- Start Docker Desktop
- Wait for Docker to fully start before running scripts

### 3. Port Already in Use

**Solution:**
- Check if ports 5672 (RabbitMQ) and 8080 (application) are in use
- Modify port configuration in `application.yml`

### 4. RabbitMQ Connection Failed

**Solution:**
- Ensure Docker container is running: `docker ps`
- Check RabbitMQ logs: `docker logs inventory-rabbitmq`
- Wait longer for RabbitMQ to fully start

## Quick Start (Using IDE)

If you don't want to install Maven, the simplest way:

1. Open project with IntelliJ IDEA or Eclipse
2. Wait for dependencies to download
3. Run `InventorySimulatorApplication` main class
4. Ensure RabbitMQ is started in Docker

## Next Steps

After successful startup, access:
- Application: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console
- RabbitMQ Management: http://localhost:15672 (guest/guest)
