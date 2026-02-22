package com.bookingplatform.infrastructure.restapi.controllers

import com.bookingplatform.core.common.DomainError
import zio.*
import zio.prelude.Validation

object ValidationHelper:
  def validate[A](validation: Validation[String, A]): IO[DomainError, A] =
    ZIO.fromEither(
      validation.toEither.left.map(errors => DomainError.ValidationError(errors.head))
    )
