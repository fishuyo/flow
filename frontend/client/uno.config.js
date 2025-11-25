import { defineConfig } from 'unocss'
import presetWind from '@unocss/preset-wind3'

export default defineConfig({
  // extractors: [
  //   {
  //     extractor: (code) => {
  //       console.log("UnoCSS scanning:", code.slice(0, 500));  // Log the first 500 chars
  //       return [...code.matchAll(/cls := "([\w- ]+)"/g)].flatMap((match) => match[1].split(" "));
  //     },
  //     extensions: ["scala", "js"],
  //   },
  // ],
  content: {
    pipeline: {
      include: [
        // the default
        ///\.(vue|svelte|[jt]sx|mdx?|astro|elm|php|phtml|html)($|\?)/,
        // include js/ts files
        //'src/**/*.{js,ts,scala}',
        //"src/main/scala/**/*.scala",  // Scan source Scala files
        "target/scala-3.3.5/**/*.js"  // Scan compiled Scala.js output
    
      ],
      // exclude files
      // exclude: []
    },
  },
  presets: [
    presetWind(),
  ],
})