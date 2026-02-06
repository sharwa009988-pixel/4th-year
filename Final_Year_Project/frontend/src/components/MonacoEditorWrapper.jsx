import { Editor } from '@monaco-editor/react';
import { useState } from 'react';

const MonacoEditorWrapper = ({ code, onChange, height = '400px' }) => {
  const [editorTheme, setEditorTheme] = useState('vs-dark');

  return (
    <div className="border rounded-lg overflow-hidden">
      <div className="bg-gray-800 px-4 py-2 flex justify-between items-center">
        <span className="text-white text-sm font-medium">Java Code Editor</span>
        <select
          value={editorTheme}
          onChange={(e) => setEditorTheme(e.target.value)}
          className="bg-gray-700 text-white px-2 py-1 rounded text-sm"
        >
          <option value="vs-dark">Dark</option>
          <option value="vs">Light</option>
        </select>
      </div>
      <Editor
        height={height}
        defaultLanguage="java"
        value={code}
        onChange={onChange}
        theme={editorTheme}
        options={{
          minimap: { enabled: false },
          fontSize: 14,
          wordWrap: 'on',
          automaticLayout: true,
          scrollBeyondLastLine: false,
        }}
      />
    </div>
  );
};

export default MonacoEditorWrapper;
