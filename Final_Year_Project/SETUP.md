# Detailed Setup Guide

## Step-by-Step Installation

### 1. Configure Grok (xAI)

This project uses the hosted Grok API by default. Provide your Grok API key via the `GROK_API_KEY` environment variable.

PowerShell (persistent):
```powershell
setx GROK_API_KEY "<your_api_key_here>"
```

Temporary (current session):
```powershell
$env:GROK_API_KEY = "<your_api_key_here>"
```

### 2. Setup MySQL

```sql
CREATE DATABASE ai_interview_prep CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configure Backend

Edit `src/main/resources/application.yml`:
- MySQL username/password
- JWT secret (32+ characters)
- JDoodle client-id and client-secret (get from https://www.jdoodle.com/compiler-api)
- (Optional) Grok model and endpoint – defaults are suitable for most users

### 4. Run Backend

```bash
mvn clean install
mvn spring-boot:run
```

Backend: http://localhost:8080/api

### 5. Setup Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend: http://localhost:5173

### 6. First Run

1. Register account
2. Select role (mandatory)
3. Access dashboard
4. Start interview!

## Troubleshooting

- **Grok connectivity**: Ensure `GROK_API_KEY` is set in the environment available to the backend process and that the backend can reach `https://api.grok.x.ai` (or your proxy).
- **MySQL errors**: Check credentials and database exists
- **Frontend can't connect**: Verify backend on port 8080 and CORS config
