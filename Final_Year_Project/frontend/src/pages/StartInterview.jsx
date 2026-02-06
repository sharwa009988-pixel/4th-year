import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import Navbar from '../components/Navbar';
import toast from 'react-hot-toast';
import { ArrowRight } from 'lucide-react';

const StartInterview = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    mode: 'MCQ',
    topic: 'Core Java',
    difficulty: 'MEDIUM',
    numberOfQuestions: 5,
    programmingLanguage: 'JAVA',
    timeLimitPerQuestionSeconds: 60,
    timeLimitOverallMinutes: 20,
  });

  const interviewTypes = [
    { value: 'MCQ', label: 'Technical (MCQ)' },
    { value: 'SUBJECTIVE', label: 'Technical (Subjective)' },
    { value: 'CODING', label: 'Technical (Coding)' },
    { value: 'BEHAVIORAL', label: 'Behavioral' },
    { value: 'FULL_MOCK', label: 'Mixed Interview' },
  ];

  const topics = [
    'Core Java',
    'Spring Boot',
    'Hibernate/JPA',
    'REST APIs',
    'Microservices',
    'SQL/Database',
    'Multithreading',
    'Collections',
    'Exception Handling',
    'React Basics',
    'Docker/Deployment',
  ];

  const difficulties = [
    { value: 'EASY', label: 'Easy' },
    { value: 'MEDIUM', label: 'Medium' },
    { value: 'HARD', label: 'Hard' },
  ];

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    setLoading(true);
    try {
      const response = await api.post('/interviews/start', formData, { timeout: 90000 });

      if (response.data?.id) {
        toast.success('Interview session started!');
        navigate(`/interview/${response.data.id}`);
      } else {
        toast.error('Session created but missing ID. Please try again.');
        setLoading(false);
      }
    } catch (error) {
      let errorMessage = 'Failed to start interview';
      if (error.code === 'ECONNABORTED') {
        errorMessage = 'Request timed out - server took too long to respond';
      } else if (error.message === 'Network Error') {
        errorMessage = 'Network error - cannot reach backend. Is it running on port 8081?';
      } else if (error.response?.data?.message) {
        errorMessage = error.response.data.message;
      } else if (error.message) {
        errorMessage = error.message;
      }
      toast.error(errorMessage);
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-white rounded-lg shadow-md p-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-6">Start Mock Interview</h1>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">Interview Type</label>
              <select
                value={formData.mode}
                onChange={(e) => setFormData({ ...formData, mode: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {interviewTypes.map((type) => (
                  <option key={type.value} value={type.value}>
                    {type.label}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Difficulty</label>
                <select
                  value={formData.difficulty}
                  onChange={(e) => setFormData({ ...formData, difficulty: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  {difficulties.map((diff) => (
                    <option key={diff.value} value={diff.value}>
                      {diff.label}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Number of Questions</label>
                <input
                  type="number"
                  min="1"
                  max="30"
                  value={formData.numberOfQuestions}
                  onChange={(e) =>
                    setFormData({ ...formData, numberOfQuestions: parseInt(e.target.value) })
                  }
                  className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Topic
              </label>
              <select
                value={formData.topic}
                onChange={(e) => setFormData({ ...formData, topic: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                {topics.map((topic) => (
                  <option key={topic} value={topic}>
                    {topic}
                  </option>
                ))}
              </select>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Time Limit per Question (sec)</label>
                <input
                  type="number"
                  min="10"
                  max="1800"
                  value={formData.timeLimitPerQuestionSeconds}
                  onChange={(e) =>
                    setFormData({ ...formData, timeLimitPerQuestionSeconds: parseInt(e.target.value) })
                  }
                  className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Overall Time Limit (min)</label>
                <input
                  type="number"
                  min="1"
                  max="240"
                  value={formData.timeLimitOverallMinutes}
                  onChange={(e) =>
                    setFormData({ ...formData, timeLimitOverallMinutes: parseInt(e.target.value) })
                  }
                  className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
            </div>

            {formData.mode === 'CODING' && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Programming Language</label>
                <select
                  value={formData.programmingLanguage}
                  onChange={(e) => setFormData({ ...formData, programmingLanguage: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="JAVA">Java</option>
                  <option value="PYTHON">Python</option>
                  <option value="CPP">C++</option>
                  <option value="C">C</option>
                </select>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center space-x-2 bg-blue-600 text-white py-3 px-6 rounded-md hover:bg-blue-700 transition disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              <span>{loading ? 'Starting Interview...' : 'Start Interview'}</span>
              {!loading && <ArrowRight className="w-5 h-5" />}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default StartInterview;
