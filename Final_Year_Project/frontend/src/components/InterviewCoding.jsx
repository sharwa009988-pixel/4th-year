import React, { useState, useCallback } from 'react';
import Editor from '@monaco-editor/react';
import { interviewApi, codeApi } from '../api/client';

const DEFAULT_JAVA = `public class Main {
    public static void main(String[] args) {
        // Your code here
        System.out.println("Hello");
    }
}`;

export default function InterviewCoding({ sessionId, onEnd, onBack }) {
  const [problem, setProblem] = useState('');
  const [code, setCode] = useState(DEFAULT_JAVA);
  const [output, setOutput] = useState('');
  const [fb, setFb] = useState(null);
  const [newProb, setNewProb] = useState(null);
  const [loadingProblem, setLoadingProblem] = useState(false);
  const [loadingRun, setLoadingRun] = useState(false);
  const [loadingEval, setLoadingEval] = useState(false);
  const [error, setError] = useState('');

  const loadProblem = useCallback(async () => {
    setError('');
    setFb(null);
    setNewProb(null);
    setLoadingProblem(true);
    try {
      const res = await interviewApi.generateCodingProblem(null);
      setProblem(res.problem || '');
    } catch (e) {
      setError(e.message || 'Failed to generate problem');
    } finally {
      setLoadingProblem(false);
    }
  }, []);

  const runCode = async () => {
    setError('');
    setOutput('');
    setLoadingRun(true);
    try {
      const res = await codeApi.execute(code);
      const errRaw = res.error;
      const errStr = typeof errRaw === 'string'
        ? errRaw.trim()
        : (errRaw && errRaw.message ? String(errRaw.message).trim() : '');
      const hasError = !!errStr && errStr !== 'null' && errStr !== 'undefined';
      if (hasError) setOutput('Error: ' + errStr);
      else setOutput(res.output ?? '');
    } catch (e) {
      setError(e.message || 'Execution failed');
    } finally {
      setLoadingRun(false);
    }
  };

  const submitForFeedback = async () => {
    setError('');
    setLoadingEval(true);
    try {
      const res = await interviewApi.evaluateCode(code, problem, output, sessionId);
      setFb(res.feedback || null);
      setNewProb(res.newProblem || null);
    } catch (e) {
      setError(e.message || 'Failed to get feedback');
    } finally {
      setLoadingEval(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold text-white">Coding interview</h2>
        <div className="flex gap-2">
          <button type="button" onClick={onBack} className="px-3 py-1.5 rounded-lg border border-slate-600 text-slate-300 hover:bg-surface-800">Back</button>
          <button type="button" onClick={onEnd} className="px-3 py-1.5 rounded-lg bg-primary-600 text-white hover:bg-primary-500">End session</button>
        </div>
      </div>
      {error && <div className="p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-4">
          <div className="flex justify-between items-center mb-2">
            <h3 className="text-lg font-medium text-white">Problem</h3>
            <button
              type="button"
              onClick={loadProblem}
              disabled={loadingProblem}
              className="px-3 py-1.5 rounded-lg bg-primary-600 text-white text-sm disabled:opacity-50"
            >
              {loadingProblem ? 'Generating...' : 'New problem'}
            </button>
          </div>
          {problem ? (
            <p className="text-slate-200 whitespace-pre-wrap text-sm">{problem}</p>
          ) : (
            <p className="text-slate-500 italic text-sm">Click &quot;New problem&quot; for a role-specific Java coding task.</p>
          )}
        </div>
        <div className="bg-surface-800/80 border border-slate-700 rounded-xl overflow-hidden">
          <div className="flex justify-between items-center px-4 py-2 border-b border-slate-700">
            <h3 className="text-sm font-medium text-white">Java code</h3>
            <div className="flex gap-2">
              <button
                type="button"
                onClick={runCode}
                disabled={loadingRun}
                className="px-3 py-1 rounded bg-slate-600 text-white text-sm disabled:opacity-50"
              >
                {loadingRun ? 'Running...' : 'Run'}
              </button>
              <button
                type="button"
                onClick={submitForFeedback}
                disabled={loadingEval}
                className="px-3 py-1 rounded bg-primary-600 text-white text-sm disabled:opacity-50"
              >
                {loadingEval ? '...' : 'Get AI feedback'}
              </button>
            </div>
          </div>
          <div className="h-64">
            <Editor
              height="100%"
              defaultLanguage="java"
              value={code}
              onChange={(v) => setCode(v ?? '')}
              theme="vs-dark"
              options={{ minimap: false, fontSize: 14 }}
            />
          </div>
        </div>
      </div>
      {output && (
        <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-4">
          <h3 className="text-sm font-medium text-slate-300 mb-2">Output</h3>
          <pre className="text-slate-200 text-sm font-mono whitespace-pre-wrap">{output}</pre>
        </div>
      )}
      {fb && (
        <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-6">
          <h3 className="text-lg font-medium text-primary-300 mb-2">AI Feedback</h3>
          <p className="text-slate-300 mb-2">Candidate Code:</p>
          <pre className="text-slate-200 whitespace-pre-wrap text-sm">{fb.candidateCode}</pre>
          {typeof fb.score !== 'undefined' && (
            <p className="mb-2 text-sm text-slate-300">Score: {fb.score}</p>
          )}
          {fb.explanation && <p className="text-slate-200 mb-1">Explanation: {fb.explanation}</p>}
          {fb.algorithmAnalysis && <p className="text-slate-200 mb-1">Algorithm Analysis: {fb.algorithmAnalysis}</p>}
          {fb.improvementSuggestions && <p className="text-slate-200">Improvement Suggestions: {fb.improvementSuggestions}</p>}
        </div>
      )}
      
    </div>
  );
}
