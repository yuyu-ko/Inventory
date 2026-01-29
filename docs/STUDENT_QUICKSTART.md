# 学生快速开始指南

## 🚀 5分钟快速上手

### 第一步：环境检查

```bash
# 检查 Java 版本（需要 17+）
java -version

# 检查 Maven（如果使用）
mvn -version

# 检查 Docker（用于 RabbitMQ）
docker --version
```

### 第二步：克隆/下载项目

```bash
# 如果有 Git
git clone <repository-url>
cd inventory

# 或者直接下载项目文件
```

### 第三步：启动 RabbitMQ

```bash
# 使用 Docker Compose（推荐）
docker-compose up -d

# 或使用 Docker 命令
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

### 第四步：运行项目

**方式 1：使用 IDE（推荐）**
1. 用 IntelliJ IDEA 或 Eclipse 打开项目
2. 等待依赖下载完成
3. 运行 `InventorySimulatorApplication` 主类

**方式 2：使用 Maven**
```bash
mvn spring-boot:run
```

### 第五步：验证运行

1. **查看日志**：应该看到模拟时钟初始化信息
2. **访问健康检查**：http://localhost:8080/api/health
3. **查看订单**：http://localhost:8080/api/orders
4. **RabbitMQ 管理界面**：http://localhost:15672 (guest/guest)

### 第六步：启动监控系统（可选，Week 5 内容）

```bash
# 启动 Prometheus + Grafana
docker-compose up -d prometheus grafana
```

然后访问：
- **Prometheus UI**: http://localhost:9090
- **Grafana Dashboard**: http://localhost:3000 (admin/admin)
- **Metrics 端点**: http://localhost:8080/actuator/prometheus

在 Grafana 中配置 Prometheus 数据源（URL: `http://prometheus:9090`）

---

## 📚 学习路径

### 新手路径（如果你不熟悉 Spring Boot）

1. **第一天**：只运行项目，看看效果
2. **第二天**：理解项目结构
3. **第三天**：阅读代码，理解每个类的作用
4. **第四天开始**：按照 WEEKLY_PLAN.md 开始实现

### 有经验路径（如果你熟悉 Spring Boot）

直接按照 STUDENT_GUIDE.md 中的四周计划开始

---

## 🎯 第一周必须完成的任务

### 必须完成（否则无法继续）

1. ✅ **环境搭建**（Day 1-2）
   - Java 17 安装
   - IDE 配置
   - Docker 安装
   - 项目可以运行

2. ✅ **数据模型**（Day 3-4）
   - Order 实体类
   - InventoryItem 实体类
   - Repository 接口
   - 数据库连接正常

3. ✅ **RabbitMQ 基础**（Day 5-7）
   - RabbitMQ 运行
   - 能够发送消息
   - 能够接收消息

**如果这些没完成，第二周会非常困难！**

---

## 🔧 常见问题快速解决

### 问题 1: Java 版本不对
```
错误: 需要 Java 17，但当前是 Java 11
解决: 安装 Java 17，配置 JAVA_HOME
```

### 问题 2: Maven 依赖下载失败
```
解决: 
1. 检查网络连接
2. 配置 Maven 镜像（使用国内镜像）
3. 或者使用 IDE 的 Maven 设置
```

### 问题 3: RabbitMQ 连接失败
```
解决:
1. 确认 Docker 运行中
2. 确认 RabbitMQ 容器运行: docker ps
3. 等待 RabbitMQ 完全启动（30-60秒）
```

### 问题 4: 端口被占用
```
解决:
1. 找到占用进程并关闭
2. 或修改 application.yml 中的端口
```

---

## 📖 推荐的学习顺序

### 第一周：基础（必须掌握）

1. **Spring Boot 基础**
   - 什么是 Spring Boot？
   - 如何创建 Spring Boot 项目？
   - 配置文件如何使用？

2. **Spring Data JPA**
   - 实体类如何定义？
   - Repository 如何使用？
   - 数据库如何配置？

3. **RabbitMQ 基础**
   - 什么是消息队列？
   - Exchange、Queue 是什么？
   - 如何发送和接收消息？

### 第二周：应用（动手实践）

1. **实现 Order Manager**
   - 理解业务逻辑
   - 实现消息监听
   - 实现订单处理

2. **实现 Inventory Manager**
   - 实现库存操作
   - 理解事务管理
   - 实现自动补货

### 第三周：提升（深入学习）

1. **CSV 处理**
   - 文件读取
   - 数据解析
   - 数据转换

2. **时间模拟**
   - 时间管理
   - 定时任务
   - 系统设计

### 第四周：完善（项目收尾）

1. **REST API**
   - API 设计
   - 错误处理
   - 测试

2. **文档与总结**
   - 代码注释
   - 文档编写
   - 项目总结

### 第五周：监控系统（进阶，可选）

1. **Spring Boot Actuator**
   - 添加依赖
   - 配置 endpoints
   - 暴露 metrics

2. **自定义 Metrics**
   - 记录订单处理指标
   - 记录处理时间
   - 记录成功/失败数

3. **Prometheus + Grafana**
   - 部署监控服务
   - 配置数据源
   - 创建 Dashboard

---

## 🎓 学习建议

### 1. 不要急于求成
- 每天完成当天的任务即可
- 理解比完成更重要
- 遇到问题多思考

### 2. 多实践
- 看代码不如写代码
- 每个功能都要自己实现
- 多测试，多调试

### 3. 多交流
- 与同学讨论
- 向老师提问
- 分享学习心得

### 4. 记录问题
- 遇到的问题记录下来
- 解决方案记录下来
- 形成自己的知识库

---

## ✅ 第一周检查清单

完成第一周后，你应该能够：

- [ ] 项目可以成功运行
- [ ] 理解项目结构
- [ ] 理解数据模型设计
- [ ] RabbitMQ 能够正常使用
- [ ] 能够发送和接收消息
- [ ] 数据库操作正常

**如果这些都完成了，恭喜你！你可以开始第二周了！🎉**

## ✅ 第五周检查清单（进阶）

完成第五周后，你应该能够：

- [ ] Spring Boot Actuator 配置完成
- [ ] 自定义 Metrics 实现完成
- [ ] Prometheus 正常运行并收集指标
- [ ] Grafana Dashboard 创建完成
- [ ] 能够可视化订单处理指标
- [ ] 理解 PromQL 基本查询

**如果这些都完成了，恭喜你！你的项目已经非常专业了！🚀**

---

## 📞 需要帮助？

1. **查看文档**
   - README.md
   - STUDENT_GUIDE.md
   - WEEKLY_PLAN.md

2. **查看代码**
   - 代码注释
   - 现有实现

3. **询问帮助**
   - 老师
   - 助教
   - 同学

**记住：每个人都会遇到问题，关键是要解决它！**
