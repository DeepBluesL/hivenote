import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // 告诉 Vite: "@" 就代表 "src" 这个文件夹
      '@': path.resolve(__dirname, 'src')
    }
  }
})