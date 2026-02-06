const express = require('express');
const cors = require('cors');
let fetchFn = typeof fetch === 'function' ? fetch : null;
if (!fetchFn) {
  try { fetchFn = require('node-fetch'); } catch {}
}

const fs = require('fs');
const path = require('path');
const USERS_DB_PATH = path.join(__dirname, 'users.json');

const app = express();
app.use(cors());
app.use(express.json());

let users = [];
try {
  if (fs.existsSync(USERS_DB_PATH)) {
    const raw = fs.readFileSync(USERS_DB_PATH, 'utf-8');
    const parsed = JSON.parse(raw || '[]');
    if (Array.isArray(parsed)) users = parsed;
  }
} catch {}
const sessions = [];
let questionSeq = 1;
const mcqCorrect = new Map();
const AI_KEY = process.env.GROKX_API_KEY || process.env.GROK_API_KEY || process.env.X_API_KEY || process.env.XAI_API_KEY || '';
const AI_MODEL = process.env.GROKX_MODEL || process.env.GROK_MODEL || 'grok-2';
const AI_BASE_URL = process.env.GROKX_API_BASE_URL || 'https://api.x.ai/v1/chat/completions';

async function grokChat(system, user, temperature = 0.7) {
  if (!AI_KEY) return null;
  try {
    const resp = await fetchFn(AI_BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${AI_KEY}`,
      },
      body: JSON.stringify({
        model: AI_MODEL,
        messages: [
          { role: 'system', content: system },
          { role: 'user', content: user },
        ],
        temperature,
        response_format: { type: 'json_object' }
      }),
    });
    if (!resp.ok) return null;
    const data = await resp.json();
    const content = data?.choices?.[0]?.message?.content || '';
    return typeof content === 'string' ? content.trim() : '';
  } catch (e) {
    return null;
  }
}

async function grokJson(system, user, schemaHint = '', temperature = 0.2) {
  const first = await grokChat(system, user, temperature);
  let parsed = parseJsonSafe(first);
  if (parsed && typeof parsed === 'object') return parsed;
  const sys2 = schemaHint
    ? `${system} Ensure output matches this schema strictly: ${schemaHint}. Return ONLY JSON.`
    : `${system} Return ONLY JSON. Do not include any prose.`;
  const second = await grokChat(sys2, user, temperature);
  parsed = parseJsonSafe(second);
  if (parsed && typeof parsed === 'object') return parsed;
  return null;
}

function parseJsonSafe(text) {
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    const start = text.indexOf('{');
    const end = text.lastIndexOf('}');
    if (start >= 0 && end > start) {
      try { return JSON.parse(text.slice(start, end + 1)); } catch {}
    }
    return null;
  }
}

function getUserFromAuth(req) {
  const auth = req.headers.authorization || '';
  if (!auth.startsWith('Bearer ')) return null;
  const token = auth.substring(7);
  const m = token.match(/^dev-token-(\d+)$/);
  if (!m) return null;
  const id = Number(m[1]);
  return users.find(u => u.id === id) || null;
}

app.post('/api/auth/register', (req, res, next) => {
  try {
    const { name, email, password, confirmPassword } = req.body;
    if (!name || !email || !password) return res.status(400).json({ message: 'Missing fields' });
    if (password !== confirmPassword) return res.status(400).json({ message: 'Passwords do not match' });
    if (users.find(u => u.email === email)) return res.status(409).json({ message: 'Email already exists' });

    const user = { id: users.length + 1, name, email, roleSelected: false, role: '' };
    users.push({ ...user, password });
    try { fs.writeFileSync(USERS_DB_PATH, JSON.stringify(users, null, 2)); } catch {}
    res.status(201).json({
      token: `dev-token-${user.id}`,
      email: user.email,
      name: user.name,
      userId: user.id,
      roleSelected: false,
    });
  } catch (err) {
    next(err);
  }
});

app.post('/api/auth/login', (req, res, next) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ message: 'Missing fields' });
    const saved = users.find(u => u.email === email);
    if (!saved) return res.status(401).json({ message: 'Invalid email or password' });
    if (saved.password !== password) return res.status(401).json({ message: 'Invalid email or password' });
    const user = { id: saved.id, name: saved.name, email: saved.email, roleSelected: !!saved.roleSelected };
    res.json({
      token: `dev-token-${user.id}`,
      email: user.email,
      name: user.name,
      userId: user.id,
      roleSelected: !!saved.roleSelected,
    });
  } catch (err) {
    next(err);
  }
});

app.get('/api/users/me', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  res.json({
    email: saved.email,
    name: saved.name,
    userId: saved.id,
    role: saved.role || '',
    roleSelected: !!saved.roleSelected,
  });
});

app.get('/api/roles/predefined', (req, res) => {
  res.json([
    "Java Full Stack Developer",
    "Java Backend Developer",
    "Spring Boot Microservices Engineer",
    "Senior Java Developer",
    "React Frontend Developer",
    "Full Stack Developer (React + Node.js / MERN)",
    "Python Backend Developer (Django / FastAPI)",
    "Data Scientist / Machine Learning Engineer",
    "DevOps Engineer",
    "Cloud Engineer (AWS / Azure / GCP)",
    "Software Engineer (General)",
    "Other"
  ]);
});

app.post('/api/roles/select', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const role = req.body.role;
  if (!role || !role.trim()) return res.status(400).json({ message: 'Role is required' });
  saved.role = role.trim();
  saved.roleSelected = true;
  try { fs.writeFileSync(USERS_DB_PATH, JSON.stringify(users, null, 2)); } catch {}
  res.json({ message: 'Role selected', role: saved.role });
});

app.get('/api/dashboard/stats', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const mySessions = sessions.filter(x => x.userId === saved.id);
  const stats = {
    targetRole: saved.role || '',
    totalSessions: mySessions.length,
    sessionsThisRole: mySessions.length,
    averageScore: 0,
    recentTopics: Array.from(new Set(mySessions.slice(-10).map(s => s.topic || ''))).filter(Boolean),
    strengthsWeaknesses: {
      strengths: [],
      weaknesses: []
    },
    topicScores: []
  };
  res.json(stats);
});

app.get('/api/public/health', (req, res) => {
  res.json({ status: 'ok' });
});

// ---------- Interview endpoints ----------
app.post('/api/interview/start', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const { mode, topic, difficulty } = req.body || {};
  if (!mode) return res.status(400).json({ message: 'Mode is required' });
  const id = Date.now();
  const session = { id, userId: saved.id, mode, topic: topic || '', difficulty: difficulty || 'MEDIUM', startedAt: new Date().toISOString(), endedAt: null };
  sessions.push(session);
  res.json({ id, mode: session.mode, topic: session.topic, difficulty: session.difficulty });
});

app.post('/api/interview/question/generate', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const { mode, topic, difficulty } = req.body || {};
  const id = questionSeq++;
  const baseTopic = (topic && topic.trim()) ? topic.trim() : (saved.role || 'General');
  const diff = (difficulty && difficulty.trim()) ? difficulty.trim().toUpperCase() : 'MEDIUM';
  const mRaw = (mode && mode.trim()) ? mode.trim().toUpperCase() : 'SUBJECTIVE';
  const m = mRaw === 'FULL_MOCK' ? 'SUBJECTIVE' : mRaw;
  function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
  const SUBJECTIVE = {
    General: [
      'Explain SOLID principles with practical examples in Java.',
      'Describe how to design REST APIs with proper status codes.',
      'Discuss transactions and isolation levels in Spring Data JPA.',
      'Explain thread safety and concurrency best practices in Java.',
      'Describe JWT-based authentication and refresh token strategy.',
    ],
    'Java Full Stack Developer': [
      'How would you design a CRUD app with Spring Boot and React?',
      'Explain state management approaches in React for complex apps.',
      'Discuss securing a full-stack app (CORS, CSRF, JWT, roles).',
      'Describe error handling and validation on backend and frontend.',
    ],
    'Java Backend Developer': [
      'Explain Spring Boot auto-configuration and profiles usage.',
      'How do you design pagination and filtering for large datasets?',
      'Discuss database indexing and query optimization with JPA.',
    ],
    'Senior Java Developer': [
      'Describe hexagonal architecture and its benefits in large systems.',
      'How to design scalable microservices with resilience patterns?',
      'Discuss performance tuning strategies for high throughput APIs.',
    ],
    'Spring Boot Microservices Engineer': [
      'Explain inter-service communication patterns and circuit breakers.',
      'Describe service discovery and configuration management approach.',
      'Discuss observability: logs, metrics, tracing in microservices.',
    ],
  };
  const MCQ = {
    General: [
      'Which collection offers O(1) average-time lookup?\\nA) LinkedList\\nB) HashMap\\nC) TreeMap\\nD) Vector\\nAnswer: B',
      'What HTTP status means created?\\nA) 200\\nB) 201\\nC) 202\\nD) 204\\nAnswer: B',
      'Which annotation enables Spring Boot app?\\nA) @Configuration\\nB) @EnableAutoConfiguration\\nC) @SpringBootApplication\\nD) @Component\\nAnswer: C',
    ],
    'Java Backend Developer': [
      'Which isolation level prevents dirty reads?\\nA) READ_UNCOMMITTED\\nB) READ_COMMITTED\\nC) REPEATABLE_READ\\nD) SERIALIZABLE\\nAnswer: B',
      'Which JPA fetch type is default for @ManyToOne?\\nA) EAGER\\nB) LAZY\\nC) NONE\\nD) EXTRA\\nAnswer: A',
    ],
    'Spring Boot Microservices Engineer': [
      'Which pattern handles remote failures?\\nA) Facade\\nB) Circuit Breaker\\nC) Decorator\\nD) Iterator\\nAnswer: B',
      'Which tool provides distributed tracing?\\nA) Lombok\\nB) Zipkin\\nC) Mockito\\nD) Flyway\\nAnswer: B',
    ],
  };
  const CODING = {
    General: [
      'Implement an LRU cache supporting get/put in O(1).',
      'Given an array, find two numbers that sum to target.',
      'Check if parentheses in a string are balanced.',
      'Design a rate limiter supporting allow() within window.',
      'Implement a thread-safe producer-consumer queue.',
    ],
    'Java Backend Developer': [
      'Design a REST endpoint to paginate results with filters.',
      'Implement a connection pool with basic acquire/release.',
      'Build a simple in-memory key-value store with TTL eviction.',
    ],
    'Java Full Stack Developer': [
      'Implement a JSON serializer for a subset of types.',
      'Build a form validator that reports field-level errors.',
    ],
  };
  let questionText = '';
  questionText = '';
  const sysQ = 'You are a senior technical interviewer. Generate one role-appropriate question only. If MCQ, list options each on a new line exactly formatted "A) ...", "B) ...", "C) ...", "D) ...". Do not include the correct answer. Keep it concise and job-relevant. Never repeat a previous question for the same role and topic.';
  const promptQ = `Seed: ${id}\nMode: ${m}\nRole: ${saved.role || baseTopic}\nTopic: ${baseTopic}\nDifficulty: ${diff}\nGenerate exactly one question.\nFor MCQ: first line is the question stem, then four lines with options A) to D). Do not include the answer.`;
  if (AI_KEY) {
    questionText = null;
    grokChat(sysQ, promptQ).then(content => {
      if (content && typeof content === 'string' && content.trim().length > 0) {
        const txt = content.replace(/\\n/g, '\n').trim();
        res.json({ questionId: id, question: txt });
      } else {
        let local = '';
        if (m === 'MCQ') {
          const bank = MCQ[baseTopic] || MCQ['General'];
          const raw = pick(bank);
          const mAns = raw.match(/Answer:\s*([A-D])/);
          const ans = mAns ? mAns[1] : null;
          if (ans) mcqCorrect.set(id, ans);
          local = `(${m}) [${diff}] ${baseTopic}: ${raw.replace(/\\nAnswer:.*$/,'')}`;
        } else if (m === 'CODING') {
          const bank = CODING[baseTopic] || CODING['General'];
          local = `(${m}) [${diff}] ${baseTopic}: ${pick(bank)}`;
        } else {
          const bank = SUBJECTIVE[baseTopic] || SUBJECTIVE['General'];
          local = `(${m}) [${diff}] ${baseTopic}: ${pick(bank)}`;
        }
        res.json({ questionId: id, question: local });
      }
    }).catch(() => {
      const bank = SUBJECTIVE[baseTopic] || SUBJECTIVE['General'];
      res.json({ questionId: id, question: `(${m}) [${diff}] ${baseTopic}: ${pick(bank)}` });
    });
  } else {
    if (m === 'MCQ') {
      const bank = MCQ[baseTopic] || MCQ['General'];
      const raw = pick(bank);
      const mAns = raw.match(/Answer:\s*([A-D])/);
      const ans = mAns ? mAns[1] : null;
      if (ans) mcqCorrect.set(id, ans);
      questionText = `(${m}) [${diff}] ${baseTopic}: ${raw.replace(/\\nAnswer:.*$/,'')}`;
    } else if (m === 'CODING') {
      const bank = CODING[baseTopic] || CODING['General'];
      questionText = `(${m}) [${diff}] ${baseTopic}: ${pick(bank)}`;
    } else {
      const bank = SUBJECTIVE[baseTopic] || SUBJECTIVE['General'];
      questionText = `(${m}) [${diff}] ${baseTopic}: ${pick(bank)}`;
    }
    res.json({ questionId: id, question: questionText });
  }
});

app.post('/api/interview/evaluate', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const { userAnswer, questionText, mode, topic, difficulty } = req.body || {};
  if (AI_KEY) {
    const m = (mode && mode.trim()) ? mode.trim().toUpperCase() : 'SUBJECTIVE';
    if (m === 'MCQ') {
      const key = typeof req.body.questionId === 'number' ? req.body.questionId : null;
      const correct = key ? mcqCorrect.get(key) : null;
      const schema = `{"feedback":{"candidateAnswer":string,"correctAnswer":string,"score":number,"explanation":string,"mistakes":string,"example":string},"newQuestion":{"question":string,"type":string,"difficulty":string}}`;
      const sysE = 'Return ONLY JSON per schema. Use provided correct option letter as ground truth and give interview-grade explanation and mistake analysis. Keep explanations 3-5 lines.';
      const promptE = `Role: ${saved.role || ''}\nType: MCQ\nDifficulty: ${(difficulty||'MEDIUM')}\nQuestion:\n${questionText}\nCorrect Option Letter: ${correct || 'UNKNOWN'}\nCandidate Option Letter: ${String(userAnswer||'').trim().toUpperCase()}\nExplain technically why the correct option is right and the candidate choice is wrong if applicable. Provide a short example.`;
      grokJson(sysE, promptE, schema, 0.2).then(parsed => {
        if (parsed && parsed.feedback && parsed.newQuestion) {
          res.json(parsed);
        } else {
          const sysExp = 'Explain briefly (3-5 lines) why the provided MCQ correct option is right and the candidate choice is wrong.';
          const promptExp = `Question:\n${questionText}\nCorrect: ${correct || 'UNKNOWN'}\nCandidate: ${String(userAnswer||'').trim().toUpperCase()}`;
          grokChat(sysExp, promptExp, 0.2).then(exp => {
            const explanation = (exp && typeof exp === 'string') ? exp.replace(/\\n/g,'\n').trim() : '';
            res.json({
              feedback: {
                candidateAnswer: String(userAnswer||'').trim().toUpperCase(),
                correctAnswer: correct || '',
                score: (correct && String(userAnswer||'').trim().toUpperCase() === correct) ? 100 : 0,
                explanation,
                mistakes: (correct && String(userAnswer||'').trim().toUpperCase() !== correct) ? 'Selected option does not satisfy the required property.' : '',
                example: ''
              },
              newQuestion: {
                question: 'Explain how to secure REST APIs in Spring Boot.',
                type: 'MCQ',
                difficulty: (difficulty||'MEDIUM')
              }
            });
          }).catch(() => {
            res.json({
              feedback: {
                candidateAnswer: String(userAnswer||'').trim().toUpperCase(),
                correctAnswer: correct || '',
                score: (correct && String(userAnswer||'').trim().toUpperCase() === correct) ? 100 : 0,
                explanation: '',
                mistakes: '',
                example: ''
              },
              newQuestion: {
                question: 'Explain how to secure REST APIs in Spring Boot.',
                type: 'MCQ',
                difficulty: (difficulty||'MEDIUM')
              }
            });
          });
        }
      }).catch(() => res.status(502).json({ message: 'AI evaluation failed' }));
    } else {
      const schema = `{"feedback":{"candidateAnswer":string,"correctAnswer":string,"score":number,"explanation":string,"mistakes":string,"example":string},"newQuestion":{"question":string,"type":string,"difficulty":string}}`;
      const sysE = 'Return ONLY JSON per schema. Provide a real 3-5 line correct answer; technical explanation; mistake analysis; concise example.';
      const promptE = `Role: ${saved.role || ''}\nType: SUBJECTIVE\nDifficulty: ${(difficulty||'MEDIUM')}\nQuestion:\n${questionText}\nCandidate Answer:\n${userAnswer}\nEvaluate and respond per schema.`;
      grokJson(sysE, promptE, schema, 0.2).then(parsed => {
        if (parsed && parsed.feedback && parsed.newQuestion) {
          res.json(parsed);
        } else {
          res.status(502).json({ message: 'AI evaluation failed to produce JSON' });
        }
      }).catch(() => res.status(502).json({ message: 'AI evaluation failed' }));
    }
  } else {
    const m = (mode && mode.trim()) ? mode.trim().toUpperCase() : 'SUBJECTIVE';
    if (m === 'MCQ') {
      const key = typeof req.body.questionId === 'number' ? req.body.questionId : null;
      const correct = key ? mcqCorrect.get(key) : null;
      const ua = String(userAnswer || '').trim().toUpperCase();
      const isCorrect = !!correct && ua === correct;
      const explanation = 'Correct option aligns with the API/library behavior and constraints.';
      const mistake = isCorrect ? '' : 'Chosen option does not satisfy required properties.';
      const example = 'Short example demonstrating the correct behavior.';
      res.json({
        feedback: {
          candidateAnswer: ua,
          correctAnswer: correct || '',
          score: isCorrect ? 100 : 0,
          explanation,
          mistakes: mistake,
          example
        },
        newQuestion: {
          question: 'Explain how to secure REST APIs in Spring Boot.',
          type: m,
          difficulty: (difficulty||'MEDIUM')
        }
      });
    } else {
      const ans = String(userAnswer||'').trim();
      const hasContent = ans.length > 20;
      const score = hasContent ? 60 : 30;
      res.json({
        feedback: {
          candidateAnswer: ans,
          correctAnswer: 'Concise 3-5 line ideal answer covering core concepts.',
          score,
          explanation: 'Provide role-specific fundamentals and justify choices.',
          mistakes: hasContent ? 'Missing specifics and trade-offs.' : 'Answer too brief; lacks key points.',
          example: 'Outline a short, concrete scenario.'
        },
        newQuestion: {
          question: 'Explain how to secure REST APIs in Spring Boot.',
          type: m,
          difficulty: (difficulty||'MEDIUM')
        }
      });
    }
  }
});

app.post('/api/interview/coding/problem', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const topic = (req.body && req.body.topic && req.body.topic.trim()) ? req.body.topic.trim() : (saved.role || 'General');
  if (AI_KEY) {
    const sysC = 'Return ONLY plain text for a single Java coding problem suitable for backend engineers. Include constraints and 2-3 sample test cases briefly.';
    const promptC = `Role: ${topic}\nGenerate one problem. No solution. Keep concise and practical.`;
    grokChat(sysC, promptC, 0.2).then(content => {
      if (content && content.trim().length > 0) res.json({ problem: content.trim() });
      else res.json({ problem: 'Implement an LRU cache supporting get/put in O(1).' });
    }).catch(() => res.json({ problem: 'Implement an LRU cache supporting get/put in O(1).' }));
  } else {
    function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
    const BANK = {
      General: [
        'Implement an LRU cache supporting get/put in O(1).',
        'Given integers, return all unique pairs summing to target.',
        'Implement a least frequently used (LFU) cache.',
        'Check if a binary tree is height-balanced.',
        'Design a thread-safe bounded queue with blocking operations.',
      ],
      'Java Backend Developer': [
        'Design a service that aggregates results from two REST APIs with retries.',
        'Implement pagination logic returning page info and items for given page.',
        'Parse logs to compute request latency percentiles (P50/P90/P99).',
      ],
      'Java Full Stack Developer': [
        'Implement a simple router resolving paths to handlers.',
        'Build a diff function that compares two JSON objects.',
      ],
      'Senior Java Developer': [
        'Design a concurrent scheduler executing tasks with priorities.',
        'Implement a rate limiter with sliding window and multi-thread safety.',
      ],
    };
    const problem = pick(BANK[topic] || BANK['General']);
    res.json({ problem });
  }
});

app.post('/api/interview/evaluate-code', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const { code, problemStatement, executionOutput } = req.body || {};
  if (AI_KEY) {
    const sysEC = 'Return ONLY JSON. Schema: {"feedback":{"candidateCode":string,"score":number,"explanation":string,"algorithmAnalysis":string,"improvementSuggestions":string},"newProblem":{"title":string,"description":string,"difficulty":string,"topics":string[]}}. Use technical reasoning and include time/space complexity and edge cases.';
    const promptEC = `Role: Java Backend Developer\nProblem: ${problemStatement}\nCandidate Code:\n${code}\nProgram output:\n${executionOutput || ''}\nEvaluate correctness; provide analysis and suggestions; then generate a new backend-aligned problem. Output strictly matching the schema.`;
    grokChat(sysEC, promptEC, 0.2).then(content => {
      let parsed = parseJsonSafe(content);
      if (!parsed || typeof parsed !== 'object') parsed = null;
      if (parsed && parsed.feedback && parsed.newProblem) {
        res.json(parsed);
      } else {
        const fb = {
          candidateCode: String(code||''),
          score: 60,
          explanation: 'Assess algorithm correctness and structure.',
          algorithmAnalysis: 'Time: O(n); Space: O(n) typical; verify edge cases.',
          improvementSuggestions: 'Use appropriate data structures; handle constraints and errors.'
        };
        const np = {
          title: 'Design a rate limiter (sliding window)',
          description: 'Implement a thread-safe rate limiter that allows N requests per user per rolling T seconds.',
          difficulty: 'MEDIUM',
          topics: ['Concurrency','Data Structures']
        };
        res.json({ feedback: fb, newProblem: np });
      }
    }).catch(() => {
      const fb = {
        candidateCode: String(code||''),
        score: 60,
        explanation: 'Assess algorithm correctness and structure.',
        algorithmAnalysis: 'Time: O(n); Space: O(n) typical; verify edge cases.',
        improvementSuggestions: 'Use appropriate data structures; handle constraints and errors.'
      };
      const np = {
        title: 'Design a rate limiter (sliding window)',
        description: 'Implement a thread-safe rate limiter that allows N requests per user per rolling T seconds.',
        difficulty: 'MEDIUM',
        topics: ['Concurrency','Data Structures']
      };
      res.json({ feedback: fb, newProblem: np });
    });
  } else {
    const ok = code && code.includes('class');
    const fb = {
      candidateCode: String(code||''),
      score: ok ? 70 : 40,
      explanation: ok ? 'Implementation compiles; verify correctness paths.' : 'Missing basic structure and operations.',
      algorithmAnalysis: 'Analyze time/space and edge cases.',
      improvementSuggestions: 'Structure code clearly; add tests and handle edge cases.'
    };
    const np = {
      title: 'Implement LRU Cache',
      description: 'Design an LRU cache supporting get/put in O(1) using appropriate data structures.',
      difficulty: 'MEDIUM',
      topics: ['Data Structures','Algorithms']
    };
    res.json({ feedback: fb, newProblem: np });
  }
});

app.post('/api/code/execute', async (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const code = (req.body && req.body.code) ? String(req.body.code) : '';
  if (!code.trim()) return res.status(400).json({ message: 'Code is required' });
  const cid = process.env.JD_CLIENT_ID || process.env.JDOODLE_CLIENT_ID || process.env.JDoodle_CLIENT_ID || '';
  const csec = process.env.JD_CLIENT_SECRET || process.env.JDOODLE_CLIENT_SECRET || process.env.JDoodle_CLIENT_SECRET || '';
  if (!cid || !csec) {
    return res.json({ output: '', error: 'JDoodle credentials not configured' });
  }
  try {
    const resp = await fetchFn('https://api.jdoodle.com/v1/execute', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        clientId: cid,
        clientSecret: csec,
        script: code,
        language: 'java',
        versionIndex: '4'
      })
    });
    const data = await resp.json();
    const output = String(data.output || '').trim();
    const error = String(data.error || data.cpuTime || '').trim();
    res.json({ output, error });
  } catch (e) {
    res.json({ output: '', error: String(e.message || 'Execution error') });
  }
});
app.post('/api/interview/sessions/:sessionId/end', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const sessionId = Number(req.params.sessionId);
  const s = sessions.find(x => x.id === sessionId && x.userId === saved.id);
  if (!s) return res.status(404).json({ message: 'Session not found' });
  s.endedAt = new Date().toISOString();
  res.json({ message: 'Ended' });
});

app.get('/api/interview/sessions/:sessionId', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const sessionId = Number(req.params.sessionId);
  const s = sessions.find(x => x.id === sessionId && x.userId === saved.id);
  if (!s) return res.status(404).json({ message: 'Session not found' });
  res.json(s);
});

app.get('/api/interview/history', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const limit = Number(req.query.limit || 20);
  const items = sessions.filter(x => x.userId === saved.id).slice(-limit).reverse();
  res.json(items);
});

// dev error handler
app.use((err, req, res, next) => {
  console.error(err);
  res.status(err.status || 500).json({ message: err.message || 'Server error', stack: err.stack });
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Dev API listening on ${PORT}`));
