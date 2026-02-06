# AI-Powered Java Full Stack Interview Preparation System
## Complete Project Documentation

### Project Overview
A production-quality final-year B.Tech project that helps students prepare for Java Full Stack interviews using **Grok (xAI)** for question generation and evaluation. Provide a Grok API key for hosted usage.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React + Vite)                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Login   │  │ Dashboard│  │ Interview│  │ Results  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│  ┌────────────────────────────────────────────────────┐   │
│  │      Monaco Editor (Java Code Editing)              │   │
│  └────────────────────────────────────────────────────┘   │
└───────────────────────┬───────────────────────────────────────┘
                       │ HTTP/REST API (JWT Auth)
┌───────────────────────┴───────────────────────────────────────┐
│                  Backend (Spring Boot 3.2+)                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐        │
│  │ Auth Service │  │Interview Svc │  │  AI Service │        │
│  └──────────────┘  └──────────────┘  └──────────────┘        │
│  ┌──────────────────────────────────────────────────────┐     │
│  │    Code Execution Service (JDoodle API)             │     │
│  └──────────────────────────────────────────────────────┘     │
└───────────────────────┬───────────────────────────────────────┘
                         │
        ┌────────────────┴────────────────┐
        │                                  │
┌───────▼────────┐              ┌────────▼────────┐
│   MySQL DB     │              │   Grok (xAI)    │
│  (Sessions,    │              │  (Hosted API)   │
│   Users, Q&A)  │              │                 │
└────────────────┘              └─────────────────┘
```

---

## 📋 Prerequisites

### 1. Configure Grok (hosted)
- This project uses the hosted Grok (xAI) API; no local LLM server is required.
- Set `GROK_API_KEY` in the environment available to the backend process.
  - Example (PowerShell persistent): `setx GROK_API_KEY "<your_api_key_here>"`
  - Or for a single session: `$env:GROK_API_KEY = "<your_api_key_here>"`

### 4. MySQL Database
```sql
CREATE DATABASE interview_prep_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 5. JDoodle API (Free Tier)
1. Sign up at https://www.jdoodle.com/
2. Get free API credentials from https://www.jdoodle.com/compiler-api
3. Free tier: 200 requests/day

---

## 🚀 Setup Instructions

### Backend Setup

1. **Navigate to backend directory:**
   ```bash
   cd backend
   ```

2. **Update `application.yml` with your credentials:**
   ```yaml
   spring:
     datasource:
       username: your_mysql_username
       password: your_mysql_password
   
    ai:
      grok:
        base-url: https://api.grok.x.ai
        endpoint: /v1/generate
        model: grokxai
        chat:
          temperature: 0.7
   
   jwt:
     secret: your-super-secret-jwt-key-minimum-256-bits-long
   
   jdoodle:
     client-id: your-jdoodle-client-id
     client-secret: your-jdoodle-client-secret
   ```

3. **Build and run:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   Backend starts on `http://localhost:8081`

### Frontend Setup

1. **Navigate to frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Run development server:**
   ```bash
   npm run dev
   ```
   Frontend starts on `http://localhost:5173` (or 5174 if 5173 is busy)

---

## 🔧 Configuration Details

### Ollama Models Comparison

| Model | Size | RAM Required | Quality | Speed |
|-------|------|--------------|---------|-------|
| llama3.2:latest (3B) | ~2GB | 4GB+ | Good | Fast |
| gemma2:9b | ~5GB | 8GB+ | Better | Medium |
| mistral:7b | ~4GB | 8GB+ | Best | Medium |

**Recommendation**: Start with `llama3.2:latest` for faster responses. Upgrade to `gemma2:9b` or `mistral:7b` for better quality.

### Environment Variables (Optional)

Instead of editing `application.yml`, you can use environment variables:

```bash
# Windows PowerShell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="yourpassword"
$env:OLLAMA_BASE_URL="http://localhost:11434"
$env:OLLAMA_MODEL="llama3.2:latest"
$env:JWT_SECRET="your-secret-key-min-256-bits"
$env:JDOODLE_CLIENT_ID="your-client-id"
$env:JDOODLE_CLIENT_SECRET="your-client-secret"

# Linux/macOS
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export OLLAMA_BASE_URL=http://localhost:11434
export OLLAMA_MODEL=llama3.2:latest
export JWT_SECRET=your-secret-key-min-256-bits
export JDOODLE_CLIENT_ID=your-client-id
export JDOODLE_CLIENT_SECRET=your-client-secret
```

---

## 📁 Project Structure

