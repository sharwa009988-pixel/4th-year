import React from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const navClass = ({ isActive }) =>
  `px-3 py-2 rounded-lg text-sm font-medium transition-colors ${isActive ? 'bg-primary-600 text-white' : 'text-slate-300 hover:bg-surface-800 hover:text-white'}`;

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen flex flex-col">
      <header className="border-b border-slate-800 bg-surface-900/80 backdrop-blur">
        <div className="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between">
          <nav className="flex items-center gap-2">
            <NavLink to="/dashboard" className="font-semibold text-primary-400">InterviewPrep</NavLink>
            <NavLink to="/dashboard" className={navClass}>Dashboard</NavLink>
            <NavLink to="/interview" className={navClass}>Interview</NavLink>
            <NavLink to="/history" className={navClass}>History</NavLink>
            <NavLink to="/profile" className={navClass}>Profile</NavLink>
          </nav>
          <div className="flex items-center gap-3">
            <span className="text-slate-400 text-sm">{user?.email}</span>
            <span className="text-primary-300 text-xs bg-primary-900/50 px-2 py-1 rounded">{user?.role || 'No role'}</span>
            <button type="button" onClick={handleLogout} className="text-slate-400 hover:text-white text-sm">Logout</button>
          </div>
        </div>
      </header>
      <main className="flex-1 max-w-6xl w-full mx-auto px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
