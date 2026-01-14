# Git 提交检查清单

## 📋 快速检查

### ✅ 必须提交的文件

#### 配置文件
- [ ] `.gitignore`
- [ ] `pom.xml`
- [ ] `docker-compose.yml`
- [ ] `README.md`

#### 文档目录（docs/）
- [ ] `docs/HOW_TO_START.md`
- [ ] `docs/STUDENT_GUIDE.md`
- [ ] `docs/IMPLEMENTATION_GUIDE.md`
- [ ] `docs/CODE_TEMPLATES.md`
- [ ] `docs/WEEKLY_PLAN.md`
- [ ] `docs/STUDENT_QUICKSTART.md`
- [ ] `docs/PROJECT_INDEX.md`
- [ ] `docs/SYSTEM_DESIGN.md`
- [ ] `docs/ARCHITECTURE_DIAGRAM.md`
- [ ] `docs/CSV_ORDER_FORMAT.md`
- [ ] `docs/INVENTORY_CSV_FORMAT.md`
- [ ] `docs/GIT_COMMIT_GUIDE.md`

#### 根目录文档
- [ ] `SETUP.md`
- [ ] `TROUBLESHOOTING.md`
- [ ] `QUICK_START.md`（如果存在）

#### 配置文件
- [ ] `src/main/resources/application.yml`

#### 示例数据
- [ ] `src/main/resources/data/orders_sample.csv`
- [ ] `src/main/resources/data/inventory_sample.csv`

#### 启动脚本
- [ ] `start.sh`
- [ ] `start.bat`

#### Java 源代码（根据你的决定）
- [ ] `src/main/java/` 目录（如果是参考实现）
- [ ] 或创建 `starter/` 分支只包含骨架代码

---

### ❌ 不应该提交的文件

#### 编译产物（由 .gitignore 自动排除）
- [ ] `target/` 目录
- [ ] `*.class` 文件
- [ ] `*.jar` 文件

#### IDE 配置（由 .gitignore 自动排除）
- [ ] `.idea/` 目录
- [ ] `.vscode/` 目录
- [ ] `*.iml` 文件
- [ ] `.settings/` 目录

---

## 🔍 检查命令

### 1. 查看所有将要提交的文件
```bash
git status
```

### 2. 确认 .gitignore 生效
```bash
# 检查 target/ 是否被忽略
git check-ignore -v target/

# 应该输出类似：.gitignore:1:target/ target/
```

### 3. 查看所有已跟踪的文件
```bash
git ls-files
```

### 4. 检查是否有不应该提交的文件
```bash
# 检查是否有 .class 文件
find . -name "*.class" | grep -v target

# 检查是否有大文件（>10MB）
find . -type f -size +10M | grep -v target | grep -v .git
```

---

## 📝 推荐的提交流程

### 第一次提交
```bash
# 1. 检查 .gitignore
cat .gitignore

# 2. 查看状态
git status

# 3. 添加所有文件
git add .

# 4. 检查将要提交的内容
git status

# 5. 提交
git commit -m "Initial commit: Inventory Management Simulator

- Add project documentation and student guides
- Add Maven configuration (pom.xml)
- Add Docker Compose setup
- Add sample CSV data files (orders, inventory)
- Add implementation guides and code templates
- Add setup and troubleshooting guides"

# 6. 如果已配置远程仓库
git remote -v
git push origin main
```

---

## ⚠️ 特别注意

### 1. application.yml 中的敏感信息
检查 `application.yml` 是否包含：
- ❌ 密码
- ❌ API 密钥
- ❌ 数据库连接字符串（生产环境）

如果有，应该：
- 使用环境变量
- 或创建 `application.example.yml` 作为模板

### 2. Java 源代码的提交策略

**选项 A：作为参考实现提交**
- 在 README 中明确说明：这是参考实现，学生应该自己写代码
- 在代码中添加注释：`// TODO: 学生需要自己实现`
- 优点：学生可以参考

**选项 B：不提交源代码**
- 只提交文档、配置、示例数据
- 优点：强制学生自己实现
- 缺点：学生缺少参考

**选项 C：创建两个分支**
- `main` 分支：完整实现（参考）
- `starter` 分支：只有骨架代码（学生使用）

---

## ✅ 最终确认

提交前最后检查：
- [ ] `.gitignore` 文件存在且配置正确
- [ ] 没有编译产物（target/, *.class）
- [ ] 没有 IDE 配置文件
- [ ] 没有敏感信息
- [ ] 所有文档都已添加
- [ ] README.md 说明了这是学生项目
- [ ] 提交信息清晰明了

---

## 🚀 快速提交

```bash
# 一键检查并提交
git add .
git status  # 仔细检查
git commit -m "Add inventory management simulator project files"
```
