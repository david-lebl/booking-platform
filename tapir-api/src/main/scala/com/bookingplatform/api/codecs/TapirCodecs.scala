package com.bookingplatform.api.codecs

import com.bookingplatform.shared.models.*
import sttp.tapir.*
import sttp.tapir.CodecFormat.TextPlain

import java.util.UUID

object TapirCodecs:
  given Schema[UserRole]            = Schema.derivedEnumeration[UserRole].defaultStringBased
  given Schema[UserStatus]          = Schema.derivedEnumeration[UserStatus].defaultStringBased
  given Schema[ServiceCategory]     = Schema.derivedEnumeration[ServiceCategory].defaultStringBased
  given Schema[RoomType]            = Schema.derivedEnumeration[RoomType].defaultStringBased
  given Schema[StationType]         = Schema.derivedEnumeration[StationType].defaultStringBased
  given Schema[VenueStatus]         = Schema.derivedEnumeration[VenueStatus].defaultStringBased
  given Schema[StationStatus]       = Schema.derivedEnumeration[StationStatus].defaultStringBased
  given Schema[ClassInstanceStatus] = Schema.derivedEnumeration[ClassInstanceStatus].defaultStringBased
  given Schema[BookingStatus]       = Schema.derivedEnumeration[BookingStatus].defaultStringBased
  given Schema[WaitlistStatus]      = Schema.derivedEnumeration[WaitlistStatus].defaultStringBased
  given Schema[SubscriptionStatus]  = Schema.derivedEnumeration[SubscriptionStatus].defaultStringBased
  given Schema[CreditPackageStatus] = Schema.derivedEnumeration[CreditPackageStatus].defaultStringBased
  given Schema[CancellationFeeType] = Schema.derivedEnumeration[CancellationFeeType].defaultStringBased
  given Schema[BillingInterval]     = Schema.derivedEnumeration[BillingInterval].defaultStringBased
