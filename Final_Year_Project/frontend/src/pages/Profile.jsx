import React, { useState, useEffect } from 'react';
import { getPredefinedRoles, rolesApi } from '../api/client';
import { useAuth } from '../context/AuthContext';

const OTHER_LABEL = 'Other';

export default function Profile() {
  const [options, setOptions] = useState([]);
  const [selected, setSelected] = useState('');
  const [customRole, setCustomRole] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const { user, updateUser } = useAuth();

  useEffect(() => {
    getPredefinedRoles().then(setOptions).catch(() => setOptions([OTHER_LABEL]));
    if (user?.targetRole) {
      const inList = options.includes(user.targetRole);
      setSelected(inList ? user.targetRole : OTHER_LABEL);
      if (!inList) setCustomRole(user.targetRole);
    }
  }, [user?.targetRole]);

  useEffect(() => {
    if (!user?.targetRole) return;
    const inList = options.includes(user.targetRole);
    setSelected(inList ? user.targetRole : OTHER_LABEL);
    if (!inList) setCustomRole(user.targetRole);
  }, [options, user?.targetRole]);

  const displayRole = selected === OTHER_LABEL ? customRole.trim() : selected;
  const canSave = displayRole.length > 0 && displayRole !== user?.targetRole;

  const handleSaveRole = async (e) => {
    e.preventDefault();
    if (!canSave) return;
    setError('');
    setMessage('');
    setLoading(true);
    try {
      const updated = await rolesApi.selectRole(displayRole);
      updateUser(updated);
      setMessage('Role updated successfully.');
    } catch (err) {
      setError(err.message || 'Failed to update role');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold text-white mb-6">Profile</h1>
      <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-6 max-w-md">
        <p className="text-slate-400 text-sm mb-4">Email: <span className="text-white">{user?.email}</span></p>
        <h2 className="text-lg font-semibold text-white mb-2">Change target role</h2>
        <p className="text-slate-400 text-sm mb-4">Interview content will be tailored to the selected role.</p>
        {message && <div className="mb-4 p-3 rounded-lg bg-green-500/10 text-green-400 text-sm">{message}</div>}
        {error && <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}
        <form onSubmit={handleSaveRole}>
          <select
            value={selected}
            onChange={(e) => setSelected(e.target.value)}
            className="w-full px-4 py-2.5 rounded-lg bg-surface-900 border border-slate-700 text-white focus:border-primary-500 focus:ring-1 focus:ring-primary-500 mb-4"
          >
            <option value="">-- Choose role --</option>
            {options.map((opt) => (
              <option key={opt} value={opt}>{opt}</option>
            ))}
          </select>
          {selected === OTHER_LABEL && (
            <input
              type="text"
              value={customRole}
              onChange={(e) => setCustomRole(e.target.value)}
              placeholder="Custom role name"
              className="w-full px-4 py-2.5 rounded-lg bg-surface-900 border border-slate-700 text-white placeholder-slate-500 focus:border-primary-500 focus:ring-1 focus:ring-primary-500 mb-4"
              maxLength={255}
            />
          )}
          <button
            type="submit"
            disabled={!canSave || loading}
            className="px-4 py-2 rounded-lg bg-primary-600 hover:bg-primary-500 text-white font-medium disabled:opacity-50"
          >
            {loading ? 'Saving...' : 'Save role'}
          </button>
        </form>
      </div>
    </div>
  );
}
