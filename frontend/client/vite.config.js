import { defineConfig } from 'vite'
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";
import UnoCSS from 'unocss/vite'

export default defineConfig({
  plugins: [
    scalaJSPlugin({cwd: '../../', projectID: 'client'}), 
    UnoCSS(),
  ],
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    rollupOptions: {
      input: {
        main: 'index.html'
      }
    },
  },
  // Configure Vite to work with Scala.js
  optimizeDeps: {
    exclude: ['scalajs:main.js']
  },
  resolve: {
    alias: {
      // Allow importing scalajs:main.js which will be resolved by our plugin
      'scalajs:main.js': 'scalajs:main.js'
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
      }
    }
  }
}) 