```
interview_system/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/interviewprep/
│       │   ├── config/
│       │   │   ├── SecurityConfig.java
│       │   │   ├── CorsConfig.java
│       │   │   └── WebClientConfig.java
│       │   ├── controller/
│       │   │   ├── AuthController.java
│       │   │   ├── InterviewController.java
│       │   │   ├── CodeExecutionController.java
│       │   │   └── ApiExceptionHandler.java
│       │   ├── dto/
│       │   │   ├── RegisterRequest.java
│       │   │   ├── LoginRequest.java
│       │   │   ├── AuthResponse.java
│       │   │   ├── InterviewRequest.java
│       │   │   ├── AnswerRequest.java
│       │   │   ├── FeedbackResponse.java
│       │   │   ├── SessionSummaryResponse.java
│       │   │   └── InterviewStartResponse.java
│       │   ├── entity/
│       │   │   ├── User.java
│       │   │   ├── InterviewSession.java
│       │   │   └── SessionQuestion.java
│       │   ├── repository/
│       │   │   ├── UserRepository.java
│       │   │   ├── InterviewSessionRepository.java
│       │   │   └── SessionQuestionRepository.java
│       │   ├── service/
│       │   │   ├── AuthService.java
│       │   │   ├── InterviewService.java
│       │   │   ├── AiService.java (Ollama-powered)
│       │   │   ├── CodeExecutionService.java
│       │   │   └── PdfExportService.java
│       │   ├── security/
│       │   │   ├── JwtAuthenticationFilter.java
│       │   │   ├── RateLimitingFilter.java
│       │   │   └── UserDetailsServiceImpl.java
│       │   ├── util/
│       │   │   ├── JwtUtil.java
│       │   │   └── PromptTemplates.java
│       │   └── JavaFullStackInterviewPrepApplication.java
│       └── resources/
│           └── application.yml
│
└── frontend/
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── components/
        │   ├── Navbar.jsx
        │   ├── MonacoEditorWrapper.jsx
        │   ├── FeedbackDisplay.jsx
        │   ├── QuestionCard.jsx
        │   └── Timer.jsx
        ├── pages/
        │   ├── Login.jsx
        │   ├── Register.jsx
        │   ├── Dashboard.jsx
        │   ├── StartInterview.jsx
        │   ├── InterviewSession.jsx
        │   └── Results.jsx
        ├── context/
        │   └── AuthContext.jsx
        ├── services/
        │   └── api.js
        ├── App.jsx
        └── main.jsx
```

---

## 🤖 AI Prompt Engineering

### System Prompt (Java Interviewer)
```
You are a strict but encouraging senior Java Full Stack interviewer with 12+ years of experience. 
Your expertise covers: Core Java, OOP, Collections, Multithreading, Spring Boot (REST, Security, Data JPA), 
Hibernate/JPA, MySQL/SQL, Microservices basics, and React + API integration.

When generating questions:
- Ask precise, job-relevant questions appropriate for the difficulty level
- Focus on practical scenarios and real-world applications
- For MCQ: provide 4 options (A-D) with clear correct answer
- For coding: provide problem statement, input/output format, sample test cases

When evaluating answers:
- Score 0-10 based on correctness, completeness, and depth
- Provide detailed explanation of what's correct/incorrect
- Give specific improvement tips
- Be encouraging but honest
- For coding: evaluate correctness, efficiency, clean code practices, edge case handling

Always respond in JSON format when requested, with no markdown fences or extra commentary.
```

### Question Generation Prompt Template
```
Generate a {questionType} question for a {interviewType} interview.
Topic: {topic}
Difficulty: {difficulty}

Rules:
- Keep it Java Full-Stack relevant
- If MCQ: include 4 options (A-D) and correct answer
- If SUBJECTIVE: ask one clear question
- If CODING: provide problem statement with input/output format and sample test cases

Output: plain text suitable to display to candidate.
```

### Answer Evaluation Prompt Template
```
Evaluate this candidate answer for a {questionType} question.

Question: {question}
Candidate Answer: {userAnswer}
Topic Context: {topicContext}

Return STRICT JSON:
{
  "score": 0-10,
  "feedback": "detailed feedback (2-6 sentences)",
  "explanation": "what an ideal answer includes",
  "suggestions": "specific improvements"
}
```

### Coding Evaluation Prompt Template
```
Review this Java code solution.

Problem: {problem}
Candidate Code: {code}
Observed Output: {output}
Hidden Test Descriptions: {hiddenTestsDescription}

Evaluate:
- Correctness (does it solve the problem?)
- Efficiency (time/space complexity)
- Clean code practices
- Edge case handling

Return STRICT JSON:
{
  "score": 0-10,
  "feedback": "detailed review",
  "explanation": "ideal solution approach",
  "suggestions": "concrete improvements"
}
```

