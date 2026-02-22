package com.bookingplatform.core.booking

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.*

import java.time.{Duration, Instant}

final case class Booking(
    id: BookingId,
    userId: UserId,
    classInstanceId: ClassInstanceId,
    stationId: Option[StationId],
    status: BookingStatus,
    cancellationFee: Option[BigDecimal],
    createdAt: Instant,
    updatedAt: Instant
)

final case class WaitlistEntry(
    id: WaitlistEntryId,
    classInstanceId: ClassInstanceId,
    userId: UserId,
    position: Int,
    status: WaitlistStatus,
    offerExpiresAt: Option[Instant],
    createdAt: Instant
)

final case class CancellationPolicy(
    id: CancellationPolicyId,
    name: NonEmptyString,
    rules: List[CancellationRule],
    createdAt: Instant
)

final case class CancellationRule(
    noticePeriodMinutes: Int,
    feeType: CancellationFeeType,
    feeValue: Option[BigDecimal]
):
  def calculateFee(servicePrice: BigDecimal): BigDecimal = feeType match
    case CancellationFeeType.NoFee         => BigDecimal(0)
    case CancellationFeeType.FixedFee      => feeValue.getOrElse(BigDecimal(0))
    case CancellationFeeType.Percentage    => servicePrice * feeValue.getOrElse(BigDecimal(0)) / 100
    case CancellationFeeType.FullPrice     => servicePrice
    case CancellationFeeType.CreditForfeit => BigDecimal(0) // handled separately
