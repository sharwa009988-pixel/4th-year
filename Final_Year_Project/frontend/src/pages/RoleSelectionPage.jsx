import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPredefinedRoles, rolesApi } from '../api/client';
import { useAuth } from '../context/AuthContext';

const OTHER_LABEL = 'Other';

export default function RoleSelectionPage() {
  const [options, setOptions] = useState([]);
  const [selected, setSelected] = useState('');
  const [customRole, setCustomRole] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { user, updateUser, refreshUser } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    getPredefinedRoles()
      .then(setOptions)
      .catch(() => setOptions([OTHER_LABEL]));
  }, []);

  const displayRole = selected === OTHER_LABEL ? customRole.trim() : selected;
  const canSubmit = displayRole.length > 0;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canSubmit) return;
    setError('');
    setLoading(true);
    try {
      await rolesApi.selectRole(displayRole);
      // refresh authenticated user so `roleSelected` becomes true
      try {
        await refreshUser();
      } catch (e) {
        // fallback: update local user with role
        updateUser({ ...(user || {}), role: displayRole, roleSelected: true });
      }
      navigate('/dashboard');
    } catch (err) {
      setError(err.message || 'Failed to save role');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-surface-950 via-surface-900 to-surface-950 px-4">
      <div className="w-full max-w-lg">
        <div className="text-center mb-6">
          <h1 className="text-2xl font-bold text-white">Select your target role</h1>
          <p className="text-slate-400 mt-1">Your interview experience will be tailored to this role.</p>
        </div>
        <form onSubmit={handleSubmit} className="bg-surface-900/60 border border-slate-800 rounded-xl p-6 shadow-xl">
          {error && (
            <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>
          )}
          <label className="block text-sm font-medium text-slate-300 mb-2">Job role</label>
          <select
            value={selected}
            onChange={(e) => setSelected(e.target.value)}
            className="w-full px-4 py-2.5 rounded-lg bg-surface-800 border border-slate-700 text-white focus:border-primary-500 focus:ring-1 focus:ring-primary-500 mb-4"
          >
            <option value="">-- Choose a role --</option>
            {options.map((opt) => (
              <option key={opt} value={opt}>{opt}</option>
            ))}
          </select>
          {selected === OTHER_LABEL && (
            <div className="mb-4">
              <label className="block text-sm font-medium text-slate-300 mb-1">Custom role name</label>
              <input
                type="text"
                value={customRole}
                onChange={(e) => setCustomRole(e.target.value)}
                placeholder="e.g. Java + Kotlin Backend Developer"
                className="w-full px-4 py-2.5 rounded-lg bg-surface-800 border border-slate-700 text-white placeholder-slate-500 focus:border-primary-500 focus:ring-1 focus:ring-primary-500"
                maxLength={255}
              />
            </div>
          )}
          <button
            type="submit"
            disabled={!canSubmit || loading}
            className="w-full py-2.5 rounded-lg bg-primary-600 hover:bg-primary-500 text-white font-medium disabled:opacity-50 transition-colors"
          >
            {loading ? 'Saving...' : 'Continue to Dashboard'}
          </button>
        </form>
        <p className="text-center text-slate-500 text-sm mt-4">You can change this later in Profile.</p>
      </div>
    </div>
  );
}
