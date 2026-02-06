import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import { api } from '../api/client';

export default function InterviewSimple(){
  const [sessionId, setSessionId] = useState(null);
  const [question, setQuestion] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const start = async () => {
    setLoading(true);
    try{
      const res = await api('POST','/interview/start');
      setSessionId(res.sessionId || res.sessionId);
      // fetch first question
      const q = await api('POST', `/interview/${res.sessionId}/next`, { difficulty: 'MEDIUM' });
      setQuestion(q.question);
    }catch(e){
      alert('Failed to start interview');
    }finally{setLoading(false);}    
  };

  const next = async () => {
    if (!sessionId) return;
    setLoading(true);
    try{
      const q = await api('POST', `/interview/${sessionId}/next`, { difficulty: 'MEDIUM' });
      setQuestion(q.question);
    }catch(e){ alert('Failed to get next question'); }
    finally{ setLoading(false); }
  };

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">Live AI Interview</h1>
      {!sessionId ? (
        <button onClick={start} className="px-4 py-2 bg-blue-600 text-white rounded" disabled={loading}>{loading? 'Starting...':'Start Interview'}</button>
      ) : (
        <div>
          <div className="bg-white p-4 rounded shadow mb-4">
            <pre className="whitespace-pre-wrap">{question}</pre>
          </div>
          <div className="flex space-x-2">
            <button onClick={next} className="px-4 py-2 bg-blue-600 text-white rounded" disabled={loading}>Next Question</button>
            <button onClick={() => navigate('/dashboard')} className="px-4 py-2 border rounded">Back to Dashboard</button>
          </div>
        </div>
      )}
    </div>
  );
}
