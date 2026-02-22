package com.bookingplatform.core.booking

import com.bookingplatform.core.common.*
import com.bookingplatform.shared.models.BookingStatus

object BookingStateMachine:

  private val validTransitions: Map[BookingStatus, Set[BookingStatus]] = Map(
    BookingStatus.Pending   -> Set(BookingStatus.Confirmed, BookingStatus.Cancelled),
    BookingStatus.Confirmed -> Set(BookingStatus.CheckedIn, BookingStatus.Cancelled, BookingStatus.NoShow),
    BookingStatus.CheckedIn -> Set(BookingStatus.Completed),
    BookingStatus.Completed -> Set.empty,
    BookingStatus.Cancelled -> Set.empty,
    BookingStatus.NoShow    -> Set.empty
  )

  def canTransition(from: BookingStatus, to: BookingStatus): Boolean =
    validTransitions.getOrElse(from, Set.empty).contains(to)

  def transition(from: BookingStatus, to: BookingStatus): Either[DomainError, BookingStatus] =
    if canTransition(from, to) then Right(to)
    else Left(DomainError.InvalidStateTransition(from.toString, to.toString))

  def isTerminal(status: BookingStatus): Boolean = status match
    case BookingStatus.Completed | BookingStatus.Cancelled | BookingStatus.NoShow => true
    case _                                                                        => false
