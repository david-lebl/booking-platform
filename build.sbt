val scala3Version     = "3.3.4"
val zioVersion        = "2.1.11"
val zioPreludeVersion = "1.0.0-RC31"
val zioJsonVersion    = "0.7.3"
val zioConfigVersion  = "4.0.2"
val zioLoggingVersion = "2.3.1"
val tapirVersion      = "1.11.4"
val quillVersion      = "4.8.5"
val laminarVersion    = "17.0.0"
val frontrouteVersion = "0.19.0"

lazy val root = project
  .in(file("."))
  .aggregate(backend, frontend)
  .settings(
    name := "booking-platform",
    publish / skip := true,
  )

lazy val backend = project
  .in(file("modules/backend"))
  .settings(
    name         := "booking-platform-backend",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio"                        % zioVersion,
      "dev.zio" %% "zio-streams"                % zioVersion,
      "dev.zio" %% "zio-prelude"                % zioPreludeVersion,
      "dev.zio" %% "zio-json"                   % zioJsonVersion,
      "dev.zio" %% "zio-config"                 % zioConfigVersion,
      "dev.zio" %% "zio-config-typesafe"        % zioConfigVersion,
      "dev.zio" %% "zio-config-magnolia"        % zioConfigVersion,
      "dev.zio" %% "zio-logging"                % zioLoggingVersion,
      "dev.zio" %% "zio-logging-slf4j2"         % zioLoggingVersion,
      "ch.qos.logback"  %  "logback-classic"    % "1.5.8",
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server"  % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-zio"          % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "io.getquill"    %% "quill-jdbc-zio"       % quillVersion,
      "org.postgresql"  %  "postgresql"          % "42.7.4",
      "org.flywaydb"    %  "flyway-core"         % "10.18.0",
      "org.flywaydb"    %  "flyway-database-postgresql" % "10.18.0",
      "dev.zio" %% "zio-test"     % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    Compile / mainClass := Some("com.booking.Main"),
  )

lazy val frontend = project
  .in(file("modules/frontend"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name         := "booking-platform-frontend",
    scalaVersion := scala3Version,
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies ++= Seq(
      "com.raquo"     %%% "laminar"    % laminarVersion,
      "io.frontroute" %%% "frontroute" % frontrouteVersion,
      "dev.zio"       %%% "zio-json"   % zioJsonVersion,
    ),
  )
