# 库存管理模拟器 - 学生项目指南

## ⚠️ 重要说明

**这是一个需要你自己实现的项目！**

- ❌ 项目**不会提供完整代码**
- ✅ 项目**会提供架构设计、方向指导和代码模板**
- 📚 你需要根据文档**自己实现所有功能**
- 🎓 这是学习 Spring Boot 和消息驱动架构的实践项目

---

## 📚 项目简介

欢迎来到**库存管理模拟器**项目！这是一个基于 Spring Boot 和 RabbitMQ 的完整系统开发项目。

**你将通过这个项目学习：**
- 如何设计和实现一个完整的系统
- Spring Boot 的实际应用
- 消息驱动架构的设计和实现
- 异步系统的开发

### 项目目标

你将学习如何构建一个**消息驱动架构（Message-Driven Architecture）**的库存管理系统，该系统能够：
- 从 CSV 文件读取订单数据
- 使用模拟时钟系统控制时间
- 通过消息队列异步处理订单
- 管理库存（预留、扣除、自动补货）
- 提供 REST API 查询功能

### 为什么要做这个项目？

1. **学习消息驱动架构**：了解微服务间如何通过消息队列解耦通信
2. **掌握 Spring Boot**：深入学习 Spring Boot 的实际应用
3. **理解异步处理**：学习如何设计和实现异步系统
4. **实际业务场景**：模拟真实世界的库存管理流程
5. **全栈开发经验**：从设计到实现的完整开发流程

### 技术栈

- **后端框架**: Spring Boot 3.2.0
- **消息队列**: RabbitMQ (Spring AMQP)
- **数据库**: H2 (内存数据库，易于开发)
- **ORM**: Spring Data JPA
- **CSV 处理**: OpenCSV
- **构建工具**: Maven
- **语言**: Java 17

### 前置知识要求

**必需知识：**
- Java 编程基础（面向对象、集合、异常处理）
- Spring 框架基础（IoC、依赖注入）
- 基本的 SQL 知识
- HTTP/REST API 基础概念

**加分项（可选）：**
- Spring Boot 基础
- RabbitMQ 基本概念
- 消息队列概念

**如果不会也没关系！** 我们会一步步学习。

---

## 🎯 学习目标

完成这个项目后，你将能够：

1. ✅ 搭建 Spring Boot 项目并配置相关依赖
2. ✅ 使用 Spring Data JPA 进行数据持久化
3. ✅ 集成 RabbitMQ 实现消息队列通信
4. ✅ 设计并实现消息驱动的系统架构
5. ✅ 处理 CSV 文件数据
6. ✅ 实现时间模拟系统
7. ✅ 开发 REST API 接口
8. ✅ 编写清晰的项目文档

---

## 📅 一个月完成计划

### 第一周：环境搭建与基础架构（Week 1）

**目标**：搭建项目框架，理解系统架构

#### Day 1-2: 项目准备
- [ ] 阅读项目文档和架构设计
- [ ] 搭建开发环境（Java 17, Maven, IDE, Docker）
- [ ] 创建 Spring Boot 项目骨架
- [ ] 配置 `pom.xml` 依赖（Spring Boot, RabbitMQ, JPA, H2, Lombok）
- [ ] 配置 `application.yml`
- [ ] 运行 Hello World，确认环境正常

**学习资源**：
- Spring Boot 官方文档：https://spring.io/projects/spring-boot
- Maven 教程：https://maven.apache.org/guides/getting-started/

**验收标准**：
- ✅ 项目可以成功编译运行
- ✅ 能够访问 http://localhost:8080
- ✅ 理解项目结构

#### Day 3-4: 数据模型设计
- [ ] 设计 `Order` 实体类（订单）
- [ ] 设计 `OrderItem` 实体类（订单项）
- [ ] 设计 `InventoryItem` 实体类（库存项）
- [ ] 创建 Repository 接口（OrderRepository, InventoryItemRepository）
- [ ] 配置 H2 数据库连接
- [ ] 测试数据库连接和表创建

**学习资源**：
- Spring Data JPA 文档：https://spring.io/projects/spring-data-jpa
- JPA 注解说明：`@Entity`, `@Table`, `@Id`, `@Column`, `@OneToMany`

**验收标准**：
- ✅ 实体类定义完整
- ✅ 数据库表能够自动创建
- ✅ 能够通过 Repository 进行基本的 CRUD 操作

