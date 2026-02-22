val scala3Version    = "3.3.4"
val zioVersion       = "2.1.6"
val zioJsonVersion   = "0.7.1"
val zioPreludeVersion = "1.0.0-RC27"
val tapirVersion     = "1.11.1"
val laminarVersion   = "17.0.0"
val upickleVersion   = "3.3.1"

lazy val commonSettings = Seq(
  scalaVersion := scala3Version,
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")
)

lazy val root = (project in file("."))
  .settings(
    name := "booking-platform",
    commonSettings,
    publish / skip := true
  )
  .aggregate(core, `tapir-api`, infrastructure, app, `integration-test`, ui)

lazy val core = (project in file("core"))
  .settings(
    name := "core",
    commonSettings,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"          % zioVersion,
      "dev.zio" %% "zio-prelude"  % zioPreludeVersion,
      "dev.zio" %% "zio-test"     % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

lazy val `tapir-api` = (project in file("tapir-api"))
  .settings(
    name := "tapir-api",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core"     % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-zio"      % tapirVersion
    )
  )
  .dependsOn(core)

lazy val infrastructure = (project in file("infrastructure"))
  .settings(
    name := "infrastructure",
    commonSettings,
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"   % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion
    )
  )
  .dependsOn(core, `tapir-api`)

lazy val app = (project in file("app"))
  .settings(
    name := "app",
    commonSettings,
    Compile / mainClass := Some("bookingplatform.app.Main")
  )
  .dependsOn(infrastructure)

lazy val `integration-test` = (project in file("integration-test"))
  .settings(
    name := "integration-test",
    commonSettings,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-test"     % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    publish / skip := true
  )
  .dependsOn(app)

lazy val ui = (project in file("ui"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "ui",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    libraryDependencies ++= Seq(
      "com.raquo"    %%% "laminar"    % laminarVersion,
      "com.lihaoyi"  %%% "upickle"    % upickleVersion,
      "org.scala-js" %%% "scalajs-dom" % "2.8.0"
    )
  )
