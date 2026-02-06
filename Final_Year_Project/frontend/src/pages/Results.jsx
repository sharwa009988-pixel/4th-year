import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import Navbar from '../components/Navbar';
import toast from 'react-hot-toast';
import { ArrowLeft, CheckCircle, XCircle, TrendingUp } from 'lucide-react';

const Results = () => {
  const { sessionId } = useParams();
  const navigate = useNavigate();
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSession();
  }, [sessionId]);

  const fetchSession = async () => {
    try {
      const response = await api.get(`/interviews/sessions/${sessionId}`);
      setSession(response.data);
    } catch (error) {
      toast.error('Failed to load session results');
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex items-center justify-center min-h-[80vh]">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
        </div>
      </div>
    );
  }

  if (!session) {
    return null;
  }

  const getScoreColor = (score) => {
    if (score >= 8) return 'text-green-600';
    if (score >= 6) return 'text-yellow-600';
    return 'text-red-600';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6">
          <button
            onClick={() => navigate('/dashboard')}
            className="flex items-center space-x-2 text-gray-600 hover:text-gray-900 mb-4"
          >
            <ArrowLeft className="w-5 h-5" />
            <span>Back to Dashboard</span>
          </button>
        </div>

        {/* Summary Card */}
        <div className="bg-white rounded-lg shadow-md p-8 mb-8">
          <div className="text-center mb-6">
            <h1 className="text-3xl font-bold text-gray-900 mb-2">Session Results</h1>
            <p className="text-gray-600">{session.topic} • {session.type}</p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-6">
            <div className="text-center p-6 bg-blue-50 rounded-lg">
              <TrendingUp className="w-12 h-12 text-blue-600 mx-auto mb-2" />
              <p className="text-3xl font-bold text-gray-900">
                {session.totalScore?.toFixed(1) || 'N/A'}
              </p>
              <p className="text-gray-600">Overall Score</p>
            </div>

            <div className="text-center p-6 bg-green-50 rounded-lg">
              <CheckCircle className="w-12 h-12 text-green-600 mx-auto mb-2" />
              <p className="text-3xl font-bold text-gray-900">
                {session.answeredQuestions}/{session.totalQuestions}
              </p>
              <p className="text-gray-600">Questions Answered</p>
            </div>

            <div className="text-center p-6 bg-purple-50 rounded-lg">
              <p className="text-3xl font-bold text-gray-900">
                {session.startTime
                  ? new Date(session.startTime).toLocaleDateString()
                  : 'N/A'}
              </p>
              <p className="text-gray-600">Date</p>
            </div>
          </div>
        </div>

        {/* Questions List */}
        <div className="bg-white rounded-lg shadow-md">
          <div className="p-6 border-b">
            <h2 className="text-xl font-semibold text-gray-900">Question Details</h2>
          </div>
          <div className="divide-y">
            {session.questions?.map((question, index) => (
              <div key={question.questionId} className="p-6">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex-1">
                    <div className="flex items-center space-x-2 mb-2">
                      <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-medium">
                        Question {index + 1}
                      </span>
                      <span className="px-3 py-1 bg-gray-100 text-gray-800 rounded-full text-sm">
                        {question.type}
                      </span>
                    </div>
                    <p className="text-gray-800 mb-4 whitespace-pre-wrap">
                      {question.questionText}
                    </p>
                  </div>
                  {question.score !== null && question.score !== undefined && (
                    <div
                      className={`text-2xl font-bold ${getScoreColor(question.score)}`}
                    >
                      {question.score.toFixed(1)}/10
                    </div>
                  )}
                </div>
                {question.score !== null && question.score !== undefined && (
                  <div className="mt-4">
                    {question.score >= 7 ? (
                      <div className="flex items-center space-x-2 text-green-600">
                        <CheckCircle className="w-5 h-5" />
                        <span className="font-medium">Good Answer</span>
                      </div>
                    ) : (
                      <div className="flex items-center space-x-2 text-red-600">
                        <XCircle className="w-5 h-5" />
                        <span className="font-medium">Needs Improvement</span>
                      </div>
                    )}
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Results;
