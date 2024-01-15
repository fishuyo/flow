import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import scala.sys.process.Process

name := "flow"

ThisBuild / organization := "com.fishuyo"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version      := "0.1.0-SNAPSHOT"


lazy val flow = project.in(file("."))
  .aggregate(server, client, shared.jvm, shared.js)


lazy val server = project.in(file("server"))
  .settings(
    scalaJSProjects := Seq(client),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
    
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.2.10",
      "com.typesafe.akka" %% "akka-stream" % "2.6.20",
      "com.typesafe.akka" %% "akka-pki" % "2.6.20",
      "com.typesafe.akka" %% "akka-remote" % "2.6.20",
      // "com.vmunier" %% "scalajs-scripts" % "1.2.0",

      "org.typelevel" %% "spire" % "0.18.0",
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "org.scala-lang" % "scala-library" % scalaVersion.value,

      "de.sciss" %% "scalaosc" % "1.3.1",
      "de.sciss" %% "audiofile" % "2.4.2",
      "seer" %% "math" % "0.1-SNAPSHOT",
      "seer" %% "actor" % "0.1-SNAPSHOT",
    ),

    Assets / WebKeys.packagePrefix := "public/",
    Runtime / managedClasspath += (Assets / packageBin).value
  )
  .enablePlugins(WebScalaJSBundlerPlugin, JavaAppPackaging)
  // .enablePlugins(SbtWeb, JavaAppPackaging)
  .dependsOn(shared.jvm)


lazy val client = project.in(file("client"))
  // .configure(browserProject)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      // "org.scala-js" %%% "scalajs-dom" % "2.1.0",
      "com.yang-bo" %%% "html" % "2.0.2",
      "org.querki" %%% "querki-jsext" % "0.12",

      // "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core"   % "2.21.2",
      // "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.21.2"


      // "me.shadaj" %%% "slinky-web" % "0.7.3",
      // "me.shadaj" %%% "slinky-hot" % "0.7.3",
    ),
    Compile / npmDependencies ++= Seq(
      // "react" -> "16.12.0",
      // "react-dom" -> "16.12.0",
      // "react-proxy" -> "1.1.8",
      // "react-router" -> "5.1.2",
      // "react-router-dom" -> "5.1.2",
      // "history" -> "^4.0.0",
      // "@types/react-dom" -> "16.9.8",
      // "@types/react-router" -> "5.1.2",
      // "@types/react-router-dom" -> "5.1.2",
      // "@types/history" -> "^4.0.0",
      "@types/jquery" -> "3.5.14",
      "@types/codemirror" -> "5.60.7",
      "@types/materialize-css" -> "1.0.0",
      // "semantic-ui-react" -> "2.1.4",
      // "semantic-ui-css" -> "2.5.0",
      // "@material-ui/core" -> "3.9.4", // note: version 4 is not supported yet
      // "@material-ui/styles" -> "3.0.0-alpha.10", // note: version 4 is not supported yet
      // "@material-ui/icons" -> "3.0.2",
      // "recharts" -> "1.8.5",
      // "@types/recharts" -> "1.8.10",
      // "@types/classnames" -> "2.2.10",
    ),
    Compile / npmDevDependencies ++= Seq(
      "file-loader" -> "6.2.0",
      "style-loader" -> "2.0.0",
      "css-loader" -> "5.2.6",
      "html-webpack-plugin" -> "4.5.1",
      "copy-webpack-plugin" -> "6.4.0",
      "windicss-webpack-plugin" -> "1.7.3",
      "webpack-merge" -> "5.8.0",
    ),
    Compile / unmanagedResources / inputFileStamper := sbt.nio.FileStamper.LastModified,
  
    scalacOptions ++= Seq("-Ymacro-annotations"),
    useYarn := true,
    stFlavour := Flavour.Slinky,
    stReactEnableTreeShaking := Selection.All,
    stIgnore ++= List("react-proxy", "react-dom", "semantic-ui-css"),

    // webpack / version := "4.44.2",
    // startWebpackDevServer / version := "3.11.2",
    webpackCliVersion := "4.10.0",

    // webpackResources := baseDirectory.value / "webpack" * "*",

    webpackConfigFile := Some((baseDirectory).value / "webpack" / "custom.webpack.config.js"),
    // fastOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-fastopt.config.js"),
    // fullOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-opt.config.js"),
    // Test / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-core.config.js"),

    fastOptJS / webpackDevServerExtraArgs := Seq("--inline", "--hot", "--history-api-fallback", "--host=0.0.0.0"),
    fastOptJS / webpackBundlingMode := BundlingMode.LibraryAndApplication(),
    Compile / fastOptJS / webpackDevServerExtraArgs += "--mode=development",
    Compile / fullOptJS / webpackDevServerExtraArgs += "--mode=production",


    // Test / requireJsDomEnv := true,

    // addCommandAlias("dev", ";fastOptJS::startWebpackDevServer;~fastOptJS"),

    // addCommandAlias("build", "fullOptJS::webpack"),
  )
  .enablePlugins(ScalaJSPlugin, ScalablyTypedConverterPlugin, ScalaJSBundlerPlugin)
  .dependsOn(shared.js)


