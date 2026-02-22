package com.booking.frontend.model

case class ClassSessionView(
  id: String,
  classTypeName: String,
  instructorName: String,
  studioName: String,
  startTime: String,
  availableSpots: Int,
  maxCapacity: Int,
  status: String,
)

case class BookingView(
  id: String,
  classSessionId: String,
  startTime: String,
  status: String,
)

case class MemberView(
  id: String,
  firstName: String,
  lastName: String,
  email: String,
  membershipType: String,
)
