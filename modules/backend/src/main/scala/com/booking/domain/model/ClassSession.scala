package com.booking.domain.model

import java.time.Instant

case class ClassSession(
  id: ClassSessionId,
  classTypeId: ClassTypeId,
  instructorId: InstructorId,
  studioId: StudioId,
  startTime: Instant,
  maxCapacity: Int,
  bookedCount: Int,
  status: ClassSessionStatus,
):
  def availableSpots: Int = maxCapacity - bookedCount
  def isFull: Boolean = bookedCount >= maxCapacity
  def isBookable: Boolean = status == ClassSessionStatus.Scheduled && !isFull
  def incrementBooking(): ClassSession = copy(bookedCount = bookedCount + 1)
  def decrementBooking(): ClassSession = copy(bookedCount = (bookedCount - 1).max(0))

object ClassSession:
  def schedule(
    classTypeId: ClassTypeId,
    instructorId: InstructorId,
    studioId: StudioId,
    startTime: Instant,
    maxCapacity: Int,
  ): ClassSession =
    ClassSession(
      id = ClassSessionId.generate(),
      classTypeId = classTypeId,
      instructorId = instructorId,
      studioId = studioId,
      startTime = startTime,
      maxCapacity = maxCapacity,
      bookedCount = 0,
      status = ClassSessionStatus.Scheduled,
    )
