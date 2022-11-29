// Comment to get more information during initialization
// logLevel := Level.Warn

ThisBuild / libraryDependencySchemes ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
  // "org.scala-js" %% "scalajs-dom" % VersionScheme.Always
)

// Resolvers
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// Sbt plugins
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.18") //"2.6.13")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.11.0")

// addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.2")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")

addSbtPlugin("com.vmunier" % "sbt-web-scalajs" % "1.2.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.1.2")

addSbtPlugin("org.scalablytyped.converter" % "sbt-converter" % "1.0.0-beta40")

// addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")