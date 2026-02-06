# ✅ Final Setup Checklist

## Before Running - Verify Everything

### ✅ Prerequisites
- [ ] Java 17/21 installed (`java -version`)
- [ ] Maven 3.6+ installed (`mvn -version`)
- [ ] Node.js 18+ installed (`node -v`)
- [ ] MySQL 8.0+ installed
- [ ] Ollama installed

### ✅ MySQL Setup
- [ ] MySQL service is running (`net start MySql84`)
- [ ] Database `interview_prep_db` exists
- [ ] Can connect with: `mysql -u root -p` (or your username)

### ✅ Ollama Setup
- [ ] Ollama is installed
- [ ] Ollama server is running (`ollama serve`)
- [ ] Model downloaded (`ollama pull llama3.2:latest`)
- [ ] Verify: `curl http://localhost:11434/api/tags` returns JSON

### ✅ Configuration
- [ ] `backend/src/main/resources/application.yml` updated with:
  - [ ] MySQL username (line 10)
  - [ ] MySQL password (line 11)
  - [ ] JDoodle client-id (line 44)
  - [ ] JDoodle client-secret (line 45)
  - [ ] JWT secret (line 39) - use strong random string
  - [ ] Ollama model matches your downloaded model (line 28)

### ✅ JDoodle API
- [ ] Account created at https://www.jdoodle.com/
- [ ] API credentials obtained from https://www.jdoodle.com/compiler-api
- [ ] Credentials added to `application.yml`

---

## 🚀 Startup Sequence

### 1. Start Ollama (Terminal 1)
```bash
ollama serve
```
**Keep this running!**

### 2. Start Backend (Terminal 2)
```powershell
cd backend
mvn spring-boot:run
```
**Wait for**: `Started JavaFullStackInterviewPrepApplication`

### 3. Start Frontend (Terminal 3)
```powershell
cd frontend
npm run dev
```
**Wait for**: `Local: http://localhost:5173`

### 4. Open Browser
- Go to: `http://localhost:5173`
- Register → Login → Start Interview!

---

## 🔍 Verification Tests

### Test 1: Backend Health
```bash
curl http://localhost:8081/actuator/health
```
Should return: `{"status":"UP"}`

### Test 2: Ollama Connection
```bash
curl http://localhost:11434/api/tags
```
Should return JSON with your models

### Test 3: Frontend Connection
- Open `http://localhost:5173`
- Should see login/register page
- No console errors

### Test 4: Full Flow
1. ✅ Register new user
2. ✅ Login
3. ✅ Start interview
4. ✅ Answer questions
5. ✅ Get AI feedback
6. ✅ View session history

---

## 🐛 Troubleshooting Quick Reference

| Error | Quick Fix |
|-------|-----------|
| Port 8081 in use | `netstat -ano \| findstr :8081` → `taskkill /PID <PID> /F` |
| MySQL connection error | `net start MySql84` |
| Ollama not found | Install from https://ollama.com/download |
| Model not found | `ollama pull llama3.2:latest` |
| Registration fails | Check MySQL running, try different email |
| "Failed to start interview" | Check Ollama running, check backend logs |
| LocalDateTime error | Already fixed - strings used instead |

---

## 📝 Configuration Values Needed

Before starting, you need these values in `application.yml`:

1. **MySQL Username**: Your MySQL username (usually `root`)
2. **MySQL Password**: Your MySQL password
3. **JDoodle Client ID**: From https://www.jdoodle.com/compiler-api
4. **JDoodle Client Secret**: From https://www.jdoodle.com/compiler-api
5. **JWT Secret**: Generate a strong random string (min 256 bits)
   - Example: Use online generator or: `openssl rand -base64 32`

---

## 🎯 Success Indicators

You'll know everything is working when:

✅ Backend starts without errors
✅ Frontend loads in browser
✅ Can register a new user
✅ Can login
✅ Can start an interview
✅ Questions appear (check backend logs for "Generated question via Ollama")
✅ Can submit answers
✅ AI feedback appears
✅ Code execution works (for coding questions)

---

## 📚 Documentation Files

- **START_HERE.md** - Quick 5-minute setup
- **COMPLETE_SETUP_GUIDE.md** - Detailed step-by-step guide
- **SETUP_OLLAMA.md** - Ollama-specific setup
- **PROJECT_DOCUMENTATION.md** - Complete project documentation
- **QUICK_START.md** - Fast reference

---

**Ready to start? Follow the Startup Sequence above!** 🚀
