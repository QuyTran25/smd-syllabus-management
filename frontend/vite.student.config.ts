import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  // ✅ GIỮ root là frontend để /src/... trỏ đúng
  root: path.resolve(__dirname),

  plugins: [
    react(),
    // ✅ Rewrite "/" -> "/student-dev/index.html" để port 3001 vào là ra Student
    {
      name: 'student-entry-rewrite',
      configureServer(server) {
        server.middlewares.use((req, _res, next) => {
          if (req.url === '/' || req.url === '/index.html') {
            req.url = '/student-dev/index.html';
          }
          next();
        });
      },
    },
  ],

  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') },
  },

  server: {
    port: 3001,
    open: true,
  },
});