#### Day 5-7: RabbitMQ 集成
- [ ] 安装并启动 RabbitMQ（使用 Docker）
- [ ] 配置 RabbitMQ 连接
- [ ] 创建 Exchange、Queue、Binding
- [ ] 实现简单的消息发送和接收
- [ ] 测试消息队列功能

**学习资源**：
- RabbitMQ 官方教程：https://www.rabbitmq.com/getstarted.html
- Spring AMQP 文档：https://spring.io/projects/spring-amqp

**验收标准**：
- ✅ RabbitMQ 容器正常运行
- ✅ 能够发送和接收消息
- ✅ 理解 Exchange、Queue、Routing Key 的概念

---

### 第二周：核心功能实现（Week 2）

**目标**：实现订单处理和库存管理核心功能

#### Day 8-10: Order Manager 实现
- [ ] 创建 `OrderReceivedMessage` 消息类
- [ ] 实现 `OrderManager` 服务类
- [ ] 实现订单接收处理逻辑（`handleOrderReceived`）
- [ ] 实现订单状态管理
- [ ] 测试订单接收和存储

**关键代码示例**：
```java
@RabbitListener(queues = "sim.order.received")
public void handleOrderReceived(OrderReceivedMessage message) {
    // 1. 创建订单实体
    // 2. 保存到数据库
    // 3. 检查库存
    // 4. 更新订单状态
}
```

**验收标准**：
- ✅ 能够接收订单消息
- ✅ 订单数据正确保存到数据库
- ✅ 订单状态能够正确更新

#### Day 11-13: Inventory Manager 实现
- [ ] 创建 `InventoryUpdateMessage` 消息类
- [ ] 实现 `InventoryManager` 服务类
- [ ] 实现库存预留功能（RESERVE）
- [ ] 实现库存扣除功能（DEDUCT）
- [ ] 实现自动补货功能
- [ ] 测试库存操作

**关键功能**：
- 预留库存（订单确认时）
- 扣除库存（订单完成时）
- 自动补货（库存低于阈值时）

**验收标准**：
- ✅ 库存预留功能正常
- ✅ 库存扣除功能正常
- ✅ 自动补货功能正常
- ✅ 库存数据一致性正确

#### Day 14: 集成测试
- [ ] 测试 Order Manager 和 Inventory Manager 的集成
- [ ] 验证完整的订单处理流程
- [ ] 修复发现的问题

---

### 第三周：订单注入与模拟时钟（Week 3）

**目标**：实现订单注入器和模拟时钟系统

#### Day 15-17: CSV 订单读取
- [ ] 了解 CSV 文件格式（长格式）
- [ ] 添加 OpenCSV 依赖
- [ ] 创建 `OrderCSVRecord` 模型类
- [ ] 实现 `OrderCSVReader` 服务类
- [ ] 实现 CSV 数据解析
- [ ] 准备示例 CSV 文件

**CSV 格式**：
```csv
ORDER_ID,ORDER_TYPE,ORDER_PLACED_TIME,ORDER_DUE_TIME,CUSTOMER_ID,SKU,QUANTITY,TEMPERATURE_ZONE
ORD-000001,PICKUP,2024-01-13T08:00:00,2024-01-13T12:00:00,CUST-001,SKU-001,2,AMBIENT
```

**验收标准**：
- ✅ 能够正确读取 CSV 文件
- ✅ 数据解析正确
- ✅ 支持多个订单项（同一订单多行）

#### Day 18-19: SimulationClock 实现
- [ ] 创建 `SimulationClock` 类
- [ ] 实现时间初始化（simStartTime, simEndTime）
- [ ] 实现 tick 功能（推进时间）
- [ ] 实现速度因子（speedFactor）
- [ ] 创建 `SimulationRunner` 定时任务
- [ ] 测试时间推进功能

**关键功能**：
- 模拟时间管理
- 时间范围控制
- 加速运行支持

**验收标准**：
- ✅ 模拟时钟能够正常推进
- ✅ 时间范围控制正确
- ✅ 加速因子功能正常

#### Day 20-21: Order Injector 实现
- [ ] 创建 `OrderInjector` 服务类
- [ ] 实现 CSV 订单加载（启动时）
- [ ] 实现订单过滤（模拟时间范围内）
- [ ] 实现订单注入逻辑（根据模拟时间）
- [ ] 集成 SimulationClock
- [ ] 测试订单注入功能

