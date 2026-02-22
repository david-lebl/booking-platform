import sbt._

object Dependencies {

  object Versions {
    val scala3       = "3.6.4"
    val zio          = "2.1.16"
    val zioPrelude   = "1.0.0-RC39"
    val zioJson      = "0.7.39"
    val zioConfig    = "4.0.3"
    val zioLogging   = "2.5.0"
    val tapir        = "1.11.24"
    val zioHttp      = "3.0.1"
    val flyway       = "10.22.0"
    val postgresql   = "42.7.4"
    val typo         = "0.31.0"
    val ical4j       = "4.0.8"
    val laminar      = "17.2.0"
    val waypoint     = "9.0.0"
    val testcontainers = "0.41.8"
  }

  // ZIO
  val zio          = "dev.zio" %% "zio"            % Versions.zio
  val zioStreams    = "dev.zio" %% "zio-streams"    % Versions.zio
  val zioPrelude   = "dev.zio" %% "zio-prelude"    % Versions.zioPrelude
  val zioJson      = "dev.zio" %% "zio-json"       % Versions.zioJson
  val zioConfig    = "dev.zio" %% "zio-config"          % Versions.zioConfig
  val zioConfigTypesafe = "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
  val zioConfigMagnolia = "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig
  val zioLogging   = "dev.zio" %% "zio-logging"    % Versions.zioLogging
  val zioLoggingSlf4j = "dev.zio" %% "zio-logging-slf4j2" % Versions.zioLogging
  val zioTest      = "dev.zio" %% "zio-test"       % Versions.zio
  val zioTestSbt   = "dev.zio" %% "zio-test-sbt"   % Versions.zio
  val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % Versions.zio

  // Tapir
  val tapirCore     = "com.softwaremill.sttp.tapir" %% "tapir-core"          % Versions.tapir
  val tapirZio      = "com.softwaremill.sttp.tapir" %% "tapir-zio"           % Versions.tapir
  val tapirZioHttp  = "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % Versions.tapir
  val tapirJsonZio  = "com.softwaremill.sttp.tapir" %% "tapir-json-zio"      % Versions.tapir
  val tapirSwagger  = "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % Versions.tapir

  // ZIO HTTP
  val zioHttp = "dev.zio" %% "zio-http" % Versions.zioHttp

  // Database
  val postgresql = "org.postgresql" % "postgresql"  % Versions.postgresql
  val flyway     = "org.flywaydb"   % "flyway-core" % Versions.flyway
  val flywayPostgresql = "org.flywaydb" % "flyway-database-postgresql" % Versions.flyway

  // Typo
  val typoRuntime = "com.olvind.typo" %% "typo-dsl-zio-jdbc" % Versions.typo

  // iCal
  val ical4j = "org.mnode.ical4j" % "ical4j" % Versions.ical4j

  // Testcontainers
  val testcontainersScala = "com.dimafeng" %% "testcontainers-scala-postgresql" % Versions.testcontainers
  val testcontainersScalaCore = "com.dimafeng" %% "testcontainers-scala-core" % Versions.testcontainers

  // Logging backend
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.5.16"
}
