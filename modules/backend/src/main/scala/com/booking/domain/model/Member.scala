package com.booking.domain.model

import java.time.Instant

case class Member(
  id: MemberId,
  name: FullName,
  email: Email,
  phone: Option[PhoneNumber],
  membershipType: MembershipType,
  createdAt: Instant,
  active: Boolean,
)

object Member:
  def create(
    name: FullName,
    email: Email,
    phone: Option[PhoneNumber],
    membershipType: MembershipType = MembershipType.Free,
  ): Member =
    Member(
      id = MemberId.generate(),
      name = name,
      email = email,
      phone = phone,
      membershipType = membershipType,
      createdAt = Instant.now(),
      active = true,
    )
