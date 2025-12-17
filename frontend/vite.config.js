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
    host: '0.0.0.0', // 允许局域网访问
    port: 3000,
    // 允许其他设备访问（禁用host检查）
    strictPort: true,
    cors: true,
    proxy: {
      '/api': {
        // 使用环境变量或回退到 localhost
        target: process.env.VITE_API_BASE_URL || 'http://localhost:8080',
        changeOrigin: true
      },
      '/media': {
        target: process.env.VITE_API_BASE_URL || 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: process.env.VITE_WS_BASE_URL || 'ws://localhost:8080',
        ws: true,
        changeOrigin: true
      }
    }
  }
})