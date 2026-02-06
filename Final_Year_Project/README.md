# Role-Based AI-Powered Java Full Stack Interview Preparation System

A production-quality B.Tech final-year project that provides personalized interview preparation based on the user's target job role. The system uses Spring AI with Grok (xAI) for role-aware question generation and feedback, and includes coding practice with Monaco Editor and JDoodle execution.

## 🎯 Core Features

1. **User Authentication**: Secure registration/login with BCrypt password hashing and JWT tokens
2. **Mandatory Role Selection**: After first login, users must select their target role before accessing the dashboard
3. **Role-Tailored Interviews**: Questions, difficulty, and feedback are personalized based on selected role
4. **Multiple Interview Modes**: MCQ, Subjective, Coding, and Full Mock Interview
5. **AI-Powered Evaluation**: Hosted Grok (xAI) integration for dynamic question generation and role-aware feedback
6. **Code Execution**: Monaco Editor with JDoodle API for safe Java code execution
7. **Analytics Dashboard**: Progress tracking, topic coverage, strengths/weaknesses analysis

## 🛠️ Tech Stack

### Backend
- Java 21
- Spring Boot 3.3.5
- Spring AI integration (Grok via HTTP)
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0+
- Maven

### Frontend
- React 18.3
- Vite 5.4
- Tailwind CSS 3.4
- Monaco Editor (@monaco-editor/react)
- React Router v6

## 📋 Prerequisites

1. **Java 21** (or Java 17+)
2. **Maven 3.8+**
3. **MySQL 8.0+**
4. **Node.js 18+** and npm
5. **Grok API key** (set `GROK_API_KEY` in your environment)
6. **JDoodle API** (free account at https://www.jdoodle.com/compiler-api)

## 🚀 Quick Start

### 1. Configure Grok API key
Set your Grok API key in the environment variable `GROK_API_KEY`. Do not commit the key to source control.

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
CREATE DATABASE ai_interview_prep;
```

### 3. Configure Backend
Edit `src/main/resources/application.yml` with your MySQL password, JDoodle credentials, and JWT secret.

### 4. Run Backend
```bash
mvn spring-boot:run
```

### 5. Run Frontend
```bash
cd frontend
npm install
npm run dev
```

### 6. Access Application
Open http://localhost:5173 and register/login!

## 📁 Project Structure

```
all_roles_interview/
├── src/main/java/com/interviewprep/
│   ├── config/          # Security, Web, AI configuration
│   ├── controller/      # REST controllers
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA entities
│   ├── repository/     # Spring Data repositories
│   ├── security/       # JWT utilities, filters
│   └── service/        # Business logic
├── frontend/
│   ├── src/
│   │   ├── api/        # API client
│   │   ├── components/ # React components
│   │   ├── context/    # Auth context
│   │   └── pages/      # Page components
└── README.md
```

## 📄 Documentation

- **SETUP.md**: Detailed setup instructions
- **PROMPTS.md**: AI prompt examples and role-aware templates
- **database/schema.sql**: Complete database schema
- **UI_UX_NOTES.md**: Design guidelines and UX considerations

## 🔐 Security

- BCrypt password hashing
- JWT authentication (24-hour expiration)
- Protected routes
- Role-based access control
- CORS configuration

## 📝 License

Educational project for B.Tech final year.
