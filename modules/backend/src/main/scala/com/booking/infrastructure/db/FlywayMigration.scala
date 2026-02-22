package com.booking.infrastructure.db

import com.booking.infrastructure.config.AppConfig
import org.flywaydb.core.Flyway
import zio.*

object FlywayMigration:

  val migrate: ZIO[AppConfig, Throwable, Unit] =
    ZIO.serviceWithZIO[AppConfig] { config =>
      ZIO.attemptBlocking {
        val flyway = Flyway.configure()
          .dataSource(config.database.jdbcUrl, config.database.user, config.database.password)
          .locations("classpath:db/migrations")
          .load()
        flyway.migrate()
      }.unit
    }
