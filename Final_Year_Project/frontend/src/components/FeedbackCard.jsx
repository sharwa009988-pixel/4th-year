import { CheckCircle, XCircle, Lightbulb, BookOpen } from 'lucide-react';

const FeedbackCard = ({ feedback, score, explanation, suggestions, isCorrect }) => {
  const getScoreColor = (score) => {
    if (score >= 8) return 'text-green-600 bg-green-100';
    if (score >= 6) return 'text-yellow-600 bg-yellow-100';
    return 'text-red-600 bg-red-100';
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 mt-6 border-l-4 border-blue-500">
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center space-x-2">
          {isCorrect ? (
            <CheckCircle className="w-6 h-6 text-green-600" />
          ) : (
            <XCircle className="w-6 h-6 text-red-600" />
          )}
          <h3 className="text-lg font-semibold text-gray-800">AI Feedback</h3>
        </div>
        {score !== null && score !== undefined && (
          <div className={`px-4 py-2 rounded-full font-bold ${getScoreColor(score)}`}>
            Score: {score.toFixed(1)}/10
          </div>
        )}
      </div>

      {feedback && (
        <div className="mb-4">
          <p className="text-gray-700 whitespace-pre-wrap">{feedback}</p>
        </div>
      )}

      {explanation && (
        <div className="mb-4 p-4 bg-blue-50 rounded-md">
          <div className="flex items-center space-x-2 mb-2">
            <BookOpen className="w-5 h-5 text-blue-600" />
            <h4 className="font-semibold text-blue-900">Explanation</h4>
          </div>
          <p className="text-blue-800 whitespace-pre-wrap">{explanation}</p>
        </div>
      )}

      {suggestions && (
        <div className="p-4 bg-yellow-50 rounded-md">
          <div className="flex items-center space-x-2 mb-2">
            <Lightbulb className="w-5 h-5 text-yellow-600" />
            <h4 className="font-semibold text-yellow-900">Suggestions</h4>
          </div>
          <p className="text-yellow-800 whitespace-pre-wrap">{suggestions}</p>
        </div>
      )}
    </div>
  );
};

export default FeedbackCard;
