package com.booking.domain.model

case class Studio(
  id: StudioId,
  name: String,
  address: Address,
  capacity: Int,
  active: Boolean,
)

object Studio:
  def create(name: String, address: Address, capacity: Int): Studio =
    Studio(
      id = StudioId.generate(),
      name = name,
      address = address,
      capacity = capacity,
      active = true,
    )
