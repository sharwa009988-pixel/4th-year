# AI-Powered Java Full Stack Interview Preparation System
## Complete Final Year B.Tech Project

### 🎯 Project Overview

A production-quality interview preparation platform that helps students prepare for Java Full Stack interviews using a hosted LLM (Grok by default) for dynamic question generation and intelligent answer evaluation. The app can use a Grok API key for hosted inference.

### ✨ Key Features

- ✅ **User Authentication**: Secure registration/login with JWT tokens
- ✅ **AI-Powered Questions**: Dynamic generation via Grok (hosted LLM)
- ✅ **Multiple Interview Modes**: MCQ, Subjective, Coding, Full Mock
- ✅ **Code Execution**: Safe Java code execution via JDoodle API
- ✅ **AI Feedback**: Intelligent scoring (0-10) with detailed feedback
- ✅ **Session History**: Complete interview history with analytics
- ✅ **PDF Export**: Generate session reports as PDF
- ✅ **Progress Tracking**: Topic-wise performance analysis

### 🛠️ Tech Stack

**Backend:**
- Java 17/21
- Spring Boot 3.2+
- Grok (xAI) HTTP integration
- Spring Security + JWT
- Spring Data JPA / Hibernate
- MySQL 8.0+
- JDoodle API (code execution)
- iText7 (PDF generation)

**Frontend:**
- React 18+ (Vite)
- React Router v6
- Tailwind CSS
- Monaco Editor (code editing)
- Axios
- React Hot Toast

**AI:**
- Ollama (local LLM server)
- Models: llama3.2:latest, gemma2:9b, mistral:7b

---

## 🚀 Quick Start Guide

### Prerequisites Checklist

- [ ] Java 17 or 21 installed
- [ ] Maven 3.6+ installed
- [ ] Node.js 18+ and npm installed
- [ ] MySQL 8.0+ installed and running
- [ ] Grok API key available (set `GROK_API_KEY` in environment)
- [ ] JDoodle API credentials (free tier)

### Step 1: Configure Grok API key

Set your Grok API key in the `GROK_API_KEY` environment variable. Do not commit the key to source control.

PowerShell (persistent):
```powershell
setx GROK_API_KEY "<your_api_key_here>"
```

Temporary (current session):
```powershell
$env:GROK_API_KEY = "<your_api_key_here>"
```

### Step 3: Setup Database

```sql
CREATE DATABASE interview_prep_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 4: Configure Backend

Edit `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    username: your_mysql_username
    password: your_mysql_password
  
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama3.2:latest
          temperature: 0.7

jwt:
  secret: your-super-secret-jwt-key-minimum-256-bits-long

jdoodle:
  client-id: your-jdoodle-client-id
  client-secret: your-jdoodle-client-secret
```

### Step 5: Start Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on `http://localhost:8081`

### Step 6: Start Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:5173`

### Step 7: Test the Application

1. Open `http://localhost:5173`
2. Register a new account
3. Login
4. Start a mock interview
5. Answer questions and get AI feedback!

---

## 📁 Project Structure

```
interview_system/
├── backend/
│   ├── pom.xml                          # Maven dependencies (includes Grok integration)
│   └── src/main/
│       ├── java/com/interviewprep/
│       │   ├── config/                  # Spring configuration
│       │   │   ├── SecurityConfig.java
│       │   │   ├── CorsConfig.java
│       │   │   ├── WebClientConfig.java
│       │   │   └── AiConfig.java        # AI ChatClient setup (Grok)
│       │   ├── controller/              # REST endpoints
│       │   │   ├── AuthController.java
│       │   │   ├── InterviewController.java
│       │   │   └── CodeExecutionController.java
│       │   ├── dto/                     # Data Transfer Objects
│       │   ├── entity/                  # JPA entities
│       │   ├── repository/              # JPA repositories
│       │   ├── service/                 # Business logic
│       │   │   ├── AuthService.java
│       │   │   ├── InterviewService.java
│       │   │   ├── AiService.java       # Grok HTTP integration
│       │   │   ├── CodeExecutionService.java
│       │   │   └── PdfExportService.java
│       │   ├── security/                # Security filters
│       │   ├── util/
│       │   │   ├── JwtUtil.java
│       │   │   └── PromptTemplates.java  # AI prompts
│       │   └── JavaFullStackInterviewPrepApplication.java
│       └── resources/
│           └── application.yml           # Configuration
│
├── frontend/
│   ├── package.json                     # NPM dependencies
│   ├── vite.config.js                   # Vite config (proxy to backend)
│   └── src/
│       ├── components/                  # React components
│       │   ├── Navbar.jsx
│       │   ├── MonacoEditorWrapper.jsx
│       │   ├── FeedbackDisplay.jsx
│       │   └── QuestionCard.jsx
│       ├── pages/                      # Page components
│       │   ├── Login.jsx
│       │   ├── Register.jsx
│       │   ├── Dashboard.jsx
│       │   ├── StartInterview.jsx
│       │   ├── InterviewSession.jsx
│       │   └── Results.jsx
│       ├── context/
│       │   └── AuthContext.jsx         # Auth state management
│       ├── services/
│       │   └── api.js                  # Axios API client
│       ├── App.jsx
│       └── main.jsx
│
├── PROJECT_DOCUMENTATION.md            # Complete documentation
├── SETUP_OLLAMA.md                    # Grok setup guide (kept filename for compatibility)
└── README_COMPLETE.md                 # This file
```

