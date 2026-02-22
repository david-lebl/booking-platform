package com.bookingplatform.app

import zio.*
import zio.config.*
import zio.config.magnolia.*

final case class ServerConfig(
    host: String = "0.0.0.0",
    port: Int = 8080
)

final case class DatabaseConfig(
    url: String = "jdbc:postgresql://localhost:5432/booking",
    user: String = "postgres",
    password: String = "postgres"
)

final case class AppConfig(
    server: ServerConfig = ServerConfig(),
    database: DatabaseConfig = DatabaseConfig()
)

object AppConfig:
  val layer: ULayer[AppConfig] = ZLayer.succeed(AppConfig())
  val serverConfig: URLayer[AppConfig, ServerConfig] = ZLayer.fromFunction((c: AppConfig) => c.server)
  val databaseConfig: URLayer[AppConfig, DatabaseConfig] = ZLayer.fromFunction((c: AppConfig) => c.database)
