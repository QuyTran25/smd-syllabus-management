import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    dedupe: ['react', 'react-dom', '@tanstack/react-query'],
    alias: {
      '@': path.resolve(__dirname, './src'),
      react: path.resolve(__dirname, './node_modules/react'),
      'react-dom': path.resolve(__dirname, './node_modules/react-dom'),
      '@tanstack/react-query': path.resolve(__dirname, './node_modules/@tanstack/react-query'),
    },
  },
  optimizeDeps: {
    include: ['react', 'react-dom', '@tanstack/react-query'],
  },
  cacheDir: 'node_modules/.vite-admin', // Cache riêng cho admin
  server: {
    port: 3000,
    strictPort: true, // Thêm này
    hmr: {
      protocol: 'ws',
      host: 'localhost',
      port: 3000,
    },
  },
  build: {
    rollupOptions: {
      input: {
        admin: path.resolve(__dirname, 'index.html'),
        student: path.resolve(__dirname, 'index.student.html'),
      },
    },
  },
});
