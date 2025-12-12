import { readdirSync, statSync, existsSync } from 'fs'
import { join } from 'path'

/**
 * Vite plugin to provide Scala.js output directory to UnoCSS
 * This allows UnoCSS to scan the compiled Scala.js files for class names
 */
export function scalaJSUnoResolver(options = {}) {
  const { cwd = process.cwd(), projectName = 'client' } = options
  
  function findScalaJSOutput() {
    const targetDir = join(cwd, 'target')
    
    if (!existsSync(targetDir)) {
      return null
    }
    
    // Find scala-* directories
    const scalaDirs = readdirSync(targetDir)
      .filter(name => name.startsWith('scala-'))
      .map(name => join(targetDir, name))
      .filter(path => statSync(path).isDirectory())
    
    // Try to find the project output directory
    for (const scalaDir of scalaDirs) {
      const projectDirs = readdirSync(scalaDir)
        .filter(name => name.startsWith(`${projectName}-`))
        .map(name => join(scalaDir, name))
        .filter(path => statSync(path).isDirectory())
      
      // Prefer opt over fastopt, but accept either
      const optDir = projectDirs.find(d => d.endsWith('-opt'))
      const fastoptDir = projectDirs.find(d => d.endsWith('-fastopt'))
      
      const chosenDir = optDir || fastoptDir
      
      if (chosenDir) {
        return chosenDir
      }
    }
    
    return null
  }
  
  return {
    name: 'scalajs-uno-resolver',
    configResolved(config) {
      const outputDir = findScalaJSOutput()
      if (outputDir) {
        // Store for UnoCSS to use
        config.scalaJSOutputDir = outputDir
      }
    }
  }
}

/**
 * Get the Scala.js output directory pattern for UnoCSS content scanning
 */
export function getScalaJSPattern() {
  const cwd = process.cwd()
  const targetDir = join(cwd, 'target')
  
  if (!existsSync(targetDir)) {
    return 'target/scala-*/client-*/**/*.js'
  }
  
  // Find scala-* directories
  const scalaDirs = readdirSync(targetDir)
    .filter(name => name.startsWith('scala-'))
    .map(name => join(targetDir, name))
    .filter(path => statSync(path).isDirectory())
  
  // Build pattern from found directories
  if (scalaDirs.length > 0) {
    // Use glob pattern that matches any scala version
    return 'target/scala-*/client-*/**/*.js'
  }
  
  return null
}
