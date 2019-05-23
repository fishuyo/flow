// Comment to get more information during initialization
// logLevel := Level.Warn

// Resolvers
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// Sbt plugins
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.18") //"2.6.13")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.27")
// addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.0.0-M3") //0.6.23")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "0.4.0")

// addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.0-M1")

addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.0.8-0.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")

// addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")