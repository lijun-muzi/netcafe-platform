import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const backendTarget = 'http://localhost:8080'
const apiPrefixes = ['/auth', '/admins', '/users', '/machines', '/sessions', '/stats', '/system', '/audit', '/machine-templates', '/recharges']

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: Object.fromEntries(
      apiPrefixes.map((prefix) => [
        prefix,
        {
          target: backendTarget,
          changeOrigin: true
        }
      ])
    )
  }
})
