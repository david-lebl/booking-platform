package com.bookingplatform.core.booking

import com.bookingplatform.shared.models.BookingStatus
import zio.test.*
import zio.test.Assertion.*

object BookingStateMachineSpec extends ZIOSpecDefault:

  def spec = suite("BookingStateMachine")(
    suite("valid transitions")(
      test("Pending -> Confirmed") {
        val result = BookingStateMachine.transition(BookingStatus.Pending, BookingStatus.Confirmed)
        assertTrue(result == Right(BookingStatus.Confirmed))
      },
      test("Pending -> Cancelled") {
        val result = BookingStateMachine.transition(BookingStatus.Pending, BookingStatus.Cancelled)
        assertTrue(result == Right(BookingStatus.Cancelled))
      },
      test("Confirmed -> CheckedIn") {
        val result = BookingStateMachine.transition(BookingStatus.Confirmed, BookingStatus.CheckedIn)
        assertTrue(result == Right(BookingStatus.CheckedIn))
      },
      test("Confirmed -> Cancelled") {
        val result = BookingStateMachine.transition(BookingStatus.Confirmed, BookingStatus.Cancelled)
        assertTrue(result == Right(BookingStatus.Cancelled))
      },
      test("Confirmed -> NoShow") {
        val result = BookingStateMachine.transition(BookingStatus.Confirmed, BookingStatus.NoShow)
        assertTrue(result == Right(BookingStatus.NoShow))
      },
      test("CheckedIn -> Completed") {
        val result = BookingStateMachine.transition(BookingStatus.CheckedIn, BookingStatus.Completed)
        assertTrue(result == Right(BookingStatus.Completed))
      }
    ),
    suite("invalid transitions")(
      test("Completed -> Confirmed is rejected") {
        val result = BookingStateMachine.transition(BookingStatus.Completed, BookingStatus.Confirmed)
        assertTrue(result.isLeft)
      },
      test("Cancelled -> Confirmed is rejected") {
        val result = BookingStateMachine.transition(BookingStatus.Cancelled, BookingStatus.Confirmed)
        assertTrue(result.isLeft)
      },
      test("NoShow -> CheckedIn is rejected") {
        val result = BookingStateMachine.transition(BookingStatus.NoShow, BookingStatus.CheckedIn)
        assertTrue(result.isLeft)
      },
      test("Pending -> CheckedIn is rejected") {
        val result = BookingStateMachine.transition(BookingStatus.Pending, BookingStatus.CheckedIn)
        assertTrue(result.isLeft)
      }
    ),
    suite("isTerminal")(
      test("Completed is terminal") {
        assertTrue(BookingStateMachine.isTerminal(BookingStatus.Completed))
      },
      test("Cancelled is terminal") {
        assertTrue(BookingStateMachine.isTerminal(BookingStatus.Cancelled))
      },
      test("NoShow is terminal") {
        assertTrue(BookingStateMachine.isTerminal(BookingStatus.NoShow))
      },
      test("Pending is not terminal") {
        assertTrue(!BookingStateMachine.isTerminal(BookingStatus.Pending))
      },
      test("Confirmed is not terminal") {
        assertTrue(!BookingStateMachine.isTerminal(BookingStatus.Confirmed))
      }
    )
  )
