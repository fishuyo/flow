name := """flow-control"""
organization := "com.fishuyo"

version := "0.1-SNAPSHOT"

val scalaV = "2.11.11"

// lazy val root = (project in file(".")).enablePlugins(PlayScala)

// scalaVersion := "2.11.11"

// libraryDependencies += guice
// libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test
// libraryDependencies += "hid" %% "hid" % "0.1-SNAPSHOT"
// libraryDependencies += "interface_server" %% "interface_server" % "0.1-SNAPSHOT"

// Adds additional packages into Twirl
// TwirlKeys.templateImports += "com.fishuyo.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.fishuyo.binders._"



lazy val server = (project in file("server")).settings(
  scalaVersion := scalaV,
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.1",
    guice,
    specs2 % Test,
    "interface_server" %% "interface_server" % "0.1-SNAPSHOT",
    "org.webjars" %% "webjars-play" % "2.6.0",
    "org.webjars" % "jquery" % "3.2.1",
    "org.webjars" % "materializecss" % "0.99.0",
    "org.webjars.npm" % "codemirror" % "5.27.4"
  )
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  // EclipseKeys.preTasks := Seq(compile in Compile)
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)


lazy val client = (project in file("client")).settings(
  scalaVersion := scalaV,
  scalaJSUseMainModuleInitializer := true,
  resolvers += Resolver.jcenterRepo,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.thoughtworks.binding" %%% "dom" % "latest.release",
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  ),
  jsDependencies ++= Seq(
    "org.webjars" % "jquery" % "3.2.1" / "jquery.js" minified "jquery.min.js",
    "org.webjars" % "materializecss" % "0.99.0" / "materialize.js" minified "materialize.min.js" dependsOn "jquery.js",
    "org.webjars.npm" % "codemirror" % "5.27.4" / "lib/codemirror.js",
    "org.webjars.npm" % "codemirror" % "5.27.4" / "mode/clike/clike.js" dependsOn "lib/codemirror.js",
    "org.webjars.npm" % "codemirror" % "5.27.4" / "keymap/sublime.js" dependsOn "lib/codemirror.js"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)


lazy val shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
  settings(scalaVersion := scalaV).
  jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value



