import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import org.scalajs.linker.interface.ModuleSplitStyle
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import scala.sys.process._

name := "flow"

ThisBuild / organization := "flow"
ThisBuild / scalaVersion := "3.3.3"
ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val pekkoV = "1.0.3"
lazy val pekkoHttpV = "1.0.1"


// Backend Core Module
lazy val server = project
  .in(file("backend/server"))
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
      "org.apache.pekko" %% "pekko-stream" % pekkoV,
      "org.apache.pekko" %% "pekko-actor-typed" % pekkoV,
      "org.apache.pekko" %% "pekko-slf4j" % pekkoV,
      "ch.qos.logback" % "logback-classic" % "1.4.11"
    )
  )

lazy val coreIO = project
  .in(file("backend/core"))
  .settings(
    name := "coreIO",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
      "org.apache.pekko" %% "pekko-stream" % pekkoV,
      "com.lihaoyi" %%% "upickle" % "3.1.4",
      // "io.github.fishuyo" %% "actor" % "0.2.0-SNAPSHOT"
    )
  )

lazy val util = project
  .in(file("backend/util"))
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
      "org.apache.pekko" %% "pekko-stream" % pekkoV,
      "org.webjars" % "webjars-locator" % "0.52",
      "seer" %% "math" % "0.2.0-SNAPSHOT"
    )
  )

// Backend Services
lazy val projectorIO = project
  .in(file("backend/services/projectorIO"))
  .settings(
    libraryDependencies ++= Seq(
      // "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
      // "org.apache.pekko" %% "pekko-stream" % pekkoV,
      // "com.lihaoyi" %%% "upickle" % "3.1.4",
    )
  ).dependsOn(coreIO)



// lazy val flowService = project
//   .in(file("backend/services/flow"))
//   .dependsOn(protocolJVM, internalProtocol, util)
//   .settings(
//     name := "flow-service",
//     libraryDependencies ++= Seq(
//       "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
//       "org.apache.pekko" %% "pekko-stream" % pekkoV,
//       "org.typelevel" %% "spire" % "0.18.0",
//       "com.eed3si9n.eval" % "eval" % "0.3.0" cross CrossVersion.full,
//       "de.sciss" %% "scalaosc" % "1.3.1",
//       "de.sciss" %% "audiofile" % "2.4.2",
//       "seer" %% "math" % "0.2.0-SNAPSHOT",
//       "net.java.dev.jna" % "jna" % "5.7.0",
//       "net.java.dev.jna" % "jna-platform" % "5.7.0",
//       "org.lwjgl" % "lwjgl-openvr" % "3.2.0",
//       "org.lwjgl" % "lwjgl-openvr" % "3.2.0" classifier "natives-windows",
//       "org.lwjgl" % "lwjgl" % "3.2.0",
//       "org.lwjgl" % "lwjgl" % "3.2.0" classifier "natives-windows"
//     )
//   )




// Frontend packages
lazy val client = project
  .in(file("frontend/client"))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .settings(
   // Tell Scala.js that this is an application with a main method
    scalaJSUseMainModuleInitializer := true,
    // mainClass := Some("client.Main"),

    /* Configure ScalablyTyped */
    externalNpm := {
      Process("npm", baseDirectory.value).!
      baseDirectory.value
    },
    stIgnore := List(),

    /* Configure Scala.js to emit modules in the optimal way to
     * connect to Vite's incremental reload.
     * - emit ECMAScript modules
     * - emit as many small modules as possible for classes in the "client" package
     * - emit as few (large) modules as possible for all other classes
     */
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(
          ModuleSplitStyle.SmallModulesFor(List("client")))
    },

    /* Dependencies */
    libraryDependencies ++= Seq(
      // "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      // "com.raquo" %%% "laminar" % "17.2.0",
      // "com.raquo" %%% "waypoint" % "9.0.0",
      // "com.lihaoyi" %%% "upickle" % "3.1.4",
      // "io.github.fishuyo" %%% "examplesjs" % "0.2.0-SNAPSHOT"
    ),
  )
  .dependsOn(coreUI, projectorRemote)


// Frontend core
lazy val coreUI = project
  .in(file("frontend/core"))
  .enablePlugins(ScalaJSPlugin)
  // .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      "com.raquo" %%% "laminar" % "17.2.0",
      "com.raquo" %%% "waypoint" % "9.0.0",
      "com.lihaoyi" %%% "upickle" % "3.1.4",
    ),
    // externalNpm := {
      // Process("npm", baseDirectory.value).!
      // baseDirectory.value
    // },
    // stIgnore := List(),
  )


// Frontend apps
lazy val projectorRemote = project
  .in(file("frontend/interfaces/projectorRemote"))
  // .dependsOn(protocolJS)
  .enablePlugins(ScalaJSPlugin)
  // .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .settings(
    name := "projectorRemote",
    libraryDependencies ++= Seq(

    ),
    // externalNpm := {
      // Process("npm", baseDirectory.value).!
      // baseDirectory.value
    // },
    // stIgnore := List(),
  )
  .dependsOn(coreUI)


// Root project
// lazy val root = project
  // .in(file("."))
  // .aggregate(
    // core,
    // projectorIO,
    // projectorRemote,
    // flowFrontend,
    // launcherFrontend
    // protocolJVM,
    // protocolJS,
    // internalProtocol,
    // util,
    // flowService,
    // launcherService,
    // flowFrontend,
    // launcherFrontend
  // ) 