package com.booking.domain

import com.booking.domain.model.*
import com.booking.domain.service.BookingDomainService
import zio.test.*
import java.time.Instant
import java.util.UUID

object BookingDomainServiceSpec extends ZIOSpecDefault:

  private val member = Member(
    id             = MemberId(UUID.randomUUID()),
    name           = FullName("Alice", "Smith"),
    email          = Email("alice@example.com"),
    phone          = None,
    membershipType = MembershipType.Basic,
    createdAt      = Instant.now(),
    active         = true,
  )

  private val session = ClassSession(
    id           = ClassSessionId(UUID.randomUUID()),
    classTypeId  = ClassTypeId(UUID.randomUUID()),
    instructorId = InstructorId(UUID.randomUUID()),
    studioId     = StudioId(UUID.randomUUID()),
    startTime    = Instant.now().plusSeconds(3600),
    maxCapacity  = 10,
    bookedCount  = 0,
    status       = ClassSessionStatus.Scheduled,
  )

  def spec = suite("BookingDomainServiceSpec")(
    test("allows booking when member is active, session is available, and not already booked") {
      val result = BookingDomainService.validateBooking(member, session, None)
      assertTrue(result == Right(()))
    },
    test("rejects booking when member is inactive") {
      val inactiveMember = member.copy(active = false)
      val result = BookingDomainService.validateBooking(inactiveMember, session, None)
      assertTrue(result == Left(BookingDomainService.BookingError.MemberNotActive))
    },
    test("rejects booking when session is full") {
      val fullSession = session.copy(bookedCount = session.maxCapacity)
      val result = BookingDomainService.validateBooking(member, fullSession, None)
      assertTrue(result == Left(BookingDomainService.BookingError.SessionNotBookable))
    },
    test("rejects booking when already booked") {
      val booking = Booking.create(member.id, session.id)
      val result = BookingDomainService.validateBooking(member, session, Some(booking))
      assertTrue(result == Left(BookingDomainService.BookingError.AlreadyBooked))
    },
    test("allows cancellation of an active booking") {
      val booking = Booking.create(member.id, session.id)
      val result = BookingDomainService.validateCancellation(booking)
      assertTrue(result == Right(()))
    },
    test("rejects cancellation of already cancelled booking") {
      val booking = Booking.create(member.id, session.id).cancel()
      val result = BookingDomainService.validateCancellation(booking)
      assertTrue(result == Left(BookingDomainService.BookingError.BookingAlreadyCancelled))
    },
  )
