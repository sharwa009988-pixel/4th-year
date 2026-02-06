const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

const users = [];

app.post('/api/auth/register', (req, res, next) => {
  try {
    const { name, email, password, confirmPassword } = req.body;
    if (!name || !email || !password) return res.status(400).json({ message: 'Missing fields' });
    if (password !== confirmPassword) return res.status(400).json({ message: 'Passwords do not match' });
    if (users.find(u => u.email === email)) return res.status(409).json({ message: 'Email already exists' });

    const user = { id: users.length + 1, name, email };
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
    const user = { id: saved.id, name: saved.name, email: saved.email };
    res.json({
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

app.get('/api/public/health', (req, res) => {
  res.json({ status: 'ok' });
});

// dev error handler
app.use((err, req, res, next) => {
  console.error(err);
  res.status(err.status || 500).json({ message: err.message || 'Server error', stack: err.stack });
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`Dev API listening on ${PORT}`));
