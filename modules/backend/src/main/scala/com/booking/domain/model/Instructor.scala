package com.booking.domain.model

import java.time.Instant

case class Instructor(
  id: InstructorId,
  name: FullName,
  email: Email,
  phone: Option[PhoneNumber],
  bio: Option[String],
  specializations: List[String],
  active: Boolean,
  createdAt: Instant,
)

object Instructor:
  def create(
    name: FullName,
    email: Email,
    phone: Option[PhoneNumber],
    bio: Option[String],
    specializations: List[String],
  ): Instructor =
    Instructor(
      id = InstructorId.generate(),
      name = name,
      email = email,
      phone = phone,
      bio = bio,
      specializations = specializations,
      active = true,
      createdAt = Instant.now(),
    )
