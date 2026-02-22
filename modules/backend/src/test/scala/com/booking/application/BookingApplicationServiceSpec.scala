package com.booking.application

import com.booking.domain.model.*
import com.booking.domain.repository.*
import com.booking.infrastructure.repository.*
import zio.*
import zio.test.*
import java.time.Instant
import java.util.UUID

object BookingApplicationServiceSpec extends ZIOSpecDefault:

  def spec = suite("BookingApplicationServiceSpec")(
    test("create and cancel a booking successfully") {
      for
        memberRepo  <- ZIO.service[MemberRepository]
        sessionRepo <- ZIO.service[ClassSessionRepository]
        service     <- ZIO.service[BookingApplicationService]
        member = Member.create(FullName("Bob", "Jones"), Email("bob@example.com"), None)
        _      <- memberRepo.save(member)
        session = ClassSession.schedule(
                    classTypeId  = ClassTypeId(UUID.randomUUID()),
                    instructorId = InstructorId(UUID.randomUUID()),
                    studioId     = StudioId(UUID.randomUUID()),
                    startTime    = Instant.now().plusSeconds(7200),
                    maxCapacity  = 5,
                  )
        _       <- sessionRepo.save(session)
        booking <- service.createBooking(CreateBookingCommand(member.id, session.id))
        _       <- assertTrue(booking.memberId == member.id)
        _       <- assertTrue(booking.classSessionId == session.id)
        _       <- assertTrue(booking.status == BookingStatus.Confirmed)
        cancelled <- service.cancelBooking(booking.id, member.id)
      yield assertTrue(cancelled.status == BookingStatus.Cancelled)
    }.provide(
      InMemoryMemberRepository.layer,
      InMemoryClassSessionRepository.layer,
      InMemoryBookingRepository.layer,
      BookingApplicationService.layer,
    ),
  )
