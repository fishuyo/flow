import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}


name := "flow"
organization := "com.fishuyo"

version := "0.1-SNAPSHOT"

val scalaV = "2.13.10"

lazy val server = project.in(file("server")).settings(
  scalaVersion := scalaV,
  // scalacOptions ++= Seq("-Ymacro-annotations"),
  scalaJSProjects := Seq(client),
  pipelineStages in Assets := Seq(scalaJSPipeline),
  pipelineStages := Seq(digest, gzip),
  // triggers scalaJSPipeline when using compile or continuous compilation
  compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
  resolvers += Resolver.sonatypeRepo("snapshots"),
  //updateOptions := updateOptions.value.withLatestSnapshots(false),

  // resolvers += Resolver.bintrayRepo("hseeberger", "maven"),
  libraryDependencies ++= List(
    "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
    "com.typesafe.akka" %% "akka-pki" % "2.6.20",
    "com.typesafe.akka" %% "akka-remote" % "2.6.20",
    "com.typesafe.akka" %% "akka-http" % "10.2.10"
  ),
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % "0.14.3"),

  libraryDependencies ++= Seq(
    "com.vmunier" %% "scalajs-scripts" % "1.2.0",
    "com.google.inject"            % "guice"                % "5.1.0",
    "com.google.inject.extensions" % "guice-assistedinject" % "5.1.0",
    guice,
    // specs2 % Test,
  
    // "net.java.dev.jna" % "jna" % "4.0.0",
    // "org.hid4java" % "hid4java" % "0.5.0",
    "org.typelevel" %% "spire" % "0.18.0",

    // "com.twitter" %% "util-eval" % "6.43.0",
    "org.scala-lang" % "scala-reflect" % scalaV,
    "org.scala-lang" % "scala-compiler" % scalaV,
    "org.scala-lang" % "scala-library" % scalaV,

    // "org.scodec" %% "scodec-core" % "2.2.0",

    "de.sciss" %% "scalaosc" % "1.3.1",
    "de.sciss" %% "audiofile" % "2.4.2",
    // "com.fishuyo.seer" %% "interface_server" % "0.1-SNAPSHOT",
    "seer" %% "math" % "0.1-SNAPSHOT",
    "seer" %% "actor" % "0.1-SNAPSHOT",
    // "script" %% "script" % "0.1-SNAPSHOT",
    // "phasespace" %% "core" % "0.1-SNAPSHOT",
    // "phasespace" % "native" % "0.1-SNAPSHOT",

    // "org.lwjgl" % "lwjgl-openvr" % "3.2.0",
    // "org.lwjgl" % "lwjgl-openvr" % "3.2.0" classifier "natives-macos",
    // "org.lwjgl" % "lwjgl-openvr" % "3.2.0" classifier "natives-windows",
    // "org.lwjgl" % "lwjgl" % "3.2.0",
    // "org.lwjgl" % "lwjgl" % "3.2.0" classifier "natives-macos",
    // "org.lwjgl" % "lwjgl" % "3.2.0" classifier "natives-windows",

    // "org.webjars" %% "webjars-play" % "2.6.0",
    // "org.webjars" % "jquery" % "3.2.1",
    // "org.webjars" % "materializecss" % "0.99.0",
    // "org.webjars.npm" % "codemirror" % "5.27.4"
  )
).settings(
  if(System.getProperty("os.name").contains("Mac")) Seq(
    envVars := Map("DYLD_LIBRARY_PATH" -> file("lib").getAbsolutePath)
  ) else Seq()
).enablePlugins(PlayScala).
  dependsOn(sharedJvm)


lazy val client = project.in(file("client")).settings(
  scalaVersion := scalaV,
  scalaJSUseMainModuleInitializer := true,
  resolvers += Resolver.jcenterRepo,
  resolvers += Resolver.bintrayRepo("denigma", "denigma-releases"),

  scalacOptions ++= Seq("-Ymacro-annotations"),

  libraryDependencies ++= Seq(
    // "me.shadaj" %%% "slinky-web" % "0.7.2"

    // "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    "com.yang-bo" %%% "html" % "2.0.0"
    // "com.yang-bo" %%% "html" % "0.0.0+444-fe1d54da+20221129-1201"
    // "org.lrng.binding" %%% "html" % "1.0.3+56-51cfb24a+20221129-1135" //"latest.release"
    // "org.lrng.binding" %%% "html" % "latest.release"
    // "com.thoughtworks.binding" %%% "dom" % "latest.release",
    // "org.querki" %%% "jquery-facade" % "1.0",
    // "com.definitelyscala" % "scala-js-materializecss_sjs0.6_2.11" % "1.0.0",
    // "org.denigma" %%% "codemirror-facade" % "5.13.2-0.8",
    // compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  ),
  // jsDependencies ++= Seq(
  //   "org.webjars" % "jquery" % "3.2.1" / "jquery.js" minified "jquery.min.js",
  //   "org.webjars" % "materializecss" % "0.99.0" / "materialize.js" minified "materialize.min.js" dependsOn "jquery.js",
  //   "org.webjars.npm" % "codemirror" % "5.27.4" / "lib/codemirror.js",
  //   "org.webjars.npm" % "codemirror" % "5.27.4" / "mode/clike/clike.js" dependsOn "lib/codemirror.js",
  //   "org.webjars.npm" % "codemirror" % "5.27.4" / "keymap/sublime.js" dependsOn "lib/codemirror.js",
  //   "org.webjars.npm" % "codemirror" % "5.27.4" / "addon/comment/comment.js" dependsOn "lib/codemirror.js",
  //   "org.webjars.npm" % "codemirror" % "5.27.4" / "addon/search/searchcursor.js" dependsOn "lib/codemirror.js"
  // ),
  Compile / npmDependencies ++= Seq(
    "@types/jquery" -> "3.5.14",
    "@types/codemirror" -> "5.60.5"
  ),
  useYarn := true,
  // dependencyOverrides ++= Seq(
  //   "org.scala-js" %%% "scalajs-dom" % "2.2.0"
  // )

).enablePlugins(ScalaJSPlugin, ScalablyTypedConverterPlugin, ScalaJSWeb).
  dependsOn(sharedJs)


lazy val shared = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("shared")).settings(
    scalaVersion := scalaV,
    // scalacOptions ++= Seq("-Ymacro-annotations"),
    libraryDependencies ++= Seq(
      // "com.typesafe.play" %% "play-json" % "2.6.1",
      "org.julienrf" %%% "play-json-derived-codecs" % "8.0.0"
    )
  ).jsConfigure(_ enablePlugins ScalaJSWeb)

lazy val sharedJvm = shared.jvm
lazy val sharedJs = shared.js

// loads the server project at sbt startup
onLoad in Global := (Command.process("project server", _: State)) compose (onLoad in Global).value


