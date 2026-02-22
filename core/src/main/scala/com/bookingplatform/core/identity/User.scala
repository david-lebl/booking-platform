package com.bookingplatform.core.identity

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.ids.*
import com.bookingplatform.shared.models.{UserRole, UserStatus}

import java.time.Instant

final case class User(
    id: UserId,
    email: Email,
    firstName: NonEmptyString,
    lastName: NonEmptyString,
    phone: Option[Phone],
    role: UserRole,
    status: UserStatus,
    createdAt: Instant,
    updatedAt: Instant
)

enum Permission:
  case ManageBookings, ManageSchedule, ManageBilling, ManageUsers, ManageVenues, ViewReports

object Permission:
  def forRole(role: UserRole): Set[Permission] = role match
    case UserRole.Client =>
      Set(ManageBookings)
    case UserRole.Trainer =>
      Set(ManageBookings, ManageSchedule)
    case UserRole.Admin =>
      Set(ManageBookings, ManageSchedule, ManageBilling, ManageUsers, ManageVenues, ViewReports)
    case UserRole.SuperAdmin =>
      Permission.values.toSet
