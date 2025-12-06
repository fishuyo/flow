import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import org.scalajs.linker.interface.ModuleSplitStyle
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import scala.sys.process._

name := "flow"

ThisBuild / organization := "flow"
ThisBuild / scalaVersion := "3.3.5"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val pekkoV = "1.0.3"
lazy val pekkoHttpV = "1.0.1"

// Vite build task - defined at build level so it can be used across projects
lazy val viteBuild = taskKey[Unit]("Build the frontend client with Vite")

// Backend Core Module
lazy val server = project
  .in(file("backend/server"))
  .enablePlugins(RevolverPlugin)
  .settings(
    mainClass := Some("flow.Server"),
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
      "org.apache.pekko" %% "pekko-stream" % pekkoV,
      "org.apache.pekko" %% "pekko-actor-typed" % pekkoV,
      "org.apache.pekko" %% "pekko-slf4j" % pekkoV,
      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "com.typesafe" % "config" % "1.4.2"
    ),
    // Vite build integration
    viteBuild := {
      val clientDir = baseDirectory.value / ".." / ".." / "frontend" / "client"
      val log = streams.value.log
      log.info("Building client with Vite...")
      val result = Process(Seq("npm", "run", "build"), clientDir).!
      if (result != 0) {
        throw new Exception(s"Vite build failed with exit code $result")
      }
      log.info("Vite build completed successfully")
    },
    // Build Scala.js client before Vite build
    viteBuild := (viteBuild dependsOn (client / Compile / fastLinkJS)).value
  )
  .dependsOn(multishell)

lazy val coreIO = project
  .in(file("backend/core"))
  .settings(
    name := "coreIO",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
      "org.apache.pekko" %% "pekko-stream" % pekkoV,
      "com.lihaoyi" %%% "upickle" % "3.1.4"
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

// Multishell Protocol (crossProject for JVM and JS)
lazy val multishellProtocol = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("protocol/multishell"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "3.1.4"
    ),
    // Explicitly add shared source directory (baseDirectory for crossProject points to .js/.jvm, need to go up)
    Compile / unmanagedSourceDirectories += baseDirectory.value.getParentFile / "shared" / "src" / "main" / "scala"
  )
  .jvmSettings()
  .jsSettings()

// Backend Services
lazy val projectorIO = project
  .in(file("backend/services/projectorIO"))
  .settings(
    libraryDependencies ++= Seq(
      // "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
      // "org.apache.pekko" %% "pekko-stream" % pekkoV,
      // "com.lihaoyi" %%% "upickle" % "3.1.4",
    )
  )
  .dependsOn(coreIO)

lazy val multishell = project
  .in(file("backend/services/multishell"))
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % pekkoHttpV,
      "org.apache.pekko" %% "pekko-stream" % pekkoV,
      "org.apache.pekko" %% "pekko-actor" % pekkoV,
      "org.jetbrains.pty4j" % "pty4j" % "0.13.4"
    )
  )
  .dependsOn(multishellProtocol.jvm, util)

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
      // Process(Seq("npm", "install"), baseDirectory.value).!
      // Process(Seq("npm", "list"), baseDirectory.value).!
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
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("client")))
    },

    /* Dependencies */
    libraryDependencies ++= Seq(
      // "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      // "com.raquo" %%% "laminar" % "17.2.0",
      // "com.raquo" %%% "waypoint" % "9.0.0",
      // "com.lihaoyi" %%% "upickle" % "3.1.4",
      // "io.github.fishuyo" %%% "examplesjs" % "0.2.0-SNAPSHOT"
    )
  )
  .dependsOn(coreUI, projectorRemote)

// Frontend core
lazy val coreUI = project
  .in(file("frontend/core"))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "2.8.0",
      "com.raquo" %%% "laminar" % "17.2.0",
      "com.raquo" %%% "waypoint" % "9.0.0",
      "com.lihaoyi" %%% "upickle" % "3.1.4"
    ),
    externalNpm := {
      // Process("npm", baseDirectory.value / ".." / "client").!
      // Process(Seq("npm", "install", "-s"), baseDirectory.value / ".." / "client").!
      baseDirectory.value / ".." / "client"
    },
    stIgnore := List(),
    // Ensure protocol project compiles before coreUI
    Compile / compile := (Compile / compile)
      .dependsOn(multishellProtocol.js / Compile / compile)
      .value
  )
  .dependsOn(multishellProtocol.js)

// Frontend apps
lazy val projectorRemote = project
  .in(file("frontend/interfaces/projectorRemote"))
  // .dependsOn(protocolJS)
  .enablePlugins(ScalaJSPlugin)
  // .enablePlugins(ScalablyTypedConverterExternalNpmPlugin)
  .settings(
    name := "projectorRemote",
    libraryDependencies ++= Seq(
    )
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

// SBT aliases for common workflows
addCommandAlias("serverDev", "project server; viteBuild; reStart")
addCommandAlias("serverBuild", "project server; viteBuild")
addCommandAlias("clientBuild", "project client; fastLinkJS")
