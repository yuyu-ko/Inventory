# Git 提交指南

## 📋 文件提交分析

### ✅ **应该提交的文件（必须）**

#### 1. 项目配置文件
```
✅ pom.xml                    # Maven 配置文件
✅ .gitignore                 # Git 忽略规则
✅ docker-compose.yml         # Docker 配置
✅ README.md                  # 项目说明
```

#### 2. 文档文件（docs/ 目录）
```
✅ docs/HOW_TO_START.md           # 如何开始
✅ docs/STUDENT_GUIDE.md          # 学生项目指南
✅ docs/IMPLEMENTATION_GUIDE.md   # 实现指南
✅ docs/CODE_TEMPLATES.md         # 代码模板参考
✅ docs/WEEKLY_PLAN.md            # 四周计划
✅ docs/STUDENT_QUICKSTART.md     # 快速开始
✅ docs/PROJECT_INDEX.md          # 文档索引
✅ docs/SYSTEM_DESIGN.md          # 系统设计文档
✅ docs/ARCHITECTURE_DIAGRAM.md   # 架构图
✅ docs/CSV_ORDER_FORMAT.md       # CSV 格式说明
✅ docs/INVENTORY_CSV_FORMAT.md   # 库存 CSV 格式
✅ docs/GIT_COMMIT_GUIDE.md       # 本文件
✅ SETUP.md                       # 环境设置
✅ TROUBLESHOOTING.md             # 故障排除
```

#### 3. 配置文件（application.yml）
```
⚠️  application.yml             # 配置文件（见下面说明）
```

#### 4. 示例数据文件
```
✅ src/main/resources/data/orders_sample.csv      # 订单示例数据
✅ src/main/resources/data/inventory_sample.csv   # 库存示例数据
```

#### 5. 启动脚本
```
✅ start.sh           # Linux/Mac 启动脚本
✅ start.bat          # Windows 启动脚本
```

---

### ⚠️ **需要决定是否提交的文件**

#### Java 源代码文件（src/main/java/）

**选项 1：作为参考模板提交（推荐）**
- ✅ 提交源代码，但添加说明：这些是**参考代码**，学生应该自己实现
- 优点：学生可以参考，但不鼓励直接复制
- 建议：在 README 或代码注释中明确说明

**选项 2：不提交源代码**
- ❌ 不提交 `src/main/java/` 目录
- 优点：强制学生自己实现
- 缺点：学生无法看到完整的实现参考

**建议做法**：
```
如果作为教学项目：
- ✅ 提交代码，但添加明确的注释说明这是参考实现
- ✅ 在 README 中强调学生需要自己实现
- ✅ 在代码中添加 TODO 注释，引导学生思考
```

---

### ❌ **不应该提交的文件（已由 .gitignore 忽略）**

#### 编译产物
```
❌ target/              # Maven 编译输出目录
❌ *.class              # 编译后的类文件
❌ *.jar                # 打包后的 jar 文件
❌ *.war                # 打包后的 war 文件
```

#### IDE 配置文件
```
❌ .idea/               # IntelliJ IDEA 配置
❌ .vscode/             # VS Code 配置
❌ *.iml                # IntelliJ 模块文件
❌ .settings/           # Eclipse 配置
❌ .project             # Eclipse 项目文件
❌ .classpath           # Eclipse 类路径
```

#### 系统文件
```
❌ .DS_Store            # macOS 系统文件
❌ Thumbs.db            # Windows 缩略图
❌ *.swp                # Vim 交换文件
❌ *.swo                # Vim 交换文件
❌ *~                   # 备份文件
```

#### 日志文件
```
❌ *.log                # 日志文件
❌ logs/                # 日志目录
```

---

## 🎯 针对学生项目的建议

### 如果这是**学生需要自己实现**的项目

#### 推荐做法：提供骨架代码

创建一个 `starter/` 分支或单独的目录，只包含：

