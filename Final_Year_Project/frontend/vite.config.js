import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        // Backend runs on 8081 by default; allow overriding via BACKEND_PORT env var
        // This helps local runs when the backend is started on a different port (e.g., 8082).
        target: `http://localhost:${process.env.BACKEND_PORT || '8082'}`,
        changeOrigin: true,
      },
    },
  },
});
