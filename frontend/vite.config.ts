import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // In dev, forward /api/* to Spring Boot so the browser only talks to one origin (no CORS needed locally).
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
