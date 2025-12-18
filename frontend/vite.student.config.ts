import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

export default defineConfig({
  plugins: [react()],

  resolve: {
    alias: { 
      '@': path.resolve(__dirname, 'src'),
    },
  },

  server: {
    port: 3001,
  },
  
  build: {
    rollupOptions: {
      input: path.resolve(__dirname, 'student-dev/index.html'),
    },
    outDir: path.resolve(__dirname, 'dist-student'),
  },
});
