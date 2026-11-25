/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { viteSingleFile } from 'vite-plugin-singlefile';

// Modo "demo": app inteiro num único HTML, com os dados sintéticos capturados da API
// (src/demo/dados.json). Serve para abrir no celular sem backend.
export default defineConfig(({ mode }) => ({
  plugins: [react(), ...(mode === 'demo' ? [viteSingleFile()] : [])],
  base: './',
  build: { outDir: mode === 'demo' ? 'dist-demo' : 'dist' },
  server: {
    host: true,
    proxy: { '/v1': 'http://localhost:8080' },
  },
  preview: { host: true, port: 4173 },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/teste/setup.ts'],
    include: ['src/**/*.test.{ts,tsx}'],
    css: false,
  },
}));