---

## 🤖 AI Integration Details

### Grok (xAI) Integration

The backend calls a Grok endpoint to generate questions and evaluate answers. Configure `spring.ai.grok.*` in `application.yml` or provide `GROK_API_KEY` in the environment for hosted access.

### How It Works

1. **Question Generation**: 
   - User starts interview → Backend calls `AiService.generateQuestion()`
   - Grok generates a question based on topic, difficulty, and type
   - Question stored in database

2. **Answer Evaluation**:
   - User submits answer → Backend calls `AiService.evaluateAnswer()`
   - Grok evaluates the answer, provides feedback and a suggested score
   - Response parsed and stored

3. **Coding Evaluation**:
   - Code executed via JDoodle API
   - Output + code sent to Grok for review
   - Detailed feedback on correctness, efficiency, and best practices

### Fallback Mechanism

If Grok is unreachable or returns no content, the system falls back to local deterministic generators so the UI remains usable.

---

## 🔐 Security Features

- **Password Hashing**: BCrypt with strength 10
- **JWT Authentication**: 24-hour expiration, secure secret
- **CORS Protection**: Restricted to frontend origins
- **Input Validation**: Jakarta Validation
- **Code Execution**: All code runs via JDoodle API (never on server)
- **SQL Injection Protection**: JPA parameterized queries
- **Rate Limiting**: Custom filter to prevent abuse

---

## 📊 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Interviews
- `POST /api/interviews/start` - Start new interview session
- `POST /api/interviews/{sessionId}/answer` - Submit answer
- `GET /api/interviews/sessions` - Get user's sessions
- `GET /api/interviews/sessions/{sessionId}` - Get session details
- `POST /api/interviews/sessions/{sessionId}/end` - End session
- `GET /api/interviews/sessions/{sessionId}/export` - Export PDF report

### Code Execution
- `POST /api/code/execute` - Execute Java code via JDoodle

---

## 🧪 Testing Checklist

- [x] User registration
- [x] User login
- [x] Start interview session
- [x] Generate questions via Grok (xAI)
- [x] Submit answers and get AI feedback
- [x] Code execution via JDoodle
- [x] Coding solution evaluation
- [x] Session history
- [x] PDF export

---

## 🐛 Troubleshooting

### Backend Issues

**Port 8081 already in use:**
```bash
# Find process
netstat -ano | findstr :8081
# Kill process (Windows)
taskkill /PID <PID> /F
```

**MySQL connection error:**
- Verify MySQL is running
- Check credentials in `application.yml`
- Ensure database exists

**Grok connection error:**
- Ensure `GROK_API_KEY` is set and valid for the backend process
- Verify the backend can reach the Grok endpoint
- Check `base-url` in `application.yml`

### Frontend Issues

**CORS errors:**
- Verify backend CORS config includes frontend URL
- Check `vite.config.js` proxy settings

**Monaco Editor not loading:**
- Clear browser cache
- Verify `@monaco-editor/react` is installed

---

## 📝 Development Notes

- **Code Execution**: All user code runs via JDoodle API - never on server
- **AI Costs**: Using local Ollama = $0 (no API charges)
- **Database**: Hibernate auto-update enabled (use Flyway for production)
- **JWT Secret**: Use strong secret (min 256 bits) in production
- **Environment Variables**: Never commit secrets to git

---

## 🚀 Production Deployment

1. **Change JWT Secret**: Use strong random value
2. **Use Environment Variables**: Never hardcode secrets
3. **Enable HTTPS**: Use SSL/TLS certificates
4. **Configure CORS**: Set specific allowed origins
5. **Database User**: Use dedicated MySQL user (not root)
6. **Ollama Deployment**: Consider separate server or cloud LLM fallback
7. **Rate Limiting**: Configure stricter limits
8. **Monitoring**: Add logging and monitoring

---

## 🔮 Future Enhancements

1. **RAG Integration**: Add local embeddings for question retrieval
2. **Voice Interviews**: WebRTC for voice-based questions
3. **Video Recording**: Record sessions for review
4. **Peer Review**: Users review each other's answers
5. **Question Bank**: Pre-defined question library
6. **Advanced Analytics**: Performance charts, trends
7. **Mobile App**: React Native version
8. **Certificates**: Generate completion certificates

---

## 📄 License

This project is for educational purposes (Final Year B.Tech Project).

---

## 👨‍💻 Author

Built for Final Year B.Tech Computer Science/Engineering students.

**Tech Stack**: Java 17, Spring Boot 3.2+, React 18, Grok (xAI), MySQL

**AI Model**: Hosted Grok (grokxai)

---

## 📚 Additional Resources

- [Grok Documentation](https://docs.grok.x.ai/)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [JDoodle API Docs](https://www.jdoodle.com/compiler-api)
- [Spring Boot Guide](https://spring.io/guides)

---

**Built with ❤️ for Java Full Stack Developers**
