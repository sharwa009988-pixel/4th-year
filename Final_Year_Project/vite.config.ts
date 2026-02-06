import { defineConfig } from 'vite';
import axios from 'axios';
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const api = axios.create({
  baseURL: '/api',
  withCredentials: true
});

api.interceptors.response.use(
  res => res,
  err => {
    const msg = err.response?.data?.message || err.message || 'Network error';
    return Promise.reject(new Error(msg));
  }
);

export default api;

export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8081',
        changeOrigin: true,
        secure: false
      }
    }
  }
});

export default function Register() {
  const [form, setForm] = useState({ name: '', email: '', password: '', confirmPassword: '' });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const onChange = e => setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

  const onSubmit = async (e) => {
    e.preventDefault(); // prevent native reload
    setError(null);
    if (form.password !== form.confirmPassword) return setError('Passwords do not match');

    try {
      setLoading(true);
      await api.post('/auth/register', form);
      navigate('/login'); // only navigate on success
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={onSubmit}>
      {/* ...inputs with name attributes and value={form.xxx} onChange={onChange}... */}
      {error && <div className="error">{error}</div>}
      <button type="submit" disabled={loading}>{loading ? 'Registering…' : 'Register'}</button>
    </form>
  );
}