lazy val shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    libraryDependencies ++= Seq(
      // "org.julienrf" %%% "play-json-derived-codecs" % "8.0.0",
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core"   % "2.21.2",
      "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-macros" % "2.21.2",
    )
  )
  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))


  
// loads the server project at sbt startup
// Global / onLoad := (Command.process("project server", _: State)) compose (Global / onLoad).value


// import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}


// lazy val server = project.in(file("server")).settings(
//   scalaVersion := scalaV,
//   scalaJSProjects := Seq(client),
//   pipelineStages in Assets := Seq(scalaJSPipeline),
//   pipelineStages := Seq(digest, gzip),
//   // triggers scalaJSPipeline when using compile or continuous compilation
//   compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
//   resolvers += Resolver.sonatypeRepo("snapshots"),
//   //updateOptions := updateOptions.value.withLatestSnapshots(false),

//   // resolvers += Resolver.bintrayRepo("hseeberger", "maven"),
//   libraryDependencies ++= List(
//     "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
//     "com.typesafe.akka" %% "akka-pki" % "2.6.20",
//     "com.typesafe.akka" %% "akka-remote" % "2.6.20",
//     "com.typesafe.akka" %% "akka-http" % "10.2.10"
//   ),
//   libraryDependencies ++= Seq(
//     "io.circe" %% "circe-core",
//     "io.circe" %% "circe-generic",
//     "io.circe" %% "circe-parser"
//   ).map(_ % "0.14.3"),

//   libraryDependencies ++= Seq(
//     "com.vmunier" %% "scalajs-scripts" % "1.2.0",
//     "com.google.inject"            % "guice"                % "5.1.0",
//     "com.google.inject.extensions" % "guice-assistedinject" % "5.1.0",
//     guice,
//     // specs2 % Test,
  
//     // "net.java.dev.jna" % "jna" % "4.0.0",
//     // "org.hid4java" % "hid4java" % "0.5.0",
//     "org.typelevel" %% "spire" % "0.18.0",

//     // "com.twitter" %% "util-eval" % "6.43.0",
//     "org.scala-lang" % "scala-reflect" % scalaV,
//     "org.scala-lang" % "scala-compiler" % scalaV,
//     "org.scala-lang" % "scala-library" % scalaV,

//     // "org.scodec" %% "scodec-core" % "2.2.0",

//     "de.sciss" %% "scalaosc" % "1.3.1",
//     "de.sciss" %% "audiofile" % "2.4.2",
//     // "com.fishuyo.seer" %% "interface_server" % "0.1-SNAPSHOT",
//     "seer" %% "math" % "0.1-SNAPSHOT",
//     "seer" %% "actor" % "0.1-SNAPSHOT",
//     // "script" %% "script" % "0.1-SNAPSHOT",
//     // "phasespace" %% "core" % "0.1-SNAPSHOT",
//     // "phasespace" % "native" % "0.1-SNAPSHOT",

//     // "org.lwjgl" % "lwjgl-openvr" % "3.2.0",
//     // "org.lwjgl" % "lwjgl-openvr" % "3.2.0" classifier "natives-macos",
//     // "org.lwjgl" % "lwjgl-openvr" % "3.2.0" classifier "natives-windows",
//     // "org.lwjgl" % "lwjgl" % "3.2.0",
//     // "org.lwjgl" % "lwjgl" % "3.2.0" classifier "natives-macos",
//     // "org.lwjgl" % "lwjgl" % "3.2.0" classifier "natives-windows",

//     // "org.webjars" %% "webjars-play" % "2.6.0",
//     // "org.webjars" % "jquery" % "3.2.1",
//     // "org.webjars" % "materializecss" % "0.99.0",
//     // "org.webjars.npm" % "codemirror" % "5.27.4"
//   )
// ).settings(
//   if(System.getProperty("os.name").contains("Mac")) Seq(
//     envVars := Map("DYLD_LIBRARY_PATH" -> file("lib").getAbsolutePath)
//   ) else Seq()
// ).enablePlugins(PlayScala).
//   dependsOn(sharedJvm)


