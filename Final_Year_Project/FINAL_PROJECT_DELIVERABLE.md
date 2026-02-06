Project: Role-Based AI-Powered Interview Preparation System (Multi-Domain)

1) Setup instructions
- Install Ollama locally and run the server: `ollama serve --listen 11434`.
- Pull a suitable model locally: `ollama pull <model-name>` (e.g. `llama3`/`llama3.2`).
- Configure MySQL and create database `ai_interview_prep` (see `database/schema.sql`).
- Set environment variables: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` (32+ chars), `OLLAMA_BASE_URL` if different, `JDOODLE_CLIENT_ID`, `JDOODLE_CLIENT_SECRET`.
- JDoodle: sign up for API keys and set `app.jdoodle.client-id` and `app.jdoodle.client-secret` in `application.yml` or environment.
- Build and run backend: `mvn clean package && java -jar target/role-based-ai-interview-1.0.0.jar`
- Frontend: `cd frontend && npm install && npm run dev` (Vite default at http://localhost:5173).

2) Full `pom.xml` (root) — updated in repository: add `spring-boot-starter-webflux`, JWT, JPA, MySQL connectors. See file: [pom.xml](pom.xml)

3) `application.yml` example
- File: [src/main/resources/application.yml](src/main/resources/application.yml)
- Key sections: `spring.datasource`, `spring.ai.ollama.base-url`, `app.jwt.secret`, `app.jdoodle.client-id`, `app.jdoodle.client-secret`.

4) Backend Java files (key paths)
- `src/main/java/com/interviewprep/entity/User.java` (user entity with `role` field)
- `src/main/java/com/interviewprep/repository/UserRepository.java`
- `src/main/java/com/interviewprep/controller/AuthController.java` (register/login)
- `src/main/java/com/interviewprep/security/JwtUtil.java` and `SecurityConfig.java`, `JwtAuthenticationFilter.java`
- `src/main/java/com/interviewprep/service/OllamaService.java` (calls local Ollama `/api/chat` using `WebClient`)
- `src/main/java/com/interviewprep/service/InterviewService.java` (session starter, builds system prompt and requests next question from Ollama)
- `src/main/java/com/interviewprep/controller/InterviewController.java` (start/next endpoints)
- `src/main/java/com/interviewprep/service/JDoodleService.java` and `src/main/java/com/interviewprep/controller/CodeController.java`

5) Frontend key files (React)
- `frontend/src/pages/RoleSelectionPage.jsx` (already in repo) — uses `/roles/predefined` and `/roles/select`.
- `frontend/src/pages/Dashboard.jsx` (new) — shows selected role and quick-start links.
- `frontend/src/pages/InterviewSimple.jsx` (new) — starts interview and requests next questions in real time.
- `frontend/src/pages/Profile.jsx` (new)
- `frontend/src/api/client.js` — simplified API client used by pages.

6) Database schema
- See `database/schema.sql` in repo — includes `users`, `interview_sessions`, `session_questions` tables. The JPA entities persist sessions separately (JSON conversation) for simplicity.

7) Sample role-aware system prompts
- System prompt template used by `InterviewService`:
  "You are a professional senior interviewer hiring for the role of [USER_ROLE]. Generate one relevant, high-quality interview question at a time based on the conversation so far. Respond in JSON with fields: {\\"type\\": <MCQ|SUBJECTIVE|CODING>, \\\"difficulty\\\": <EASY|MEDIUM|HARD>, \\\"question\\\": <text>, \\\"choices\\\": <array|null>, \\\"stdin\\\": <for coding|null> }"
- Example for `Spring Boot Microservices Engineer`:
  "You are a senior interviewer hiring for the role of Spring Boot Microservices Engineer. Generate one question at a time, considering previous answers, focusing on distributed transactions, service discovery, resilience, and observability. Difficulty: MEDIUM. Return JSON as specified."

8) Notes on real-time generation best practices
- Always include the `role` and short system instruction in the system prompt to keep the model focused.
- Keep per-session conversation history on the server and prune old tokens to avoid context overflow; keep the last N turns and a short summary of earlier turns.
- Ask the model to return structured JSON to make parsing reliable.
- Validate and sanitize model output before rendering in UI.
- Use streaming (SSE/WebSocket) if low-latency incremental responses are required; current implementation uses blocking WebClient for simplicity.
- Rate-limit and cache similar prompts when load is high; do not store or hardcode questions anywhere.

9) Next steps & extensions
- Add more robust client-side components for MCQ rendering and code submission with Monaco editor integration (`@monaco-editor/react` exists in repo).
- Add tests and monitoring (Actuator, health checks for Ollama & JDoodle).
- Consider adding conversation summarization to reduce context size for long sessions.

If you'd like, I can now:
- Run a local build and point out any compile errors.
- Add streaming responses and a session-summary endpoint.
