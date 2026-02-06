# ⚡ Quick Start Guide

## 🎯 Get Running in 3 Steps

### Step 1: Configure Grok API key
Set your Grok API key in the environment variable `GROK_API_KEY`. Do not commit the key to source control.

PowerShell (persistent):
```powershell
setx GROK_API_KEY "<your_api_key_here>"
```

Temporary (current session):
```powershell
$env:GROK_API_KEY = "<your_api_key_here>"
```

### Step 2: Configure & Start Backend
```powershell
# Edit backend/src/main/resources/application.yml
# Set: MySQL username/password, JDoodle credentials, JWT secret

# Then start:
cd backend
mvn spring-boot:run
```

### Step 3: Start Frontend
```powershell
# Open new terminal
cd frontend
npm install
npm run dev
```

### Step 4: Use the App
- Open `http://localhost:5173`
- Register → Login → Start Interview!

---
## 🔍 Verify Everything Works

### Check Grok / Backend:
- Ensure `GROK_API_KEY` is set in the environment available to the backend process.
- Look for: `Started JavaFullStackInterviewPrepApplication`
- No errors about MySQL or Grok connectivity in backend logs
### Check Backend:
- Look for: `Started JavaFullStackInterviewPrepApplication`
### Check Frontend:
- No CORS errors in browser console

---

## 🐛 Common Issues
| Grok API key missing or invalid | Ensure `GROK_API_KEY` is set and valid |
| MySQL not running | `net start MySql84` (as Admin) |
| Ollama not found | (Not required) This project uses hosted Grok; ensure `GROK_API_KEY` is set |
| Model not found | (Not applicable) Using hosted Grok model via API |
| "Failed to start interview" | Check backend logs for Grok connectivity and that `GROK_API_KEY` is present |

---

## 📋 Configuration Checklist
	- [ ] `GROK_API_KEY` is set in environment for backend process
	- [ ] `GROK_API_KEY` is set in environment for backend process

---

## 🚀 Using Batch Files (Windows)

Double-click these files in order:

1. `start-ollama.bat` - Starts Ollama server
2. `start-backend.bat` - Starts Spring Boot backend
3. `start-frontend.bat` - Starts React frontend
**Note**: Keep all 3 windows open while using the app!

---

**Ready? Start with Step 1!** ⬆️
