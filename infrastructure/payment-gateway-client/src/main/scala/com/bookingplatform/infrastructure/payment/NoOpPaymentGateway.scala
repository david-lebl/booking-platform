package com.bookingplatform.infrastructure.payment

import com.bookingplatform.core.billing.PaymentGateway
import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import zio.*

import java.util.UUID

final case class NoOpPaymentGateway() extends PaymentGateway:
  def charge(userId: UserId, amount: Money, description: String): IO[DomainError, String] =
    ZIO.succeed(s"charge_${UUID.randomUUID()}")

  def refund(chargeId: String, amount: Money): IO[DomainError, Unit] =
    ZIO.unit

object NoOpPaymentGateway:
  val layer: ULayer[PaymentGateway] = ZLayer.succeed(NoOpPaymentGateway())