// lazy val client = project.in(file("client")).settings(
//   scalaVersion := scalaV,
//   scalaJSUseMainModuleInitializer := true,
//   resolvers += Resolver.jcenterRepo,
//   resolvers += Resolver.bintrayRepo("denigma", "denigma-releases"),

//   scalacOptions ++= Seq("-Ymacro-annotations"),

//   libraryDependencies ++= Seq(
//     // "me.shadaj" %%% "slinky-web" % "0.7.2"

//     // "org.scala-js" %%% "scalajs-dom" % "0.9.1",
//     "com.yang-bo" %%% "html" % "2.0.0"
//     // "com.yang-bo" %%% "html" % "0.0.0+444-fe1d54da+20221129-1201"
//     // "org.lrng.binding" %%% "html" % "1.0.3+56-51cfb24a+20221129-1135" //"latest.release"
//     // "org.lrng.binding" %%% "html" % "latest.release"
//     // "com.thoughtworks.binding" %%% "dom" % "latest.release",
//     // "org.querki" %%% "jquery-facade" % "1.0",
//     // "com.definitelyscala" % "scala-js-materializecss_sjs0.6_2.11" % "1.0.0",
//     // "org.denigma" %%% "codemirror-facade" % "5.13.2-0.8",
//     // compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
//   ),
//   // jsDependencies ++= Seq(
//   //   "org.webjars" % "jquery" % "3.2.1" / "jquery.js" minified "jquery.min.js",
//   //   "org.webjars" % "materializecss" % "0.99.0" / "materialize.js" minified "materialize.min.js" dependsOn "jquery.js",
//   //   "org.webjars.npm" % "codemirror" % "5.27.4" / "lib/codemirror.js",
//   //   "org.webjars.npm" % "codemirror" % "5.27.4" / "mode/clike/clike.js" dependsOn "lib/codemirror.js",
//   //   "org.webjars.npm" % "codemirror" % "5.27.4" / "keymap/sublime.js" dependsOn "lib/codemirror.js",
//   //   "org.webjars.npm" % "codemirror" % "5.27.4" / "addon/comment/comment.js" dependsOn "lib/codemirror.js",
//   //   "org.webjars.npm" % "codemirror" % "5.27.4" / "addon/search/searchcursor.js" dependsOn "lib/codemirror.js"
//   // ),
//   Compile / npmDependencies ++= Seq(
//     "@types/jquery" -> "3.5.14",
//     "@types/codemirror" -> "5.60.5"
//   ),
//   useYarn := true,
//   // dependencyOverrides ++= Seq(
//   //   "org.scala-js" %%% "scalajs-dom" % "2.2.0"
//   // )

// ).enablePlugins(ScalaJSPlugin, ScalablyTypedConverterPlugin, ScalaJSWeb).
//   dependsOn(sharedJs)


// lazy val shared = crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure).in(file("shared")).settings(
//     scalaVersion := scalaV,
//     // scalacOptions ++= Seq("-Ymacro-annotations"),
//     libraryDependencies ++= Seq(
//       // "com.typesafe.play" %% "play-json" % "2.6.1",
//       "org.julienrf" %%% "play-json-derived-codecs" % "8.0.0"
//     )
//   ).jsConfigure(_ enablePlugins ScalaJSWeb)

// lazy val sharedJvm = shared.jvm
// lazy val sharedJs = shared.js



/**
  * Implement the `start` and `dist` tasks defined above.
  * Most of this is really just to copy the index.html file around.
  */
// lazy val browserProject: Project => Project =
//   _.settings(
//     start := {
//       (Compile / fastOptJS / startWebpackDevServer).value
//     },
//     dist := {
//       val artifacts = (Compile / fullOptJS / webpack).value
//       val artifactFolder = (Compile / fullOptJS / crossTarget).value
//       val distFolder = (ThisBuild / baseDirectory).value / "docs" / moduleName.value

//       distFolder.mkdirs()
//       artifacts.foreach { artifact =>
//         val target = artifact.data.relativeTo(artifactFolder) match {
//           case None          => distFolder / artifact.data.name
//           case Some(relFile) => distFolder / relFile.toString
//         }

//         Files.copy(artifact.data.toPath, target.toPath, REPLACE_EXISTING)
//       }

//       val indexFrom = baseDirectory.value / "src/main/js/index.html"
//       val indexTo = distFolder / "index.html"

//       val indexPatchedContent = {
//         import collection.JavaConverters._
//         Files
//           .readAllLines(indexFrom.toPath, IO.utf8)
//           .asScala
//           .map(_.replaceAllLiterally("-fastopt-", "-opt-"))
//           .mkString("\n")
//       }

//       Files.write(indexTo.toPath, indexPatchedContent.getBytes(IO.utf8))
//       distFolder
//     }
//   )