```
✅ 项目配置文件（pom.xml, application.yml）
✅ 文档文件（所有 docs/）
✅ 实体类定义（只有字段，没有实现）
✅ Repository 接口（空接口）
✅ Service 接口或抽象类（只有方法签名和 TODO 注释）
✅ Controller 骨架（空方法）
✅ 配置文件示例
```

**主分支（main/master）可以包含完整实现作为参考**

---

## 📝 Git 提交清单

### 第一次提交（初始化）

```bash
# 1. 检查 .gitignore
git check-ignore target/

# 2. 查看将要提交的文件
git status

# 3. 添加所有应该提交的文件
git add .gitignore
git add README.md
git add pom.xml
git add docker-compose.yml
git add docs/
git add SETUP.md
git add TROUBLESHOOTING.md
git add start.sh
git add start.bat
git add src/main/resources/

# 4. 根据决定是否添加源代码
# 选项 A：添加源代码（作为参考）
git add src/main/java/

# 选项 B：不添加源代码
# （跳过这一步）

# 5. 提交
git commit -m "Initial commit: Inventory Management Simulator

- Add project documentation and guides
- Add Maven configuration
- Add Docker Compose setup
- Add sample CSV data files
- Add implementation guides for students"
```

---

## 🔍 检查命令

### 查看哪些文件会被提交
```bash
git status
```

### 查看 .gitignore 是否生效
```bash
git check-ignore -v target/
git check-ignore -v *.class
```

### 查看将要提交的文件列表
```bash
git ls-files
```

### 检查是否有大文件
```bash
find . -type f -size +1M | grep -v target | grep -v .git
```

---

## ⚙️ .gitignore 建议配置

检查你的 `.gitignore` 应该包含：

```gitignore
# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
pom.xml.versionsBackup
pom.xml.next
release.properties
dependency-reduced-pom.xml
buildNumber.properties
.mvn/timing.properties
.mvn/wrapper/maven-wrapper.jar

# Compiled class files
*.class

# Log files
*.log
logs/

# Package Files
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# IDE - IntelliJ IDEA
.idea/
*.iws
*.iml
*.ipr
out/

# IDE - Eclipse
.settings/
.classpath
.project
.metadata
bin/

# IDE - VS Code
.vscode/

# OS
.DS_Store
Thumbs.db
*~

# Spring Boot
application-*.yml
!application.yml

# H2 Database
*.db
*.mv.db
*.trace.db
```

---

## 🎓 针对学生的提交建议

### 主分支（main/master）- 完整实现参考
```
✅ 所有文档
✅ 完整的源代码（作为参考实现）
✅ 配置文件
✅ 示例数据
```

### starter 分支 - 骨架代码
```
✅ 所有文档
✅ 配置文件
✅ 示例数据
❌ 源代码（或只有骨架代码）
```

**学生可以从 starter 分支开始，完成后再合并到 main 分支**

---

## ✅ 最终检查清单

提交前确认：
- [ ] `.gitignore` 已配置正确
- [ ] `target/` 目录不会被提交
- [ ] 所有文档文件都已添加
- [ ] 配置文件已添加
- [ ] 示例数据文件已添加
- [ ] 源代码是否提交已决定
- [ ] README.md 已更新（说明这是学生项目）
- [ ] 没有敏感信息（密码、密钥等）
- [ ] 没有大文件（>10MB）
- [ ] 提交信息清晰明确

---

## 🚀 快速提交命令

```bash
# 检查状态
git status

# 查看将要提交的文件
git add -n .

# 添加所有文件（.gitignore 会自动排除）
git add .

# 查看将要提交的内容
git status

# 提交
git commit -m "Add inventory management simulator project

- Project documentation and student guides
- Maven and Docker configuration
- Sample CSV data files
- Implementation guides and templates"

# 如果已配置远程仓库
git push origin main
```

---

## 📚 相关资源

- [Git 官方文档](https://git-scm.com/doc)
- [GitHub .gitignore 模板](https://github.com/github/gitignore)
- [Maven .gitignore](https://github.com/github/gitignore/blob/main/Maven.gitignore)