**关键逻辑**：
- 启动时加载 CSV 订单
- 过滤模拟时间范围内的订单
- 根据当前模拟时间发送订单

**验收标准**：
- ✅ 订单能够正确加载
- ✅ 订单能够根据时间正确发送
- ✅ 日志输出清晰

---

### 第四周：完善与优化（Week 4）

**目标**：完善功能，优化代码，编写文档

#### Day 22-24: REST API 实现
- [ ] 创建 `OrderController`
  - [ ] `GET /api/orders` - 获取所有订单
  - [ ] `GET /api/orders/{orderId}` - 获取指定订单
- [ ] 创建 `InventoryController`
  - [ ] `GET /api/inventory/{sku}` - 获取库存
  - [ ] `POST /api/inventory/initialize` - 初始化库存
- [ ] 创建 `HealthController`
  - [ ] `GET /api/health` - 健康检查
- [ ] 测试所有 API 端点

**验收标准**：
- ✅ 所有 API 端点正常工作
- ✅ 返回数据格式正确
- ✅ 错误处理完善

#### Day 25-26: 日志优化与测试
- [ ] 优化日志输出格式
- [ ] 关闭不必要的日志（Hibernate SQL）
- [ ] 添加关键操作日志
- [ ] 编写单元测试（可选）
- [ ] 进行完整的功能测试
- [ ] 修复发现的 Bug

**日志格式示例**：
```
[08:00:00] ord-000001 received
[08:00:01] ord-000001 completed successfully
```

**验收标准**：
- ✅ 日志输出清晰简洁
- ✅ 关键操作都有日志记录
- ✅ 系统运行稳定

#### Day 27-28: 文档编写与项目总结
- [ ] 更新 README.md
- [ ] 编写项目总结报告
- [ ] 准备项目演示
- [ ] 代码审查和优化
- [ ] 最终测试

**文档内容**：
- 项目功能介绍
- 系统架构说明
- 使用说明
- 配置说明
- API 文档

**验收标准**：
- ✅ 文档完整清晰
- ✅ 代码注释完善
- ✅ 项目可以正常运行
- ✅ 能够进行项目演示

---

## 📖 每周学习重点

### Week 1 重点：基础架构
- Spring Boot 项目结构
- Spring Data JPA 使用
- RabbitMQ 基本概念

### Week 2 重点：业务逻辑
- 消息驱动架构设计
- 异步处理模式
- 事务管理

### Week 3 重点：高级功能
- CSV 文件处理
- 时间模拟系统
- 定时任务

### Week 4 重点：完善优化
- REST API 设计
- 日志管理
- 项目文档

---

## 🛠️ 开发建议

### 1. 代码规范
- 使用有意义的变量名和函数名
- 添加必要的注释
- 遵循 Java 编码规范
- 使用 Lombok 简化代码

### 2. 测试策略
- 每个功能实现后立即测试
- 使用 Postman 或 curl 测试 API
- 查看 RabbitMQ 管理界面确认消息
- 查看数据库确认数据正确性

### 3. 调试技巧
- 使用 IDE 的调试功能
- 查看日志输出
- 使用 H2 控制台查看数据
- 使用 RabbitMQ 管理界面查看消息

### 4. 遇到问题怎么办？
1. **阅读错误信息**：大多数问题都有明确的错误提示
2. **查看日志**：日志通常会告诉你发生了什么
3. **搜索文档**：Spring Boot 和 RabbitMQ 都有详细的官方文档
4. **询问老师/同学**：讨论可以帮助快速解决问题
5. **查看示例代码**：参考已有的代码结构

---

## ✅ 项目验收标准

### 功能完整性（60%）
- [ ] 订单能够从 CSV 文件正确加载
- [ ] 订单能够根据模拟时间正确注入
- [ ] 订单能够被正确处理
- [ ] 库存管理功能正常（预留、扣除、补货）
- [ ] REST API 功能正常
- [ ] 系统能够稳定运行

### 代码质量（20%）
- [ ] 代码结构清晰
- [ ] 代码注释充分
- [ ] 遵循编码规范
- [ ] 没有明显的 Bug

