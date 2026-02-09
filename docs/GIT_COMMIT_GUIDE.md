# Git Commit Guide

## 📋 File Commit Analysis

### ✅ **Files That Should Be Committed (Required)**

#### 1. Project Configuration Files
```
✅ pom.xml                    # Maven configuration file
✅ .gitignore                 # Git ignore rules
✅ docker-compose.yml         # Docker configuration
✅ README.md                  # Project documentation
```

#### 2. Documentation Files (docs/ directory)
```
✅ docs/HOW_TO_START.md           # How to start
✅ docs/PROJECT_GUIDE.md          # Project guide
✅ docs/IMPLEMENTATION_GUIDE.md   # Implementation guide
✅ docs/CODE_TEMPLATES.md         # Code template reference
✅ docs/WEEKLY_PLAN.md            # Weekly plan
✅ docs/QUICKSTART.md             # Quick start
✅ docs/PROJECT_INDEX.md          # Documentation index
✅ docs/SYSTEM_DESIGN.md          # System design documentation
✅ docs/ARCHITECTURE_DIAGRAM.md   # Architecture diagrams
✅ docs/CSV_ORDER_FORMAT.md       # CSV format specification
✅ docs/INVENTORY_CSV_FORMAT.md   # Inventory CSV format
✅ docs/GIT_COMMIT_GUIDE.md       # This file
✅ SETUP.md                       # Environment setup
✅ TROUBLESHOOTING.md             # Troubleshooting
```

#### 3. Configuration Files (application.yml)
```
⚠️  application.yml             # Configuration file (see notes below)
```

#### 4. Sample Data Files
```
✅ src/main/resources/data/orders_sample.csv      # Order sample data
✅ src/main/resources/data/inventory_sample.csv   # Inventory sample data
```

#### 5. Startup Scripts
```
✅ start.sh           # Linux/Mac startup script
✅ start.bat          # Windows startup script
```

---

### ⚠️ **Files That Need Decision on Whether to Commit**

#### Java Source Code Files (src/main/java/)

**Option 1: Commit as Reference Template (Recommended)**
- ✅ Commit source code, but add notes: These are **reference code**, implementation should be done independently
- Pros: Can be used as reference, but direct copying is discouraged
- Recommendation: Clearly state in README or code comments

**Option 2: Don't Commit Source Code**
- ❌ Don't commit `src/main/java/` directory
- Pros: Forces independent implementation
- Cons: Cannot see complete implementation reference

**Recommended Approach**:
```
If as a reference project:
- ✅ Commit code, but add clear comments stating this is reference implementation
- ✅ Emphasize in README that independent implementation is required
- ✅ Add TODO comments in code to guide thinking
```

---

### ❌ **Files That Should NOT Be Committed (Already Ignored by .gitignore)**

#### Build Artifacts
```
❌ target/              # Maven build output directory
❌ *.class              # Compiled class files
❌ *.jar                # Packaged jar files
❌ *.war                # Packaged war files
```

#### IDE Configuration Files
```
❌ .idea/               # IntelliJ IDEA configuration
❌ .vscode/             # VS Code configuration
❌ *.iml                # IntelliJ module files
❌ .settings/           # Eclipse configuration
❌ .project             # Eclipse project file
❌ .classpath           # Eclipse classpath
```

#### System Files
```
❌ .DS_Store            # macOS system files
❌ Thumbs.db            # Windows thumbnails
❌ *.swp                # Vim swap files
❌ *.swo                # Vim swap files
❌ *~                   # Backup files
```

#### Log Files
```
❌ *.log                # Log files
❌ logs/                # Log directory
```

---

## 🎯 Recommendations for Reference Projects

### If This is a Project That Requires Independent Implementation

#### Recommended Approach: Provide Skeleton Code

Create a `starter/` branch or separate directory containing only:

```
✅ Project configuration files (pom.xml, application.yml)
✅ Documentation files (all docs/)
✅ Entity class definitions (fields only, no implementation)
✅ Repository interfaces (empty interfaces)
✅ Service interfaces or abstract classes (method signatures and TODO comments only)
✅ Controller skeletons (empty methods)
✅ Configuration file examples
```

**Main branch (main/master) can contain complete implementation as reference**

---

## 📝 Git Commit Checklist

### First Commit (Initialization)

```bash
# 1. Check .gitignore
git check-ignore target/

# 2. View files to be committed
git status

# 3. Add all files that should be committed
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

# 4. Decide whether to add source code
# Option A: Add source code (as reference)
git add src/main/java/

# Option B: Don't add source code
# (Skip this step)

# 5. Commit
git commit -m "Initial commit: Inventory Management Simulator

- Add project documentation and guides
- Add Maven configuration
- Add Docker Compose setup
- Add sample CSV data files
- Add implementation guides"
```

---

## 🔍 Check Commands

### View Which Files Will Be Committed
```bash
git status
```

### Check if .gitignore is Working
```bash
git check-ignore -v target/
git check-ignore -v *.class
```

### View List of Files to Be Committed
```bash
git ls-files
```

### Check for Large Files
```bash
find . -type f -size +1M | grep -v target | grep -v .git
```

---

## ⚙️ Recommended .gitignore Configuration

Check that your `.gitignore` should include:

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

## 🎯 Commit Recommendations for Reference Projects

### Main Branch (main/master) - Complete Implementation Reference
```
✅ All documentation
✅ Complete source code (as reference implementation)
✅ Configuration files
✅ Sample data
```

### starter Branch - Skeleton Code
```
✅ All documentation
✅ Configuration files
✅ Sample data
❌ Source code (or skeleton code only)
```

**Developers can start from starter branch, then merge to main branch after completion**

---

## ✅ Final Checklist

Before committing, confirm:
- [ ] `.gitignore` is configured correctly
- [ ] `target/` directory will not be committed
- [ ] All documentation files have been added
- [ ] Configuration files have been added
- [ ] Sample data files have been added
- [ ] Decision made on whether to commit source code
- [ ] README.md has been updated
- [ ] No sensitive information (passwords, keys, etc.)
- [ ] No large files (>10MB)
- [ ] Commit message is clear and descriptive

---

## 🚀 Quick Commit Commands

```bash
# Check status
git status

# View files to be committed
git add -n .

# Add all files (.gitignore will automatically exclude)
git add .

# View what will be committed
git status

# Commit
git commit -m "Add inventory management simulator project

- Project documentation and guides
- Maven and Docker configuration
- Sample CSV data files
- Implementation guides and templates"

# If remote repository is configured
git push origin main
```

---

## 📚 Related Resources

- [Git Official Documentation](https://git-scm.com/doc)
- [GitHub .gitignore Templates](https://github.com/github/gitignore)
- [Maven .gitignore](https://github.com/github/gitignore/blob/main/Maven.gitignore)
