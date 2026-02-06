import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import Navbar from '../components/Navbar';
import QuestionRenderer from '../components/QuestionRenderer';
import MonacoEditorWrapper from '../components/MonacoEditorWrapper';
import CodeRunner from '../components/CodeRunner';
import FeedbackCard from '../components/FeedbackCard';
import toast from 'react-hot-toast';
import { Send, ArrowRight, ArrowLeft, CheckCircle } from 'lucide-react';

const InterviewSession = () => {
  const { sessionId } = useParams();
  const navigate = useNavigate();
  const [session, setSession] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [answer, setAnswer] = useState('');
  const [code, setCode] = useState('');
  const [feedback, setFeedback] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [codeOutput, setCodeOutput] = useState('');

  useEffect(() => {
    fetchSession();
  }, [sessionId]);

  const fetchSession = async () => {
    try {
      const response = await api.get(`/interviews/sessions/${sessionId}`);
      const data = response.data;
      setSession(data);
      setQuestions(data.questions || []);
      if (data.questions && data.questions.length > 0) {
        loadQuestionData(0);
      }
      setLoading(false);
    } catch (error) {
      console.error('Failed to load session:', error);
      toast.error('Failed to load session');
      setLoading(false);
    }
  };

  const loadQuestionData = (index) => {
    const question = questions[index];
    if (question.userAnswer) {
      setAnswer(question.userAnswer);
      if (question.codeInput) {
        setCode(question.codeInput);
      }
    } else {
      setAnswer('');
      setCode('');
    }
    setFeedback(null);
    setCodeOutput('');
  };

  useEffect(() => {
    if (questions.length > 0) {
      loadQuestionData(currentQuestionIndex);
    }
  }, [currentQuestionIndex, questions]);

  const handleSubmitAnswer = async () => {
    if (!answer.trim() && !code.trim()) {
      toast.error('Please provide an answer');
      return;
    }

    setSubmitting(true);
    try {
      const currentQuestion = questions[currentQuestionIndex];
      const response = await api.post(`/interviews/${sessionId}/answer`, {
        questionId: currentQuestion.questionId,
        answer: answer,
        code: code || undefined,
        stdin: '',
      });

      const feedbackData = response.data;
      setFeedback(feedbackData);

      // Update local question state
      const updatedQuestions = [...questions];
      updatedQuestions[currentQuestionIndex] = {
        ...updatedQuestions[currentQuestionIndex],
        userAnswer: answer,
        score: feedbackData.score,
      };
      setQuestions(updatedQuestions);

      toast.success('Answer submitted successfully!');
    } catch (error) {
      toast.error('Failed to submit answer');
    } finally {
      setSubmitting(false);
    }
  };

  const handleNext = () => {
    if (currentQuestionIndex < questions.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
    }
  };

  const handlePrevious = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(currentQuestionIndex - 1);
    }
  };

  const handleEndSession = async () => {
    try {
      await api.post(`/interviews/sessions/${sessionId}/end`);
      toast.success('Session ended');
      navigate(`/results/${sessionId}`);
    } catch (error) {
      toast.error('Failed to end session');
    }
  };

  if (loading || !questions || questions.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex items-center justify-center min-h-[80vh]">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4 mx-auto"></div>
            <p className="text-gray-600">Generating questions...</p>
            <p className="text-sm text-gray-500 mt-2">This may take a moment</p>
          </div>
        </div>
      </div>
    );
  }

  const currentQuestion = questions[currentQuestionIndex];
  const isCodingQuestion = currentQuestion.type === 'CODING';
  const isAnswered = currentQuestion.userAnswer || currentQuestion.codeInput;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Progress Bar */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold text-gray-900">
              Question {currentQuestionIndex + 1} of {questions.length}
            </h2>
            <div className="flex items-center space-x-2">
              {isAnswered && <CheckCircle className="w-5 h-5 text-green-600" />}
              <span className="text-sm text-gray-600">
                {questions.filter((q) => q.userAnswer || q.codeInput).length} answered
              </span>
            </div>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div
              className="bg-blue-600 h-2 rounded-full transition-all"
              style={{
                width: `${((currentQuestionIndex + 1) / questions.length) * 100}%`,
              }}
            ></div>
          </div>
        </div>

        {/* Question */}
        <QuestionRenderer question={currentQuestion.questionText} type={currentQuestion.type} />

        {/* Answer Input */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          {isCodingQuestion ? (
            <div className="space-y-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Write your Java code:
                </label>
                <MonacoEditorWrapper code={code} onChange={setCode} height="400px" />
              </div>
              <CodeRunner
                code={code}
                onOutputChange={(output) => setCodeOutput(output)}
              />
            </div>
          ) : (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                Your Answer:
              </label>
              <textarea
                value={answer}
                onChange={(e) => setAnswer(e.target.value)}
                rows={8}
                className="w-full px-4 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                placeholder="Type your answer here..."
              />
            </div>
          )}
        </div>

        {/* Feedback */}
        {feedback && <FeedbackCard {...feedback} />}

        {/* Navigation */}
        <div className="flex items-center justify-between bg-white rounded-lg shadow-md p-6">
          <button
            onClick={handlePrevious}
            disabled={currentQuestionIndex === 0}
            className="flex items-center space-x-2 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <ArrowLeft className="w-5 h-5" />
            <span>Previous</span>
          </button>

          <div className="flex items-center space-x-4">
            {!isAnswered && (
              <button
                onClick={handleSubmitAnswer}
                disabled={submitting || (!answer.trim() && !code.trim())}
                className="flex items-center space-x-2 bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
              >
                <Send className="w-5 h-5" />
                <span>{submitting ? 'Submitting...' : 'Submit Answer'}</span>
              </button>
            )}

            {currentQuestionIndex === questions.length - 1 ? (
              <button
                onClick={handleEndSession}
                className="bg-green-600 text-white px-6 py-2 rounded-md hover:bg-green-700"
              >
                End Session
              </button>
            ) : (
              <button
                onClick={handleNext}
                disabled={currentQuestionIndex === questions.length - 1}
                className="flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <span>Next</span>
                <ArrowRight className="w-5 h-5" />
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default InterviewSession;
