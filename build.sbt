import sbtcrossproject.{crossProject, CrossType}

name := """flow"""
organization := "com.fishuyo"

version := "0.1-SNAPSHOT"

val scalaV = "2.12.5" //"2.11.11"

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
  resolvers += Resolver.sonatypeRepo("snapshots"),
  //updateOptions := updateOptions.value.withLatestSnapshots(false),
  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.1.1",
    guice,
    specs2 % Test,
  
    "net.java.dev.jna" % "jna" % "4.0.0",
    "org.hid4java" % "hid4java" % "0.5.0",
    "org.spire-math" %% "spire" % "0.13.0",

    "com.twitter" %% "util-eval" % "6.43.0",
    "org.scala-lang" % "scala-reflect" % scalaV,
    "org.scala-lang" % "scala-compiler" % scalaV,
    "org.scala-lang" % "scala-library" % scalaV,

    "org.scodec" %% "scodec-core" % "1.10.3",

    "de.sciss" %% "scalaosc" % "1.1.6",
    "de.sciss" %% "scalaaudiofile" % "1.4.7",
    // "com.fishuyo.seer" %% "interface_server" % "0.1-SNAPSHOT",
    "com.fishuyo.seer" %% "core" % "0.1-SNAPSHOT",
    //"script" %% "script" % "0.1-SNAPSHOT",

    "org.webjars" %% "webjars-play" % "2.6.0",
    "org.webjars" % "jquery" % "3.2.1",
    "org.webjars" % "materializecss" % "0.99.0",
    "org.webjars.npm" % "codemirror" % "5.27.4"
  )
  // Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
  // EclipseKeys.preTasks := Seq(compile in Compile)
).settings(
  if(System.getProperty("os.name").contains("Mac")) Seq(
    envVars := Map("DYLD_LIBRARY_PATH" -> file("lib").getAbsolutePath)
  ) else Seq()
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)

lazy val client = (project in file("client")).settings(
  scalaVersion := scalaV,
  scalaJSUseMainModuleInitializer := true,
  resolvers += Resolver.jcenterRepo,
  resolvers += Resolver.bintrayRepo("denigma", "denigma-releases"),

  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.thoughtworks.binding" %%% "dom" % "latest.release",
    "org.querki" %%% "jquery-facade" % "1.0",
    // "com.definitelyscala" % "scala-js-materializecss_sjs0.6_2.11" % "1.0.0",
    "org.denigma" %%% "codemirror-facade" % "5.13.2-0.8",
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  ),
  jsDependencies ++= Seq(
    "org.webjars" % "jquery" % "3.2.1" / "jquery.js" minified "jquery.min.js",
    "org.webjars" % "materializecss" % "0.99.0" / "materialize.js" minified "materialize.min.js" dependsOn "jquery.js",
    "org.webjars.npm" % "codemirror" % "5.27.4" / "lib/codemirror.js",
    "org.webjars.npm" % "codemirror" % "5.27.4" / "mode/clike/clike.js" dependsOn "lib/codemirror.js",
    "org.webjars.npm" % "codemirror" % "5.27.4" / "keymap/sublime.js" dependsOn "lib/codemirror.js",
    "org.webjars.npm" % "codemirror" % "5.27.4" / "addon/comment/comment.js" dependsOn "lib/codemirror.js",
    "org.webjars.npm" % "codemirror" % "5.27.4" / "addon/search/searchcursor.js" dependsOn "lib/codemirror.js"
  )
).enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(sharedJs)


lazy val shared = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("shared")).settings(
    scalaVersion := scalaV,
    libraryDependencies ++= Seq(
      // "com.typesafe.play" %% "play-json" % "2.6.1",
      "org.julienrf" %%% "play-json-derived-codecs" % "4.0.0"
    )
  ).jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value



