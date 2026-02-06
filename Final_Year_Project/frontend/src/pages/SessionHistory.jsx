import React, { useState, useEffect } from 'react';
import { interviewApi } from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function SessionHistory() {
  const [sessions, setSessions] = useState([]);
  const [roleFilter, setRoleFilter] = useState('');
  const [loading, setLoading] = useState(true);
  const { user } = useAuth();

  useEffect(() => {
    setLoading(true);
    interviewApi.history(roleFilter || undefined)
      .then(setSessions)
      .catch(() => setSessions([]))
      .finally(() => setLoading(false));
  }, [roleFilter]);

  const formatDate = (iso) => {
    if (!iso) return '—';
    try {
      return new Date(iso).toLocaleString();
    } catch {
      return iso;
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-white mb-4">Session history</h1>
      <p className="text-slate-400 text-sm mb-4">Filter by role (optional):</p>
      <input
        type="text"
        value={roleFilter}
        onChange={(e) => setRoleFilter(e.target.value)}
        placeholder={user?.targetRole || 'e.g. Java Backend Developer'}
        className="w-full max-w-xs px-4 py-2 rounded-lg bg-surface-800 border border-slate-700 text-white placeholder-slate-500 focus:border-primary-500 focus:ring-1 focus:ring-primary-500 mb-6"
      />
      {loading && <p className="text-slate-400">Loading...</p>}
      {!loading && sessions.length === 0 && (
        <p className="text-slate-500">No sessions yet. Start an interview from the Dashboard.</p>
      )}
      {!loading && sessions.length > 0 && (
        <div className="overflow-x-auto">
          <table className="w-full text-sm text-left">
            <thead>
              <tr className="border-b border-slate-700 text-slate-400">
                <th className="py-3 pr-4">Date</th>
                <th className="py-3 pr-4">Role</th>
                <th className="py-3 pr-4">Type</th>
                <th className="py-3 pr-4">Topic</th>
                <th className="py-3 pr-4">Score</th>
                <th className="py-3">Questions</th>
              </tr>
            </thead>
            <tbody>
              {sessions.map((s) => (
                <tr key={s.id} className="border-b border-slate-800 text-slate-300">
                  <td className="py-3 pr-4">{formatDate(s.startedAt)}</td>
                  <td className="py-3 pr-4">{s.roleSnapshot || '—'}</td>
                  <td className="py-3 pr-4">{s.sessionType || '—'}</td>
                  <td className="py-3 pr-4">{s.topic || '—'}</td>
                  <td className="py-3 pr-4">{s.score != null ? s.score + '%' : '—'}</td>
                  <td className="py-3">{s.totalQuestions ?? 0}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
