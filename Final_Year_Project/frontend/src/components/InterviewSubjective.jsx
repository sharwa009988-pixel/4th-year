import React, { useState, useCallback } from 'react';
import { interviewApi } from '../api/client';

export default function InterviewSubjective({ sessionId, mode, topic, difficulty, onEnd, onBack }) {
  const [question, setQuestion] = useState('');
  const [questionId, setQuestionId] = useState(null);
  const [answer, setAnswer] = useState('');
  const [feedback, setFeedback] = useState('');
  const [score, setScore] = useState(null);
  const [isCorrect, setIsCorrect] = useState(null);
  const [correctAnswer, setCorrectAnswer] = useState('');
  const [reason, setReason] = useState('');
  const [loadingQuestion, setLoadingQuestion] = useState(false);
  const [loadingEval, setLoadingEval] = useState(false);
  const [error, setError] = useState('');

  const loadQuestion = useCallback(async () => {
    setError('');
    setFeedback('');
    setAnswer('');
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
    if (!question.trim() || !answer.trim()) return;
    setError('');
    setLoadingEval(true);
    try {
      const res = await interviewApi.evaluate(questionId, question, answer, sessionId, topic, difficulty);
      setFeedback(res.feedback || '');
      setScore(res.score ?? null);
      setIsCorrect(typeof res.isCorrect === 'boolean' ? res.isCorrect : null);
      setCorrectAnswer(res.correctAnswer || '');
      setReason(res.reason || '');
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
        {question ? (
          <p className="text-slate-200 whitespace-pre-wrap mb-4">{question}</p>
        ) : (
          <p className="text-slate-500 italic">Click &quot;New question&quot; to generate a role-specific question.</p>
        )}
        <label className="block text-sm font-medium text-slate-300 mb-2">Your answer</label>
        <textarea
          value={answer}
          onChange={(e) => setAnswer(e.target.value)}
          rows={5}
          className="w-full px-4 py-2 rounded-lg bg-surface-900 border border-slate-700 text-white placeholder-slate-500 focus:border-primary-500 resize-y"
          placeholder="Type your answer..."
        />
        <button
          type="button"
          onClick={submitAnswer}
          disabled={!answer.trim() || loadingEval}
          className="mt-4 px-4 py-2 rounded-lg bg-primary-600 text-white font-medium disabled:opacity-50"
        >
          {loadingEval ? 'Evaluating...' : 'Submit for feedback'}
        </button>
      </div>
      {feedback && (
        <div className="bg-surface-800/80 border border-slate-700 rounded-xl p-6">
          <h3 className="text-lg font-medium text-primary-300 mb-2">AI Feedback</h3>
          {isCorrect != null && (
            <p className={`mb-2 text-sm font-semibold ${isCorrect ? 'text-green-400' : 'text-red-400'}`}>
              {isCorrect ? 'Correct' : 'Incorrect'}
            </p>
          )}
          {score != null && (
            <p className="mb-2 text-sm text-slate-300">Score: {Math.round(score * 10) / 10}</p>
          )}
          {correctAnswer && (
            <p className="mb-1 text-sm text-slate-300">Correct answer: <span className="font-semibold text-white">{correctAnswer}</span></p>
          )}
          {reason && (
            <p className="mb-2 text-sm text-slate-300">Reason: <span className="text-slate-200">{reason}</span></p>
          )}
          <p className="text-slate-200 whitespace-pre-wrap">{feedback}</p>
        </div>
      )}
    </div>
  );
}
