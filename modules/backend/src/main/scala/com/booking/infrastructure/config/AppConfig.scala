package com.booking.infrastructure.config

import zio.*
import zio.config.*
import zio.config.magnolia.*
import zio.config.typesafe.*

case class DatabaseConfig(
  host: String,
  port: Int,
  database: String,
  user: String,
  password: String,
):
  def jdbcUrl: String = s"jdbc:postgresql://$host:$port/$database"

case class HttpConfig(
  host: String,
  port: Int,
)

case class AppConfig(
  database: DatabaseConfig,
  http: HttpConfig,
)

object AppConfig:
  val layer: ZLayer[Any, Config.Error, AppConfig] =
    ZLayer.fromZIO(
      read(deriveConfig[AppConfig].from(TypesafeConfigProvider.fromResourcePath()))
    )
