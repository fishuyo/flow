import { defineConfig } from 'vite'
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";
import UnoCSS from 'unocss/vite'
import { copyFileSync, readdirSync, statSync } from 'fs'
import { join } from 'path'

// Custom plugin to copy Scala.js output files
// function copyScalaJSFiles() {
//   return {
//     name: 'copy-scalajs-files',
//     generateBundle() {
//       const scalaJSDir = 'target/scala-3.3.3/client-opt'
//       const outDir = 'dist'
      
//       try {
//         const files = readdirSync(scalaJSDir)
//         files.forEach(file => {
//           if (file.endsWith('.js') || file.endsWith('.js.map')) {
//             const sourcePath = join(scalaJSDir, file)
//             const destPath = join(outDir, file)
//             copyFileSync(sourcePath, destPath)
//             console.log(`Copied ${file} to dist/`)
//           }
//         })
//       } catch (error) {
//         console.warn('Could not copy Scala.js files:', error.message)
//       }
//     }
//   }
// }

export default defineConfig({
  plugins: [
    scalaJSPlugin({cwd: '../../'}), 
    UnoCSS(),
    // copyScalaJSFiles()
  ],
  build: {
    outDir: 'dist',
    // emptyOutDir: true,
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