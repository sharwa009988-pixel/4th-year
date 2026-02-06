# 🚀 RUN NOW - Execute These Commands (Grok)

## Quick Start (copy-paste)

1) Ensure `GROK_API_KEY` is set in the environment available to the backend process.

PowerShell (persistent):
```powershell
setx GROK_API_KEY "<your_api_key_here>"
```

Or for current session only:
```powershell
$env:GROK_API_KEY = "<your_api_key_here>"
```

2) Start the backend:
```powershell
cd backend
mvn spring-boot:run
```
Wait for: `Started JavaFullStackInterviewPrepApplication`

3) Start the frontend in a new terminal:
```powershell
cd frontend
npm install
npm run dev
```
Wait for: `Local: http://localhost:5173`

4) Open the app: http://localhost:5173

---

Checks
- Ensure `GROK_API_KEY` is visible to the backend process (restart terminal/IDE if needed).
- Check backend logs for Grok call activity or errors.
- Ensure MySQL is running: `net start MySql84`

If the UI shows "AI coding problem generation is temporarily unavailable" then the backend returned a fallback; restart the backend so it picks up the new code and environment variables, then retry.

---

**Ready? Start with step 1 above.**