### 文档完整性（15%）
- [ ] README.md 完整
- [ ] 代码注释充分
- [ ] 配置说明清晰
- [ ] API 文档完整

### 创新与扩展（5%）
- [ ] 有额外的功能改进
- [ ] 有性能优化
- [ ] 有代码重构

---

## 📚 推荐学习资源

### 实现指南
- **[IMPLEMENTATION_GUIDE.md](IMPLEMENTATION_GUIDE.md)** - **详细实现指南** ⭐
  - 每个组件需要实现什么
  - 关键逻辑提示
  - 实现步骤建议
  - 常见问题解答

- **[CODE_TEMPLATES.md](CODE_TEMPLATES.md)** - **代码模板参考**
  - 实体类结构提示
  - Service 类结构提示
  - 代码示例（仅参考，需自己实现）

### 官方文档
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Spring Data JPA**: https://spring.io/projects/spring-data-jpa
- **RabbitMQ**: https://www.rabbitmq.com/documentation.html
- **OpenCSV**: http://opencsv.sourceforge.net/

### 教程
- Spring Boot 快速入门：https://spring.io/guides/gs/spring-boot/
- RabbitMQ 入门教程：https://www.rabbitmq.com/tutorials/tutorial-one-java.html
- Java 时间处理：https://docs.oracle.com/javase/tutorial/datetime/

### 视频资源
- Spring Boot 实战视频（B站/YouTube）
- RabbitMQ 教程视频
- Java 并发编程（了解异步处理）

---

## 💡 常见问题 FAQ

### Q1: 我不会 Spring Boot，怎么办？
**A**: 不用担心！第一周的重点就是学习 Spring Boot 基础。跟着教程一步步来，从简单的 Hello World 开始。

### Q2: RabbitMQ 很复杂，学不会怎么办？
**A**: 这个项目只使用了 RabbitMQ 的基本功能（发送和接收消息）。先理解基本概念，实际使用时会越来越熟悉。

### Q3: 代码太多，不知道从哪里开始？
**A**: 按照时间计划，一天一天完成。每天的目标都很明确，完成当天的任务即可。

### Q4: 遇到 Bug 怎么解决？
**A**: 
1. 仔细阅读错误信息
2. 查看日志输出
3. 使用 IDE 调试功能
4. 询问老师或同学
5. 搜索错误信息（Stack Overflow）

### Q5: 可以修改项目需求吗？
**A**: 可以！在完成基础功能后，可以添加自己的创新功能。这会在验收时加分。

### Q6: 一个月时间不够怎么办？
**A**: 时间规划是理想的进度。如果遇到困难，可以：
- 优先完成核心功能
- 简化非关键功能
- 寻求帮助
- 适当延长部分任务的完成时间

---

## 🎓 学习成果

完成这个项目后，你将获得：

1. **实际项目经验**：完整的系统开发经验
2. **技术能力提升**：掌握 Spring Boot、RabbitMQ、JPA 等主流技术
3. **架构设计能力**：理解消息驱动架构的设计
4. **问题解决能力**：通过解决各种问题提升调试和解决问题的能力
5. **项目作品**：可以作为作品集展示的项目

---

## 📝 项目报告模板

完成项目后，建议撰写项目报告，包括：

1. **项目概述**
   - 项目背景和目标
   - 技术栈选择理由

2. **系统设计**
   - 架构设计
   - 数据模型设计
   - 消息流程设计

3. **实现过程**
   - 开发过程记录
   - 遇到的问题和解决方案
   - 关键代码说明

4. **功能演示**
   - 系统运行截图
   - 功能演示视频（可选）

5. **总结与反思**
   - 学到了什么
   - 遇到的问题和解决方法
   - 未来改进方向

---

## 🚀 开始你的项目之旅

现在，你已经了解了整个项目。让我们开始第一周的学习吧！

**第一步**：仔细阅读项目文档和代码
**第二步**：搭建开发环境
**第三步**：创建项目骨架
**第四步**：开始编码！

**记住**：每天完成一点点，一个月后你会有惊人的成果！

**加油！💪**

---

## 📞 获取帮助

如果在项目中遇到问题：
1. 查看项目文档：`docs/` 目录
2. 查看代码注释
3. 询问老师或助教
4. 与同学讨论
5. 搜索相关技术文档

**祝你项目顺利！🎉**