---

## 🔐 Security Features

1. **Password Hashing**: BCrypt with strength 10
2. **JWT Authentication**: 24-hour expiration, secure secret (min 256 bits)
3. **CORS Configuration**: Restricted to frontend origins
4. **Input Validation**: Jakarta Validation annotations
5. **Code Execution**: All user code executed via JDoodle API (never on server)
6. **SQL Injection Protection**: JPA/Hibernate parameterized queries
7. **Rate Limiting**: Custom filter to prevent abuse

---

## 📊 Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Interview Sessions Table
```sql
CREATE TABLE interview_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    topic VARCHAR(255),
    total_score DOUBLE,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Session Questions Table
```sql
CREATE TABLE session_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    user_answer TEXT,
    ai_feedback TEXT,
    score DOUBLE,
    question_order INT,
    code_input TEXT,
    code_output TEXT,
    test_cases TEXT,
    FOREIGN KEY (session_id) REFERENCES interview_sessions(id)
);
```

---

## 🧪 Testing Checklist

- [ ] User registration
- [ ] User login
- [ ] Start interview session
- [ ] Generate MCQ questions via Ollama
- [ ] Generate subjective questions via Ollama
- [ ] Generate coding problems via Ollama
- [ ] Submit text answers and get AI feedback
- [ ] Write Java code in Monaco Editor
- [ ] Execute code via JDoodle API
- [ ] Submit coding solution and get AI evaluation
- [ ] View session history
- [ ] View session details with scores
- [ ] Export session report as PDF

---

## 🐛 Troubleshooting

### Ollama Connection Issues
**Error**: `Connection refused` or `Failed to connect to Ollama`
- **Solution**: Ensure Ollama server is running: `ollama serve`
- Check if Ollama is accessible: `curl http://localhost:11434/api/tags`
- Verify model is downloaded: `ollama list`

### Model Not Found
**Error**: `Model 'llama3.2:latest' not found`
- **Solution**: Pull the model: `ollama pull llama3.2:latest`

### Slow Response Times
- Use smaller model (`llama3.2:latest` instead of `gemma2:9b`)
- Reduce `temperature` in `application.yml` (try 0.5)
- Ensure adequate RAM (8GB+ recommended)

### JDoodle API Errors
**Error**: `401 Unauthorized` or `403 Forbidden`
- **Solution**: Verify JDoodle credentials in `application.yml`
- Check daily limit (200 requests/day on free tier)

### MySQL Connection Issues
- Verify MySQL service is running
- Check credentials in `application.yml`
- Ensure database exists: `CREATE DATABASE interview_prep_db;`

---

## 🚀 Production Deployment Notes

1. **Change JWT Secret**: Use a strong random secret (min 256 bits)
2. **Use Environment Variables**: Never commit secrets to git
3. **Enable HTTPS**: Use SSL/TLS certificates
4. **Configure CORS**: Set specific allowed origins (not `*`)
5. **Database User**: Use dedicated MySQL user (not root)
6. **Ollama Deployment**: 
   - For production, consider hosting Ollama on a separate server
   - Or use a cloud LLM service as fallback
7. **Rate Limiting**: Configure stricter limits for production
8. **Monitoring**: Add logging and monitoring (e.g., Spring Boot Actuator)

---

## 🔮 Future Enhancements

1. **RAG (Retrieval-Augmented Generation)**: Add local embeddings (e.g., Ollama embeddings) for question bank retrieval
2. **Voice Interviews**: WebRTC integration for voice-based questions
3. **Video Recording**: Record interview sessions for review
4. **Peer Review**: Allow users to review each other's answers
5. **Question Bank**: Pre-defined question library with difficulty levels
6. **Advanced Analytics**: Topic-wise performance charts, improvement trends
7. **Mobile App**: React Native mobile application
8. **Certificates**: Generate completion certificates
9. **Scheduled Interviews**: Schedule interviews with reminders
10. **OpenRouter Integration**: Optional fallback to free-tier cloud LLMs

---

## 📝 License

This project is for educational purposes (Final Year B.Tech Project).

---

## 👨‍💻 Development Notes

- **Code Execution**: All user code is executed via JDoodle API - never directly on the server
- **AI Costs**: Using local Ollama = $0 cost (no API charges)
- **Database**: Uses Hibernate auto-update; consider Flyway/Liquibase for production
- **JWT Secret**: Use a strong secret (min 256 bits) in production
- **Environment Variables**: Never commit API keys to version control

---

**Built with ❤️ for Java Full Stack Developers**
