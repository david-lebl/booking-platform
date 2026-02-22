package com.booking.domain.model

case class ClassType(
  id: ClassTypeId,
  name: String,
  description: String,
  durationMinutes: Int,
  difficultyLevel: DifficultyLevel,
  maxCapacity: Int,
  priceInCents: Long,
)

object ClassType:
  def create(
    name: String,
    description: String,
    durationMinutes: Int,
    difficultyLevel: DifficultyLevel,
    maxCapacity: Int,
    priceInCents: Long,
  ): ClassType =
    ClassType(
      id = ClassTypeId.generate(),
      name = name,
      description = description,
      durationMinutes = durationMinutes,
      difficultyLevel = difficultyLevel,
      maxCapacity = maxCapacity,
      priceInCents = priceInCents,
    )
