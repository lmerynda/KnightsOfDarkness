import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        host: true, // This allows Vite to listen on all network interfaces
        port: 3000, // Ensure this matches the exposed port
        watch: {
            usePolling: true,
            interval: 100, // adjust as needed
          },
        // the page asks its own origin for /api, as in production, so any device that opens it reaches the game server
        proxy: {
            '/api': {
                target: process.env.GAME_SERVER ?? 'http://localhost:8080',
                changeOrigin: true,
                rewrite: path => path.replace(/^\/api/, ''),
            },
        },
    },
})
