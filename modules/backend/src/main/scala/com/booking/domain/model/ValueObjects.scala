package com.booking.domain.model

import java.util.UUID

// Opaque types for type-safe IDs
opaque type MemberId       = UUID
opaque type InstructorId   = UUID
opaque type ClassTypeId    = UUID
opaque type StudioId       = UUID
opaque type ClassSessionId = UUID
opaque type BookingId      = UUID

object MemberId:
  def apply(id: UUID): MemberId     = id
  def generate(): MemberId          = UUID.randomUUID()
  extension (id: MemberId) def value: UUID = id

object InstructorId:
  def apply(id: UUID): InstructorId     = id
  def generate(): InstructorId          = UUID.randomUUID()
  extension (id: InstructorId) def value: UUID = id

object ClassTypeId:
  def apply(id: UUID): ClassTypeId     = id
  def generate(): ClassTypeId          = UUID.randomUUID()
  extension (id: ClassTypeId) def value: UUID = id

object StudioId:
  def apply(id: UUID): StudioId     = id
  def generate(): StudioId          = UUID.randomUUID()
  extension (id: StudioId) def value: UUID = id

object ClassSessionId:
  def apply(id: UUID): ClassSessionId     = id
  def generate(): ClassSessionId          = UUID.randomUUID()
  extension (id: ClassSessionId) def value: UUID = id

object BookingId:
  def apply(id: UUID): BookingId     = id
  def generate(): BookingId          = UUID.randomUUID()
  extension (id: BookingId) def value: UUID = id

// Value objects
case class Email(value: String):
  require(value.contains("@"), s"Invalid email: $value")

case class PhoneNumber(value: String)

case class FullName(first: String, last: String):
  def display: String = s"$first $last"

case class Address(
  street: String,
  city: String,
  country: String,
  postalCode: String,
)

enum DifficultyLevel:
  case Beginner, Intermediate, Advanced, AllLevels

enum MembershipType:
  case Free, Basic, Premium

enum BookingStatus:
  case Confirmed, Cancelled, WaitListed, NoShow

enum ClassSessionStatus:
  case Scheduled, Cancelled, Completed
