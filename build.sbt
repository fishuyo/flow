import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import scala.sys.process.Process

name := "flow"

ThisBuild / organization := "flow"
ThisBuild / scalaVersion := "3.3.1" //"2.13.10"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val pekkoV = "1.0.3"
lazy val pekkoHttpV = "1.0.1"

// lazy val flow = project.in(file("."))
  // .aggregate(server, client, shared.jvm, shared.js)
// 
lazy val server = project.in(file("modules/server"))
  .settings(
    // scalaJSProjects := Seq(client),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
    
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
    ),

    Assets / WebKeys.packagePrefix := "public/",
    Runtime / managedClasspath += (Assets / packageBin).value
  )
  .enablePlugins(WebScalaJSBundlerPlugin, JavaAppPackaging)
  // .enablePlugins(SbtWeb, JavaAppPackaging)
  .dependsOn(flow_service)


// lazy val client = project.in(file("modules/client"))
//   .settings(
//     scalaJSUseMainModuleInitializer := true,
//     libraryDependencies ++= Seq(
//       "com.yang-bo" %%% "html" % "3.0.3",
//       "com.thoughtworks.binding" %%% "latestevent" % "2.0.0",
//       "org.querki" %%% "querki-jsext" % "0.12",
//     ),
//     // Compile / npmDependencies ++= Seq(),
//     Compile / npmDevDependencies ++= Seq(
//       "file-loader" -> "6.2.0",
//       "style-loader" -> "2.0.0",
//       "css-loader" -> "5.2.6",
//       "html-webpack-plugin" -> "4.5.1",
//       "copy-webpack-plugin" -> "6.4.0",
//       "windicss-webpack-plugin" -> "1.7.3",
//       "webpack-merge" -> "5.8.0",
//     ),
//     Compile / unmanagedResources / inputFileStamper := sbt.nio.FileStamper.LastModified,
  
//     scalacOptions ++= Seq("-Ymacro-annotations"),
//     useYarn := true,
//     stFlavour := Flavour.Slinky,
//     stReactEnableTreeShaking := Selection.All,
//     stIgnore ++= List("react-proxy", "react-dom", "semantic-ui-css"),

//     webpackCliVersion := "4.10.0",
//     webpackConfigFile := Some((baseDirectory).value / "webpack" / "custom.webpack.config.js"),

//     fastOptJS / webpackBundlingMode := BundlingMode.LibraryAndApplication(),
//   )
//   .enablePlugins(ScalaJSPlugin, ScalablyTypedConverterPlugin, ScalaJSBundlerPlugin)

lazy val util = project.in(file("modules/util"))
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
      "org.apache.pekko" %% "pekko-stream" % pekkoV,
      "org.webjars" % "webjars-locator" % "0.52"
    ),
  )  


/**
 * Services
 */

lazy val flow_service = project.in(file("modules/services/flow/service"))
  .settings(
    scalaJSProjects := Seq(flow_client),
    Assets / pipelineStages := Seq(scalaJSPipeline),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
    
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
      "org.apache.pekko" %% "pekko-stream" % pekkoV,
      // "org.apache.pekko" %% "pekko-pki" % "2.6.20",
      // "org.apache.pekko" %% "pekko-remote" % "2.6.20",
      // "com.vmunier" %% "scalajs-scripts" % "1.2.0",
      // "org.webjars" % "webjars-locator-core" % "0.52",

      "org.typelevel" %% "spire" % "0.18.0",
      "com.eed3si9n.eval" %% "eval" % "0.3.0" cross CrossVersion.full,
      // "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      // "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      // "org.scala-lang" % "scala-library" % scalaVersion.value,

      "de.sciss" %% "scalaosc" % "1.3.1",
      "de.sciss" %% "audiofile" % "2.4.2",
      "seer" %% "math" % "0.2.0-SNAPSHOT",
      // "seer" %% "actor" % "0.1-SNAPSHOT",
      "net.java.dev.jna" % "jna" % "5.7.0",
      "net.java.dev.jna" % "jna-platform" % "5.7.0",

      "org.lwjgl" % "lwjgl-openvr" % "3.2.0",
      // "org.lwjgl" % "lwjgl-openvr" % "3.2.0" classifier "natives-macos",
      "org.lwjgl" % "lwjgl-openvr" % "3.2.0" classifier "natives-windows",
      "org.lwjgl" % "lwjgl" % "3.2.0",
      // "org.lwjgl" % "lwjgl" % "3.2.0" classifier "natives-macos",
      "org.lwjgl" % "lwjgl" % "3.2.0" classifier "natives-windows",

    ),

    Assets / WebKeys.packagePrefix := "public/",
    Runtime / managedClasspath += (Assets / packageBin).value
  )
  .enablePlugins(WebScalaJSBundlerPlugin)
  .dependsOn(flow_shared.jvm, util)


lazy val flow_client = project.in(file("modules/services/flow/client"))
  // .configure(browserProject)
  .settings(
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      // "org.scala-js" %%% "scalajs-dom" % "2.1.0",
      "com.yang-bo" %%% "html" % "3.0.3",
      "com.thoughtworks.binding" %%% "latestevent" % "2.0.0",
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
  .dependsOn(flow_shared.js)


lazy val flow_shared = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/services/flow/shared"))
  .settings(
    libraryDependencies ++= Seq(
      // "org.julienrf" %%% "play-json-derived-codecs" % "8.0.0",
      // "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-core"   % "2.27.4",
      // "com.github.plokhotnyuk.jsoniter-scala" %%% "jsoniter-scala-macros" % "2.27.4",
      "com.lihaoyi" %%% "upickle" % "3.1.4"
    )
  )
  .jsConfigure(_.enablePlugins(ScalaJSBundlerPlugin))

