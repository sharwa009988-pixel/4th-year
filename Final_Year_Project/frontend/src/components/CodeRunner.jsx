import { useState } from 'react';
import api from '../services/api';
import toast from 'react-hot-toast';
import { Play, Loader2 } from 'lucide-react';

const CodeRunner = ({ code, onOutputChange, stdin = '' }) => {
  const [input, setInput] = useState(stdin);
  const [output, setOutput] = useState('');
  const [loading, setLoading] = useState(false);
  const [language, setLanguage] = useState('JAVA'); // JAVA, PYTHON, CPP, C

  const handleRunCode = async () => {
    if (!code || code.trim() === '') {
      toast.error('Please write some code first');
      return;
    }

    setLoading(true);
    try {
      const response = await api.post('/code/execute', {
        code,
        stdin: input,
        language,
      });

      const result = response.data;
      const outputText = result.error
        ? `Error: ${result.error}\n${result.output || ''}`
        : result.output || 'No output';

      setOutput(outputText);
      if (onOutputChange) {
        onOutputChange(outputText, result);
      }

      if (result.error) {
        toast.error('Code execution failed');
      } else {
        toast.success('Code executed successfully');
      }
    } catch (error) {
      toast.error('Failed to execute code');
      setOutput('Error: Failed to execute code');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Language
          </label>
          <select
            value={language}
            onChange={(e) => setLanguage(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
          >
            <option value="JAVA">Java</option>
            <option value="PYTHON">Python</option>
            <option value="CPP">C++ (17)</option>
            <option value="C">C</option>
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Standard Input (stdin)
          </label>
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Enter input values (one per line)..."
            className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
            rows="3"
          />
        </div>
      </div>
      <button
        onClick={handleRunCode}
        disabled={loading}
        className="flex items-center space-x-2 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition"
      >
        {loading ? (
          <>
            <Loader2 className="w-5 h-5 animate-spin" />
            <span>Running...</span>
          </>
        ) : (
          <>
            <Play className="w-5 h-5" />
            <span>Run Code</span>
          </>
        )}
      </button>
      {output && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Output
          </label>
          <pre className="bg-gray-900 text-green-400 p-4 rounded-md overflow-auto max-h-64">
            {output}
          </pre>
        </div>
      )}
    </div>
  );
};

export default CodeRunner;
