package com.bookingplatform.infrastructure.httpclient.notifications

import com.bookingplatform.core.common.*
import com.bookingplatform.core.notification.*
import zio.*

final case class LoggingNotificationSender() extends NotificationSender:
  def send(message: NotificationMessage): IO[DomainError, Unit] =
    ZIO.logInfo(
      s"[NOTIFICATION] channel=${message.channel} to=${message.to} subject=${message.subject}"
    )

object LoggingNotificationSender:
  val layer: ULayer[NotificationSender] = ZLayer.succeed(LoggingNotificationSender())
