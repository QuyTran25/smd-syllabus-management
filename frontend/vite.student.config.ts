import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';
import fs from 'fs';

// Plugin để serve student index.html thay vì index.html mặc định
function studentHtmlPlugin() {
  return {
    name: 'student-html',
    enforce: 'pre' as const,
    configureServer(server) {
      return () => {
        server.middlewares.use((req, res, next) => {
          if (req.url === '/' || req.url === '/index.html' || req.url?.endsWith('.html')) {
            const studentHtmlPath = path.resolve(__dirname, 'index.student.html');
            let html = fs.readFileSync(studentHtmlPath, 'utf-8');

            server
              .transformIndexHtml(req.url || '/', html, req.originalUrl)
              .then((transformed) => {
                res.statusCode = 200;
                res.setHeader('Content-Type', 'text/html');
                res.end(transformed);
              })
              .catch(next);
            return;
          }
          next();
        });
      };
    },
  };
}

export default defineConfig({
  plugins: [studentHtmlPlugin(), react()],
  resolve: {
    dedupe: ['react', 'react-dom', '@tanstack/react-query'],
    alias: {
      '@': path.resolve(__dirname, 'src'),
      react: path.resolve(__dirname, './node_modules/react'),
      'react-dom': path.resolve(__dirname, './node_modules/react-dom'),
      '@tanstack/react-query': path.resolve(__dirname, './node_modules/@tanstack/react-query'),
    },
  },
  optimizeDeps: {
    include: ['react', 'react-dom', '@tanstack/react-query'],
  },
  cacheDir: 'node_modules/.vite-student', // Cache riêng cho student
  server: {
    port: 3001,
    strictPort: true, // Thêm này
    hmr: {
      protocol: 'ws',
      host: 'localhost',
      port: 3001,
    },
  },
  build: {
    rollupOptions: {
      input: path.resolve(__dirname, 'index.student.html'),
    },
    outDir: path.resolve(__dirname, 'dist-student'),
  },
});
