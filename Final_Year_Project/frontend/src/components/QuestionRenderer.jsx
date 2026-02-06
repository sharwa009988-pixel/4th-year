import { FileText, Code, MessageSquare, HelpCircle } from 'lucide-react';

const QuestionRenderer = ({ question, type }) => {
  const getIcon = () => {
    switch (type) {
      case 'MCQ':
        return <HelpCircle className="w-6 h-6 text-blue-600" />;
      case 'CODING':
        return <Code className="w-6 h-6 text-green-600" />;
      case 'BEHAVIORAL':
        return <MessageSquare className="w-6 h-6 text-purple-600" />;
      default:
        return <FileText className="w-6 h-6 text-gray-600" />;
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-md p-6 mb-6">
      <div className="flex items-start space-x-4">
        <div className="flex-shrink-0">{getIcon()}</div>
        <div className="flex-1">
          <div className="flex items-center justify-between mb-4">
            <span className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-medium">
              {type}
            </span>
          </div>
          <div className="prose max-w-none">
            <pre className="whitespace-pre-wrap font-sans text-gray-800">
              {question}
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuestionRenderer;
