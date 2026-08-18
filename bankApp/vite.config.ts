/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
        proxy: { '/api': { target: 'https://bankapp-3cwp.onrender.com', changeOrigin: true } }
    },
  test: {
    // Components need a DOM to render into; Node alone has none
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.js',
    css: true,
  }
})