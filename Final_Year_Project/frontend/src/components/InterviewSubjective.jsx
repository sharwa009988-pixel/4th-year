import React, { useState, useCallback } from 'react';
import { interviewApi } from '../api/client';

export default function InterviewSubjective({ sessionId, mode, topic, difficulty, onEnd, onBack }) {
  const [question, setQuestion] = useState('');
  const [questionId, setQuestionId] = useState(null);
  const [answer, setAnswer] = useState('');
  const [selectedOption, setSelectedOption] = useState('');
  const [fb, setFb] = useState(null);
  const [suggested, setSuggested] = useState(null);
  const [loadingQuestion, setLoadingQuestion] = useState(false);
  const [loadingEval, setLoadingEval] = useState(false);
  const [error, setError] = useState('');

  function parseMcq(q) {
    if (!q) return { stem: q, options: [] };
    const text = String(q).replace(/\\n/g, '\n').trim();
    const lines = text.split('\n').map(s => s.trim());
    const stem = lines[0] || text;
    const options = [];
    const regex = /(?:^|\s|\n)([A-D])[\)\.]\s*([^A-D].*?)(?=(?:\s|\n)[A-D][\)\.]|\s*$)/g;
    const flat = text.slice(stem.length);
    const matches = Array.from(flat.matchAll(regex));
    if (matches.length > 0) {
      for (const m of matches) {
        const key = m[1];
        const val = m[2].trim();
        options.push({ key, text: val });
      }
    } else {
      for (let i = 1; i < lines.length; i++) {
        const m = lines[i].match(/^([A-D])[\)\.]\s*(.*)$/);
        if (m) options.push({ key: m[1], text: m[2] });
      }
    }
    return { stem, options };
  }

  const loadQuestion = useCallback(async () => {
    setError('');
    setFb(null);
    setSuggested(null);
    setAnswer('');
    setSelectedOption('');
    setLoadingQuestion(true);
    try {
      const res = await interviewApi.generateQuestion(mode, topic, difficulty);
      setQuestion(res.question || '');
      setQuestionId(res.questionId || null);
    } catch (e) {
      setError(e.message || 'Failed to generate question');
    } finally {
      setLoadingQuestion(false);
    }
  }, [mode, topic, difficulty]);

  const submitAnswer = async () => {
    if (!question.trim()) return;
    const isMcq = String(mode).toUpperCase() === 'MCQ';
    const userAnswer = isMcq ? selectedOption.trim() : answer.trim();
    if (!userAnswer) return;
    setError('');
    setLoadingEval(true);
    try {
      const res = await interviewApi.evaluate(questionId, question, userAnswer, sessionId, mode, topic, difficulty);
      setFb(res.feedback || null);
      if (String(mode).toUpperCase() !== 'FULL_MOCK') {
        setSuggested(res.newQuestion || null);
      }
    } catch (e) {
      setError(e.message || 'Failed to evaluate');
    } finally {
      setLoadingEval(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-xl font-bold text-white">Interview — {mode}</h2>
        <div className="flex gap-2">
          <button type="button" onClick={onBack} className="px-3 py-1.5 rounded-lg border border-slate-600 text-slate-300 hover:bg-surface-800">Back</button>
          <button type="button" onClick={onEnd} className="px-3 py-1.5 rounded-lg bg-primary-600 text-white hover:bg-primary-500">End session</button>
        </div>
      </div>
      {error && <div className="p-3 rounded-lg bg-red-500/10 text-red-400 text-sm">{error}</div>}
      <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-6">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-medium text-white">Question</h3>
          <button
            type="button"
            onClick={loadQuestion}
            disabled={loadingQuestion}
            className="px-3 py-1.5 rounded-lg bg-primary-600 text-white text-sm disabled:opacity-50"
          >
            {loadingQuestion ? 'Generating...' : 'New question'}
          </button>
        </div>
        {!question ? (
          <p className="text-slate-500 italic">Click &quot;New question&quot; to generate a role-specific question.</p>
        ) : String(mode).toUpperCase() === 'MCQ' ? (
          (() => {
            const parsed = parseMcq(question);
            return (
              <div className="mb-4 space-y-3">
                <p className="text-slate-200">{parsed.stem}</p>
                <div className="space-y-2">
                  {parsed.options.map(opt => (
                    <label key={opt.key} className="flex items-center gap-2 text-slate-200">
                      <input
                        type="radio"
                        name="mcq"
                        value={opt.key}
                        checked={selectedOption === opt.key}
                        onChange={(e) => setSelectedOption(e.target.value)}
                        className="accent-primary-600"
                      />
                      <span className="font-semibold">{opt.key})</span>
                      <span>{opt.text}</span>
                    </label>
                  ))}
                </div>
              </div>
            );
          })()
        ) : (
          <p className="text-slate-200 whitespace-pre-wrap mb-4">{question}</p>
        )}
        {String(mode).toUpperCase() !== 'MCQ' && (
          <>
            <label className="block text-sm font-medium text-slate-300 mb-2">Your answer</label>
            <textarea
              value={answer}
              onChange={(e) => setAnswer(e.target.value)}
              rows={5}
              className="w-full px-4 py-2 rounded-lg bg-surface-900 border border-slate-700 text-white placeholder-slate-500 focus:border-primary-500 resize-y"
              placeholder="Type your answer..."
            />
          </>
        )}
        <button
          type="button"
          onClick={submitAnswer}
          disabled={loadingEval || (String(mode).toUpperCase() === 'MCQ' ? !selectedOption : !answer.trim())}
          className="mt-4 px-4 py-2 rounded-lg bg-primary-600 text-white font-medium disabled:opacity-50"
        >
          {loadingEval ? 'Evaluating...' : 'Submit for feedback'}
        </button>
      </div>
      {fb && (
        <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-6">
          <h3 className="text-lg font-medium text-primary-300 mb-2">AI Feedback</h3>
          <p className="text-slate-300 mb-2">Question: <span className="text-white">{question}</span></p>
          <p className="text-slate-300 mb-2">Candidate Answer: <span className="text-white">{fb.candidateAnswer}</span></p>
          {fb.correctAnswer && (
            <p className="text-slate-300 mb-2">Correct Answer: <span className="text-white">{fb.correctAnswer}</span></p>
          )}
          <div className="mt-3 space-y-1 text-slate-200">
            {fb.explanation && <p>- Explanation: {fb.explanation}</p>}
            {fb.mistakes && <p>- Mistake Analysis: {fb.mistakes}</p>}
            {fb.example && <p>- Example: <span className="whitespace-pre-wrap">{fb.example}</span></p>}
          </div>
          {typeof fb.score !== 'undefined' && (
            <p className="mt-3 text-sm text-slate-300">Score: {fb.score}</p>
          )}
        </div>
      )}
      {String(mode).toUpperCase() !== 'FULL_MOCK' && String(mode).toUpperCase() !== 'MCQ' && suggested && suggested.question && (
        <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-6">
          <h3 className="text-lg font-medium text-primary-300 mb-2">Next Question</h3>
          <p className="text-slate-200 whitespace-pre-wrap mb-2">{suggested.question}</p>
          <p className="text-slate-400 text-sm">Type: {suggested.type} • Difficulty: {suggested.difficulty}</p>
        </div>
      )}
    </div>
  );
}
