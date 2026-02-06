const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const users = [];
const sessions = [];
let questionSeq = 1;

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
    console.log('Registered:', user);
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
  res.json({ message: 'Role selected', role: saved.role });
});

app.get('/api/dashboard/stats', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const stats = {
    targetRole: saved.role || '',
    totalSessions: 0,
    sessionsThisRole: 0,
    averageScore: 0,
    recentTopics: [],
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
  const baseTopic = topic && topic.trim() ? topic.trim() : (saved.role || 'General');
  const question = `(${mode || 'SUBJECTIVE'}) [${difficulty || 'MEDIUM'}] ${baseTopic}: Describe key concepts and best practices.`;
  res.json({ questionId: id, question });
});

app.post('/api/interview/evaluate', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const { userAnswer } = req.body || {};
  const hasContent = userAnswer && userAnswer.trim().length > 20;
  const score = hasContent ? 70 : 40;
  res.json({
    feedback: hasContent ? 'Good structure. Add more specifics and examples.' : 'Answer is too brief. Elaborate key points and examples.',
    score,
    isCorrect: hasContent,
    correctAnswer: 'Explain fundamentals, typical patterns, and pitfalls with examples.',
    reason: 'Assessed clarity, completeness, and relevance to the topic.'
  });
});

app.post('/api/interview/coding/problem', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  res.json({ problem: 'Implement an LRU cache with get/put in O(1).' });
});

app.post('/api/interview/evaluate-code', (req, res) => {
  const saved = getUserFromAuth(req);
  if (!saved) return res.status(401).json({ message: 'Unauthorized' });
  const { code } = req.body || {};
  const ok = code && code.includes('class') && code.includes('get') && code.includes('put');
  res.json({
    feedback: ok ? 'Implementation looks reasonable. Verify eviction and capacity handling.' : 'Add class structure and both get/put operations.',
    score: ok ? 75 : 45,
    isCorrect: ok,
    correctAnswer: 'Use HashMap + Doubly Linked List to achieve O(1).',
    reason: 'Checks data structures used and operation complexity.'
  });
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
