import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    host: '0.0.0.0',
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://10.69.218.40:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'ws://10.69.218.40:8080',
        ws: true,
        changeOrigin: true
      }
    }
  }
})