import React, { useState } from 'react';
import { interviewApi, codeApi } from '../api/client';
import InterviewSubjective from '../components/InterviewSubjective';
import InterviewCoding from '../components/InterviewCoding';

const MODES = [
  { value: 'MCQ', label: 'MCQ' },
  { value: 'SUBJECTIVE', label: 'Subjective' },
  { value: 'CODING', label: 'Coding' },
  { value: 'FULL_MOCK', label: 'Full Mock (role-specific)' },
];

const TOPICS = ['Spring Boot', 'REST API', 'JPA/Hibernate', 'Security', 'Microservices', 'React basics', 'Java Core', 'SQL', 'Design Patterns', ''];

const DIFFICULTIES = ['EASY', 'MEDIUM', 'HARD'];

export default function Interview() {
  const [mode, setMode] = useState('SUBJECTIVE');
  const [topic, setTopic] = useState('');
  const [difficulty, setDifficulty] = useState('MEDIUM');
  const [session, setSession] = useState(null);
  const [error, setError] = useState('');

  const startSession = async () => {
    setError('');
    try {
      const s = await interviewApi.start(mode, topic || null, difficulty);
      setSession(s);
    } catch (e) {
      setError(e.message || 'Failed to start session');
    }
  };

  const endSession = async () => {
    if (!session?.id) return;
    try {
      await interviewApi.endSession(session.id);
      setSession(null);
    } catch (e) {
      setError(e.message);
    }
  };

  if (session) {
    if (mode === 'CODING') {
      return (
        <InterviewCoding
          sessionId={session.id}
          onEnd={endSession}
          onBack={() => setSession(null)}
        />
      );
    }
    return (
      <InterviewSubjective
        sessionId={session.id}
        mode={mode}
        topic={topic || null}
        difficulty={difficulty}
        onEnd={endSession}
        onBack={() => setSession(null)}
      />
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold text-white mb-6">Start role-specific interview</h1>
      {error && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}
      <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-6 max-w-lg space-y-4">
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-1">Mode</label>
          <select
            value={mode}
            onChange={(e) => setMode(e.target.value)}
            className="w-full px-4 py-2.5 rounded-lg bg-surface-900 border border-slate-700 text-white focus:border-primary-500"
          >
            {MODES.map((m) => (
              <option key={m.value} value={m.value}>{m.label}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-1">Topic (optional)</label>
          <select
            value={topic}
            onChange={(e) => setTopic(e.target.value)}
            className="w-full px-4 py-2.5 rounded-lg bg-surface-900 border border-slate-700 text-white focus:border-primary-500"
          >
            <option value="">Any</option>
            {TOPICS.filter(Boolean).map((t) => (
              <option key={t} value={t}>{t}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-300 mb-1">Difficulty</label>
          <select
            value={difficulty}
            onChange={(e) => setDifficulty(e.target.value)}
            className="w-full px-4 py-2.5 rounded-lg bg-surface-900 border border-slate-700 text-white focus:border-primary-500"
          >
            {DIFFICULTIES.map((d) => (
              <option key={d} value={d}>{d}</option>
            ))}
          </select>
        </div>
        <button
          type="button"
          onClick={startSession}
          className="w-full py-2.5 rounded-lg bg-primary-600 hover:bg-primary-500 text-white font-medium"
        >
          Start interview
        </button>
      </div>
    </div>
  );
}
