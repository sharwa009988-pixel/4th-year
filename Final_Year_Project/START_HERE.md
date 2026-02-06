# 🚀 START HERE - Quick Setup

## ⚡ Fast Track (5 Minutes)

### 1️⃣ Configure Grok (xAI)
This project now uses Grok (xAI) instead of Ollama. Provide your API key via an environment variable:

Windows (PowerShell):
```powershell
setx GROK_API_KEY "<your_api_key_here>"
```

Or for a single session:
```powershell
$env:GROK_API_KEY = "<your_api_key_here>"
```

Optionally set `GROK_BASE_URL` and `GROK_ENDPOINT` if you use a custom proxy.

### 3️⃣ Update Configuration
Edit `backend/src/main/resources/application.yml`:
- Line 10: `username: YOUR_MYSQL_USERNAME`
- Line 11: `password: YOUR_MYSQL_PASSWORD`
- Line 44: `client-id: YOUR_JDOODLE_CLIENT_ID`
- Line 45: `client-secret: YOUR_JDOODLE_CLIENT_SECRET`

### 4️⃣ Start Everything

No local Ollama server is required when using the hosted Grok API.

**Terminal 2 (Backend):**
```powershell
cd backend
mvn spring-boot:run
```
Wait for: `Started JavaFullStackInterviewPrepApplication`

**Terminal 3 (Frontend):**
```powershell
cd frontend
npm run dev
```
Wait for: `Local: http://localhost:5173`

### 5️⃣ Use the App
1. Open `http://localhost:5173`
2. Click "Sign Up"
3. Register → Login → Start Interview!

---

## 🔧 If Something Fails

### Backend won't start?
- Check MySQL is running: `net start MySql84`
- Check port 8081 is free
- Check `application.yml` credentials

### Frontend won't start?
- Run `npm install` first
- Check if port 5173 is free

### "Failed to start interview"?
- Ensure `GROK_API_KEY` is set in the backend environment and reachable from the running process
- Check backend logs for errors

### Registration fails?
- Check MySQL is running
- Check database exists
- Try different email (might already exist)

---

## 📚 Full Documentation

- **Complete Setup**: See `COMPLETE_SETUP_GUIDE.md`
- **Grok Setup**: See `SETUP_OLLAMA.md`
- **Project Docs**: See `PROJECT_DOCUMENTATION.md`

---

**Ready? Start with Step 1 above!** ⬆️
