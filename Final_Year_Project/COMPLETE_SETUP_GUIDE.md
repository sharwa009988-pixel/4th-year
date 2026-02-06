# Complete Setup Guide - Step by Step

## 🎯 Prerequisites Checklist

Before starting, ensure you have:

- [ ] **Java 17 or 21** installed (`java -version`)
- [ ] **Maven 3.6+** installed (`mvn -version`)
- [ ] **Node.js 18+** installed (`node -v`)
- [ ] **MySQL 8.0+** installed and running
- [ ] **Ollama** installed (see Step 1 below)
- [ ] **JDoodle API** credentials (free tier)

---

## Step 1: Install and Setup Ollama

### Windows:
1. Download from: https://ollama.com/download
2. Run the installer
3. Ollama will start automatically

### macOS:
```bash
brew install ollama
```

### Linux:
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

### Start Ollama Server:
```bash
ollama serve
```
**Keep this terminal open!** Ollama must be running.

### Pull a Model (in a NEW terminal):
```bash
# For 4GB+ RAM (Recommended):
ollama pull llama3.2:latest

# OR for better quality (8GB+ RAM):
ollama pull gemma2:9b
# OR
ollama pull mistral:7b
```

### Verify Ollama is Running:
```bash
# Test the API
curl http://localhost:11434/api/tags

# Test the model
ollama run llama3.2:latest "What is Java?"
```

---

## Step 2: Setup MySQL Database

### Start MySQL Service:
```powershell
# Windows (as Administrator)
net start MySql84
```

### Create Database:
Open MySQL Workbench or command line:
```sql
CREATE DATABASE interview_prep_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Verify Connection:
```sql
SHOW DATABASES;
-- Should see interview_prep_db
```

---

## Step 3: Get JDoodle API Credentials

1. Go to: https://www.jdoodle.com/
2. Sign up for free account
3. Navigate to: https://www.jdoodle.com/compiler-api
4. Copy your `clientId` and `clientSecret`
5. Free tier: 200 requests/day

---

## Step 4: Configure Backend

### Edit `backend/src/main/resources/application.yml`:

```yaml
server:
  port: 8081

spring:
  application:
    name: java-fullstack-interview-prep
  
  datasource:
    url: jdbc:mysql://localhost:3306/interview_prep_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: YOUR_MYSQL_USERNAME  # Change this
    password: YOUR_MYSQL_PASSWORD  # Change this
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.2:latest  # Change if using different model
          temperature: 0.7

jwt:
  secret: YOUR_SUPER_SECRET_JWT_KEY_MINIMUM_256_BITS_LONG_CHANGE_THIS
  expiration: 86400000

jdoodle:
  client-id: YOUR_JDOODLE_CLIENT_ID
  client-secret: YOUR_JDOODLE_CLIENT_SECRET
  api-url: https://api.jdoodle.com/v1/execute

cors:
  allowed-origins: http://localhost:5173,http://localhost:5174,http://localhost:3000

logging:
  level:
    com.interviewprep: DEBUG
    org.springframework.security: INFO
    org.springframework.web: INFO
```

**Important**: Replace all `YOUR_*` placeholders with your actual values!

---

## Step 5: Build Backend

```powershell
cd backend
mvn clean install
```

**Wait for build to complete** - this downloads all dependencies including Ollama Spring AI.

---

## Step 6: Start Backend

```powershell
# Make sure Ollama is running first!
mvn spring-boot:run
```

**Wait for**: `Started JavaFullStackInterviewPrepApplication`

**If you see errors:**
- **Port 8081 in use**: Kill the process or change port in `application.yml`
- **MySQL connection error**: Check MySQL is running and credentials are correct
- **Ollama connection error**: Ensure `ollama serve` is running

---

## Step 7: Start Frontend

**Open a NEW terminal:**

```powershell
cd frontend
npm install
npm run dev
```

**Wait for**: `Local: http://localhost:5173` (or 5174)

---

## Step 8: Test the Application

1. **Open browser**: `http://localhost:5173` (or the port shown)
2. **Register**: Create a new account
3. **Login**: Use your credentials
4. **Start Interview**: 
   - Select interview type
   - Choose topic
   - Click "Start Interview"
5. **Answer Questions**: 
   - Submit answers
   - Get AI feedback
   - For coding: write code, run it, submit

---

## 🔍 Troubleshooting

### Backend Won't Start

**Error: Port 8081 already in use**
```powershell
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

**Error: MySQL connection refused**
- Verify MySQL service: `net start MySql84`
- Check credentials in `application.yml`
- Test connection: `mysql -u root -p`

**Error: Ollama connection failed**
- Check Ollama is running: `curl http://localhost:11434/api/tags`
- Verify model is downloaded: `ollama list`
- Check `base-url` in `application.yml`

### Frontend Issues

**Error: Cannot connect to backend**
- Verify backend is running on port 8081
- Check `vite.config.js` proxy target
- Check browser console for CORS errors

**Error: Registration failed**
- Check backend logs for error details
- Verify MySQL is running
- Check if email already exists (try different email)

### Ollama Issues

**Slow responses**
- Use smaller model (`llama3.2` instead of `gemma2:9b`)
- Reduce temperature in `application.yml` (try 0.5)
- Ensure adequate RAM (8GB+ recommended)

**Model not found**
- Pull the model: `ollama pull llama3.2:latest`
- Verify: `ollama list`

---

## ✅ Verification Checklist

After setup, verify:

- [ ] Ollama server is running (`ollama serve`)
- [ ] Ollama model is downloaded (`ollama list` shows your model)
- [ ] MySQL service is running
- [ ] Database `interview_prep_db` exists
- [ ] Backend starts without errors
- [ ] Frontend starts without errors
- [ ] Can register a new user
- [ ] Can login
- [ ] Can start an interview
- [ ] Questions are generated (check backend logs for "Generated question via Ollama")
- [ ] Can submit answers and get feedback
- [ ] Code execution works (for coding questions)

---

## 🚀 Quick Start Commands

**Terminal 1 - Ollama:**
```bash
ollama serve
```

**Terminal 2 - Backend:**
```powershell
cd backend
mvn spring-boot:run
```

**Terminal 3 - Frontend:**
```powershell
cd frontend
npm run dev
```

**Browser:**
- Open `http://localhost:5173`
- Register → Login → Start Interview

---

## 📝 Configuration Summary

| Component | Location | Key Settings |
|-----------|----------|--------------|
| **Ollama** | `application.yml` | `base-url: http://localhost:11434`, `model: llama3.2:latest` |
| **MySQL** | `application.yml` | `url`, `username`, `password` |
| **JDoodle** | `application.yml` | `client-id`, `client-secret` |
| **JWT** | `application.yml` | `secret`, `expiration` |
| **Backend Port** | `application.yml` | `port: 8081` |
| **Frontend Port** | Vite auto | Usually `5173` |

---

## 🎓 For Project Submission

### Required Files:
1. ✅ Complete source code (backend + frontend)
2. ✅ Documentation (this guide + PROJECT_DOCUMENTATION.md)
3. ✅ Database schema (JPA entities)
4. ✅ Setup instructions (this file)
5. ✅ Screenshots of working application
6. ✅ Demo video (optional but recommended)

### Project Report Sections:
1. Introduction & Problem Statement
2. Literature Survey
3. System Analysis & Design
4. Implementation Details
5. Testing & Results
6. Conclusion & Future Work

---

**You're all set! Follow the steps above to get everything running.** 🚀
