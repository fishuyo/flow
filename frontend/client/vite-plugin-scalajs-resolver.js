import { readdirSync, statSync, existsSync } from 'fs'
import { join, resolve } from 'path'

/**
 * Vite plugin to dynamically resolve Scala.js output directory
 * Finds the Scala.js build output regardless of Scala version or build mode (fastopt/opt)
 */
export function scalaJSResolver(options = {}) {
  const { cwd = process.cwd(), projectName = 'client' } = options
  
  let resolvedPath = null
  
  function findScalaJSOutput() {
    // Always re-scan in dev mode to handle fastopt/opt switching
    if (process.env.NODE_ENV === 'production' && resolvedPath && existsSync(resolvedPath)) {
      return resolvedPath
    }
    
    // Look for target/scala-*/{projectName}-{mode}/main.js
    // cwd is the project root (../../ from vite.config.js)
    // Scala.js output is at: frontend/client/target/scala-*/client-*/
    const targetDir = join(cwd, 'frontend', projectName, 'target')
    
    if (!existsSync(targetDir)) {
      // In dev mode, this is OK - Scala.js might not be built yet
      if (process.env.NODE_ENV !== 'production') {
        return null
      }
      console.warn(`[scalaJSResolver] target directory not found at: ${targetDir}`)
      return null
    }
    
    // Find scala-* directories
    let scalaDirs
    try {
      scalaDirs = readdirSync(targetDir)
        .filter(name => name.startsWith('scala-'))
        .map(name => join(targetDir, name))
        .filter(path => {
          try {
            return statSync(path).isDirectory()
          } catch {
            return false
          }
        })
    } catch (e) {
      console.warn(`[scalaJSResolver] Error reading target directory: ${e.message}`)
      return null
    }
    
    if (scalaDirs.length === 0) {
      // In dev mode, this is OK
      if (process.env.NODE_ENV !== 'production') {
        return null
      }
      console.warn('[scalaJSResolver] No scala-* directories found in target')
      return null
    }
    
    // Try to find the project output directory
    for (const scalaDir of scalaDirs) {
      let projectDirs
      try {
        projectDirs = readdirSync(scalaDir)
          .filter(name => name.startsWith(`${projectName}-`))
          .map(name => join(scalaDir, name))
          .filter(path => {
            try {
              return statSync(path).isDirectory()
            } catch {
              return false
            }
          })
      } catch (e) {
        console.warn(`[scalaJSResolver] Error reading scala directory ${scalaDir}: ${e.message}`)
        continue
      }
      
      // Prefer fastopt in dev mode, opt in production
      const isDev = process.env.NODE_ENV !== 'production'
      const optDir = projectDirs.find(d => d.endsWith('-opt'))
      const fastoptDir = projectDirs.find(d => d.endsWith('-fastopt'))
      
      const chosenDir = isDev ? (fastoptDir || optDir) : (optDir || fastoptDir)
      
      if (chosenDir) {
        const mainJsPath = join(chosenDir, 'main.js')
        if (existsSync(mainJsPath)) {
          resolvedPath = chosenDir
          console.log(`[scalaJSResolver] Found Scala.js output: ${resolvedPath}`)
          return resolvedPath
        } else {
          console.warn(`[scalaJSResolver] Directory found but main.js missing: ${mainJsPath}`)
        }
      }
    }
    
    // In dev mode, it's OK if we don't find it yet
    if (process.env.NODE_ENV !== 'production') {
      return null
    }
    
    console.warn('[scalaJSResolver] Could not find Scala.js output directory')
    return null
  }
  
  return {
    name: 'scalajs-resolver',
    configResolved(config) {
      // Resolve path when config is resolved
      const outputDir = findScalaJSOutput()
      if (outputDir) {
        // Store in config for use in resolveId
        config.scalaJSOutputDir = outputDir
      }
    },
    resolveId(id) {
      // Handle scalajs:main.js import
      if (id === 'scalajs:main.js') {
        const outputDir = findScalaJSOutput()
        if (outputDir) {
          const mainJsPath = join(outputDir, 'main.js')
          return mainJsPath
        }
        return null
      }
      return null
    },
    configureServer(server) {
      // Expose resolved path to other plugins/config
      server.scalaJSOutputDir = findScalaJSOutput()
    }
  }
}
