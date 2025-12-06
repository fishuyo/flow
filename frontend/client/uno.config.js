import { defineConfig } from 'unocss'
import presetWind from '@unocss/preset-wind3'

export default defineConfig({
  content: {
    pipeline: {
      include: [
        // the default
        ///\.(vue|svelte|[jt]sx|mdx?|astro|elm|php|phtml|html)($|\?)/,
        // Dynamically scan compiled Scala.js output regardless of Scala version or build mode
        // This pattern matches: target/scala-*/client-*/**/*.js
        "target/scala-*/client-*/**/*.js"
      ],
    },
  },
  presets: [
    presetWind(),
  ],
})