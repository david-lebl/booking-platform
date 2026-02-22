import Dependencies._

ThisBuild / scalaVersion := Versions.scala3
ThisBuild / organization := "com.bookingplatform"
ThisBuild / version      := "0.1.0-SNAPSHOT"

ThisBuild / scalacOptions ++= Seq(
  "-Wunused:all",
  "-deprecation",
  "-feature",
  "-language:implicitConversions"
)

ThisBuild / libraryDependencySchemes ++= Seq(
  "dev.zio" %% "zio-schema"            % VersionScheme.Always,
  "dev.zio" %% "zio-schema-derivation" % VersionScheme.Always,
  "dev.zio" %% "zio-schema-json"       % VersionScheme.Always
)

ThisBuild / resolvers ++= Seq(
  "ical4j" at "https://repo.mnode.org/releases"
)

lazy val root = project
  .in(file("."))
  .aggregate(
    sharedJVM,
    sharedJS,
    core,
    tapirApi,
    infraDb,
    infraRestApiController,
    infraHttpClient,
    infraPaymentGatewayClient,
    app,
    integrationTest,
    ui
  )
  .settings(
    name    := "booking-platform",
    publish := {},
    publishLocal := {}
  )

// --- Cross-compiled shared module ---

lazy val shared = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    name := "shared",
    libraryDependencies ++= Seq(
      "dev.zio" %%% "zio-json" % Versions.zioJson
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      zioTest    % Test,
      zioTestSbt % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .jsSettings(
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) }
  )

lazy val sharedJVM = shared.jvm
lazy val sharedJS  = shared.js

// --- Core domain ---

lazy val core = project
  .in(file("core"))
  .dependsOn(sharedJVM)
  .settings(
    name := "core",
    libraryDependencies ++= Seq(
      zio,
      zioStreams,
      zioPrelude,
      zioJson,
      zioTest         % Test,
      zioTestSbt      % Test,
      zioTestMagnolia % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

// --- Tapir API definitions ---

lazy val tapirApi = project
  .in(file("tapir-api"))
  .dependsOn(core)
  .settings(
    name := "tapir-api",
    libraryDependencies ++= Seq(
      tapirCore,
      tapirZio,
      tapirJsonZio
    )
  )

// --- Infrastructure: Database ---

lazy val infraDb = project
  .in(file("infrastructure/db"))
  .dependsOn(core)
  .settings(
    name := "infra-db",
    libraryDependencies ++= Seq(
      typoRuntime,
      postgresql,
      flyway,
      flywayPostgresql,
      zioJson
    )
  )

// --- Infrastructure: REST API Controller ---

lazy val infraRestApiController = project
  .in(file("infrastructure/rest-api-controller"))
  .dependsOn(tapirApi, infraDb)
  .settings(
    name := "infra-rest-api-controller",
    libraryDependencies ++= Seq(
      tapirZioHttp,
      tapirSwagger,
      zioHttp,
      zioLogging,
      zioLoggingSlf4j
    )
  )

// --- Infrastructure: HTTP Client ---

lazy val infraHttpClient = project
  .in(file("infrastructure/http-client"))
  .dependsOn(core)
  .settings(
    name := "infra-http-client",
    libraryDependencies ++= Seq(
      zioHttp,
      ical4j
    )
  )

// --- Infrastructure: Payment Gateway Client ---

lazy val infraPaymentGatewayClient = project
  .in(file("infrastructure/payment-gateway-client"))
  .dependsOn(core)
  .settings(
    name := "infra-payment-gateway-client",
    libraryDependencies ++= Seq(
      zio
    )
  )

// --- App (main entry point) ---

lazy val app = project
  .in(file("app"))
  .dependsOn(
    core,
    tapirApi,
    infraDb,
    infraRestApiController,
    infraHttpClient,
    infraPaymentGatewayClient
  )
  .settings(
    name := "app",
    libraryDependencies ++= Seq(
      zio,
      zioConfig,
      zioConfigTypesafe,
      zioConfigMagnolia,
      zioLogging,
      zioLoggingSlf4j,
      logbackClassic
    ),
    Compile / mainClass := Some("com.bookingplatform.app.Main")
  )

// --- Integration Tests ---

lazy val integrationTest = project
  .in(file("integration-test"))
  .dependsOn(app)
  .settings(
    name := "integration-test",
    libraryDependencies ++= Seq(
      zioTest              % Test,
      zioTestSbt           % Test,
      testcontainersScala     % Test,
      testcontainersScalaCore % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    publish := {},
    publishLocal := {}
  )

// --- UI (Scala.js frontend) ---

lazy val ui = project
  .in(file("ui"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(sharedJS)
  .settings(
    name := "ui",
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    libraryDependencies ++= Seq(
      "com.raquo"    %%% "laminar"  % Versions.laminar,
      "com.raquo"    %%% "waypoint" % Versions.waypoint,
      "dev.zio"      %%% "zio-json" % Versions.zioJson
    ),
    scalaJSUseMainModuleInitializer := true
  )
