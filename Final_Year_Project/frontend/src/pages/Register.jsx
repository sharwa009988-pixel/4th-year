import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authApi } from "../api/client";
import { useAuth } from "../context/AuthContext";

export default function Register() {
  const [email, setEmail] = useState("");
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    const cleanPassword = password.trim();
    const cleanConfirm = confirmPassword.trim();

    /* ======================
       PASSWORD MATCH CHECK
    ====================== */
    if (cleanPassword !== cleanConfirm) {
      setError("Passwords do not match");
      return;
    }

    /* ======================
       STRONG PASSWORD CHECK
    ====================== */
    const passwordRegex =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{6,}$/;

    if (!passwordRegex.test(cleanPassword)) {
      setError(
        "Password must include uppercase, lowercase, number & special character"
      );
      return;
    }

    setLoading(true);

    try {
      const res = await authApi.register(
        name.trim(),
        email.trim(),
        cleanPassword,
        cleanConfirm
      );

      /* ======================
         LOGIN AFTER REGISTER
      ====================== */
      const userObj = {
        email: res.email,
        name: res.name,
        userId: res.userId,
      };

      login(userObj, res.token);
      navigate("/select-role");

    } catch (err) {
      setError(err.message || "Registration failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-surface-950 via-surface-900 to-surface-950 px-4">
      <div className="w-full max-w-md">

        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-white">Create account</h1>
          <p className="text-slate-400 mt-1">
            Register to start interview preparation
          </p>
        </div>

        <form
          onSubmit={handleSubmit}
          className="bg-surface-900/60 border border-slate-800 rounded-xl p-6 shadow-xl"
        >
          {error && (
            <div className="mb-4 p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">
              {error}
            </div>
          )}

          {/* NAME */}
          <label className="block text-sm font-medium text-slate-300 mb-1">
            Name
          </label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="w-full px-4 py-2.5 rounded-lg bg-surface-800 border border-slate-700 text-white mb-4"
            placeholder="Your full name"
            required
          />

          {/* EMAIL */}
          <label className="block text-sm font-medium text-slate-300 mb-1">
            Email
          </label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full px-4 py-2.5 rounded-lg bg-surface-800 border border-slate-700 text-white mb-4"
            placeholder="you@example.com"
            required
          />

          {/* PASSWORD */}
          <label className="block text-sm font-medium text-slate-300 mb-1">
            Password
          </label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="w-full px-4 py-2.5 rounded-lg bg-surface-800 border border-slate-700 text-white mb-2"
            placeholder="Enter password"
            required
          />

          <p className="text-xs text-slate-400 mb-4">
            Must include uppercase, lowercase, number & special character.
          </p>

          {/* CONFIRM PASSWORD */}
          <label className="block text-sm font-medium text-slate-300 mb-1">
            Confirm Password
          </label>
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            className="w-full px-4 py-2.5 rounded-lg bg-surface-800 border border-slate-700 text-white mb-5"
            placeholder="Repeat password"
            required
          />

          {/* SUBMIT */}
          <button
            type="submit"
            disabled={loading}
            className="w-full py-2.5 rounded-lg bg-primary-600 hover:bg-primary-500 text-white font-medium disabled:opacity-50"
          >
            {loading ? "Creating account..." : "Register"}
          </button>
        </form>

        <p className="text-center text-slate-400 text-sm mt-4">
          Already have an account?{" "}
          <Link to="/login" className="text-primary-400 hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
