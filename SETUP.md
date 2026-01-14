# 环境设置指南

## 前置要求

1. **Java 17+**
   - 下载地址: https://www.oracle.com/java/technologies/downloads/
   - 或使用 OpenJDK: https://adoptium.net/

2. **Maven 3.6+** (可选，如果使用 IDE 可以不需要)
   - 下载地址: https://maven.apache.org/download.cgi
   - 安装后添加到系统 PATH 环境变量

3. **Docker Desktop** (用于运行 RabbitMQ)
   - 下载地址: https://www.docker.com/products/docker-desktop

## 安装步骤

### 方式一：使用 Maven (推荐)

1. **安装 Maven**
   - 下载 Maven 并解压
   - 添加到系统 PATH 环境变量
   - 验证安装: `mvn -version`

2. **生成 Maven Wrapper** (可选，但推荐)
   ```bash
   mvn wrapper:wrapper
   ```
   这将生成 `mvnw` (Linux/Mac) 或 `mvnw.cmd` (Windows) 文件

3. **运行项目**
   ```bash
   # Windows
   start.bat
   
   # Linux/Mac
   ./start.sh
   ```

### 方式二：使用 IDE (无需安装 Maven)

1. **IntelliJ IDEA**
   - 打开项目文件夹
   - 等待 Maven 依赖自动下载
   - 找到 `InventorySimulatorApplication.java`
   - 右键 → Run 'InventorySimulatorApplication'

2. **Eclipse**
   - File → Import → Existing Maven Projects
   - 选择项目文件夹
   - 右键项目 → Run As → Spring Boot App

3. **VS Code**
   - 安装 Java Extension Pack
   - 打开项目文件夹
   - 运行主类: `com.inventory.InventorySimulatorApplication`

### 方式三：使用 Maven Wrapper (如果已生成)

如果已生成 Maven Wrapper，可以直接使用：

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

## 验证安装

### 检查 Java
```bash
java -version
```
应该显示 Java 17 或更高版本

### 检查 Maven (如果使用)
```bash
mvn -version
```

### 检查 Docker
```bash
docker --version
docker-compose --version
```

## 常见问题

### 1. Maven 命令未找到

**解决方案：**
- 确保 Maven 已安装并添加到 PATH
- 或使用 IDE 运行项目（不需要 Maven）
- 或生成 Maven Wrapper: `mvn wrapper:wrapper`

### 2. Docker 未运行

**解决方案：**
- 启动 Docker Desktop
- 等待 Docker 完全启动后再运行脚本

### 3. 端口被占用

**解决方案：**
- 检查端口 5672 (RabbitMQ) 和 8080 (应用) 是否被占用
- 修改 `application.yml` 中的端口配置

### 4. RabbitMQ 连接失败

**解决方案：**
- 确保 Docker 容器正在运行: `docker ps`
- 检查 RabbitMQ 日志: `docker logs inventory-rabbitmq`
- 等待更长时间让 RabbitMQ 完全启动

## 快速启动（使用 IDE）

如果不想安装 Maven，最简单的方式：

1. 使用 IntelliJ IDEA 或 Eclipse 打开项目
2. 等待依赖下载完成
3. 运行 `InventorySimulatorApplication` 主类
4. 确保 Docker 中 RabbitMQ 已启动

## 下一步

启动成功后，访问：
- 应用: http://localhost:8080
- H2 控制台: http://localhost:8080/h2-console
- RabbitMQ 管理界面: http://localhost:15672 (guest/guest)
