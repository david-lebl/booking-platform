package com.bookingplatform.shared.models

import zio.json.*

enum UserRole derives JsonEncoder, JsonDecoder:
  case Client, Trainer, Admin, SuperAdmin

enum UserStatus derives JsonEncoder, JsonDecoder:
  case Active, Inactive, Suspended

enum ServiceCategory derives JsonEncoder, JsonDecoder:
  case IndividualTraining, GroupClass, ReformerPilates, Yoga, Spinning, Custom

enum RoomType derives JsonEncoder, JsonDecoder:
  case Standard, ReformerStudio, YogaStudio, Gym

enum StationType derives JsonEncoder, JsonDecoder:
  case Reformer, Cadillac, Chair, Barrel, Generic

enum VenueStatus derives JsonEncoder, JsonDecoder:
  case Active, Inactive

enum StationStatus derives JsonEncoder, JsonDecoder:
  case Available, Maintenance, OutOfService

enum ClassInstanceStatus derives JsonEncoder, JsonDecoder:
  case Scheduled, InProgress, Completed, Cancelled

enum BookingStatus derives JsonEncoder, JsonDecoder:
  case Pending, Confirmed, CheckedIn, Completed, Cancelled, NoShow

enum WaitlistStatus derives JsonEncoder, JsonDecoder:
  case Waiting, Offered, Converted, Expired, Removed

enum SubscriptionStatus derives JsonEncoder, JsonDecoder:
  case Active, Paused, PastDue, Cancelled, Expired

enum CreditPackageStatus derives JsonEncoder, JsonDecoder:
  case Active, Expired, Depleted

enum CancellationFeeType derives JsonEncoder, JsonDecoder:
  case NoFee, FixedFee, Percentage, FullPrice, CreditForfeit

enum BillingInterval derives JsonEncoder, JsonDecoder:
  case Monthly, Quarterly, Yearly
