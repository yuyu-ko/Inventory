# Git Commit Checklist

## 📋 Quick Check

### ✅ Files That Must Be Committed

#### Configuration Files
- [ ] `.gitignore`
- [ ] `pom.xml`
- [ ] `docker-compose.yml`
- [ ] `README.md`

#### Documentation Directory (docs/)
- [ ] `docs/HOW_TO_START.md`
- [ ] `docs/PROJECT_GUIDE.md`
- [ ] `docs/IMPLEMENTATION_GUIDE.md`
- [ ] `docs/CODE_TEMPLATES.md`
- [ ] `docs/WEEKLY_PLAN.md`
- [ ] `docs/QUICKSTART.md`
- [ ] `docs/PROJECT_INDEX.md`
- [ ] `docs/SYSTEM_DESIGN.md`
- [ ] `docs/ARCHITECTURE_DIAGRAM.md`
- [ ] `docs/CSV_ORDER_FORMAT.md`
- [ ] `docs/INVENTORY_CSV_FORMAT.md`
- [ ] `docs/GIT_COMMIT_GUIDE.md`

#### Root Directory Documentation
- [ ] `SETUP.md`
- [ ] `TROUBLESHOOTING.md`

#### Configuration Files
- [ ] `src/main/resources/application.yml`

#### Sample Data
- [ ] `src/main/resources/data/orders_sample.csv`
- [ ] `src/main/resources/data/inventory_sample.csv`

#### Startup Scripts
- [ ] `start.sh`
- [ ] `start.bat`

#### Java Source Code (Based on Your Decision)
- [ ] `src/main/java/` directory (if providing reference implementation)
- [ ] Or create `starter/` branch with skeleton code only

---

### ❌ Files That Should NOT Be Committed

#### Build Artifacts (Automatically Excluded by .gitignore)
- [ ] `target/` directory
- [ ] `*.class` files
- [ ] `*.jar` files

#### IDE Configuration (Automatically Excluded by .gitignore)
- [ ] `.idea/` directory
- [ ] `.vscode/` directory
- [ ] `*.iml` files
- [ ] `.settings/` directory

---

## 🔍 Check Commands

### 1. View All Files to Be Committed
```bash
git status
```

### 2. Confirm .gitignore is Working
```bash
# Check if target/ is ignored
git check-ignore -v target/

# Should output something like: .gitignore:1:target/ target/
```

### 3. View All Tracked Files
```bash
git ls-files
```

### 4. Check for Files That Should Not Be Committed
```bash
# Check for .class files
find . -name "*.class" | grep -v target

# Check for large files (>10MB)
find . -type f -size +10M | grep -v target | grep -v .git
```

---

## 📝 Recommended Commit Process

### First Commit
```bash
# 1. Check .gitignore
cat .gitignore

# 2. View status
git status

# 3. Add all files
git add .

# 4. Check what will be committed
git status

# 5. Commit
git commit -m "Initial commit: Inventory Management Simulator

- Add project documentation and guides
- Add Maven configuration (pom.xml)
- Add Docker Compose setup
- Add sample CSV data files (orders, inventory)
- Add implementation guides and code templates
- Add setup and troubleshooting guides"

# 6. If remote repository is configured
git remote -v
git push origin main
```

---

## ⚠️ Special Notes

### 1. Sensitive Information in application.yml
Check if `application.yml` contains:
- ❌ Passwords
- ❌ API keys
- ❌ Database connection strings (production)

If yes, should:
- Use environment variables
- Or create `application.example.yml` as template

### 2. Java Source Code Commit Strategy

**Option A: Commit as Reference Implementation**
- Clearly state in README: This is a reference implementation
- Add comments in code: `// TODO: Implementation required`
- Pros: Can be used as reference

**Option B: Don't Commit Source Code**
- Only commit documentation, configuration, sample data
- Pros: Forces independent implementation
- Cons: No reference available

**Option C: Create Two Branches**
- `main` branch: Complete implementation (reference)
- `starter` branch: Skeleton code only

---

## ✅ Final Confirmation

Before committing, final check:
- [ ] `.gitignore` file exists and is configured correctly
- [ ] No build artifacts (target/, *.class)
- [ ] No IDE configuration files
- [ ] No sensitive information
- [ ] All documentation has been added
- [ ] Commit message is clear and descriptive

---

## 🚀 Quick Commit

```bash
# One-click check and commit
git add .
git status  # Review carefully
git commit -m "Add inventory management simulator project files"
```
