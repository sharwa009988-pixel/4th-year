import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { dashboardApi } from '../api/client';
import { useAuth } from '../context/AuthContext';

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');
  const { user } = useAuth();

  useEffect(() => {
    dashboardApi.stats()
      .then(setStats)
      .catch((e) => setError(e.message));
  }, []);

  if (error) {
    return (
      <div className="rounded-lg bg-red-500/10 text-red-400 p-4">{error}</div>
    );
  }

  const strengths = stats?.strengthsWeaknesses?.strengths ?? [];
  const weaknesses = stats?.strengthsWeaknesses?.weaknesses ?? [];
  const topicScores = stats?.topicScores ?? [];

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-bold text-white">Dashboard</h1>
          <p className="text-primary-300 mt-1">Target role: <strong>{user?.targetRole || stats?.targetRole}</strong></p>
        </div>
        <Link
          to="/interview"
          className="inline-flex items-center gap-2 px-4 py-2.5 rounded-lg bg-primary-600 hover:bg-primary-500 text-white font-medium transition-colors"
        >
          Start Role-Specific Interview
        </Link>
      </div>

      {!stats && !error && (
        <div className="animate-pulse text-slate-400">Loading stats...</div>
      )}

      {stats && (
        <div className="space-y-6">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-4">
              <p className="text-slate-400 text-sm">Total sessions</p>
              <p className="text-2xl font-bold text-white mt-1">{stats.totalSessions}</p>
            </div>
            <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-4">
              <p className="text-slate-400 text-sm">Sessions (this role)</p>
              <p className="text-2xl font-bold text-white mt-1">{stats.sessionsThisRole}</p>
            </div>
            <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-4">
              <p className="text-slate-400 text-sm">Average score</p>
              <p className="text-2xl font-bold text-primary-400 mt-1">{stats.averageScore?.toFixed(1) ?? 0}%</p>
            </div>
          </div>

          {topicScores.length > 0 && (
            <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-4">
              <h2 className="text-lg font-semibold text-white mb-3">Topic coverage</h2>
              <ul className="space-y-2">
                {topicScores.map((t) => (
                  <li key={t.topic} className="flex justify-between items-center text-sm">
                    <span className="text-slate-300">{t.topic}</span>
                    <span className="text-primary-400 font-medium">{t.averageScore?.toFixed(0) ?? 0}%</span>
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-4">
              <h2 className="text-lg font-semibold text-white mb-2">Strengths</h2>
              {strengths.length === 0 ? (
                <p className="text-slate-500 text-sm">Complete more sessions to see strengths.</p>
              ) : (
                <ul className="list-disc list-inside text-slate-300 text-sm space-y-1">
                  {strengths.map((s) => (
                    <li key={s}>{s}</li>
                  ))}
                </ul>
              )}
            </div>
            <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-4">
              <h2 className="text-lg font-semibold text-white mb-2">Areas to improve</h2>
              {weaknesses.length === 0 ? (
                <p className="text-slate-500 text-sm">Keep practicing to identify weak topics.</p>
              ) : (
                <ul className="list-disc list-inside text-slate-300 text-sm space-y-1">
                  {weaknesses.map((w) => (
                    <li key={w}>{w}</li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
