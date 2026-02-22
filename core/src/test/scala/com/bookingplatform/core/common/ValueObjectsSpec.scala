package com.bookingplatform.core.common

import zio.*
import zio.prelude.Validation
import zio.test.*

object ValueObjectsSpec extends ZIOSpecDefault:

  private def isValid[A](v: Validation[?, A]): Boolean   = v.toEither.isRight
  private def isInvalid[A](v: Validation[?, A]): Boolean = v.toEither.isLeft

  def spec = suite("ValueObjects")(
    suite("Email")(
      test("valid email is accepted") {
        assertTrue(isValid(Email.make("user@example.com")))
      },
      test("invalid email is rejected") {
        assertTrue(isInvalid(Email.make("not-an-email")))
      },
      test("empty email is rejected") {
        assertTrue(isInvalid(Email.make("")))
      }
    ),
    suite("PositiveInt")(
      test("positive number is accepted") {
        assertTrue(isValid(PositiveInt.make(5)))
      },
      test("zero is rejected") {
        assertTrue(isInvalid(PositiveInt.make(0)))
      },
      test("negative number is rejected") {
        assertTrue(isInvalid(PositiveInt.make(-1)))
      }
    ),
    suite("NonNegativeInt")(
      test("positive number is accepted") {
        assertTrue(isValid(NonNegativeInt.make(5)))
      },
      test("zero is accepted") {
        assertTrue(isValid(NonNegativeInt.make(0)))
      },
      test("negative number is rejected") {
        assertTrue(isInvalid(NonNegativeInt.make(-1)))
      }
    ),
    suite("NonEmptyString")(
      test("non-empty string is accepted") {
        assertTrue(isValid(NonEmptyString.make("hello")))
      },
      test("empty string is rejected") {
        assertTrue(isInvalid(NonEmptyString.make("")))
      }
    ),
    suite("Money")(
      test("addition of same currency works") {
        val a = Money(BigDecimal(10), Currency.CZK)
        val b = Money(BigDecimal(20), Currency.CZK)
        assertTrue((a + b) == Right(Money(BigDecimal(30), Currency.CZK)))
      },
      test("addition of different currencies fails") {
        val a = Money(BigDecimal(10), Currency.CZK)
        val b = Money(BigDecimal(20), Currency.EUR)
        assertTrue((a + b).isLeft)
      },
      test("multiplication works") {
        val m = Money(BigDecimal(10), Currency.CZK)
        assertTrue((m * BigDecimal(3)) == Money(BigDecimal(30), Currency.CZK))
      }
    ),
    suite("Phone")(
      test("valid phone is accepted") {
        assertTrue(isValid(Phone.make("+420123456789")))
      },
      test("invalid phone is rejected") {
        assertTrue(isInvalid(Phone.make("not-a-phone")))
      }
    )
  )
