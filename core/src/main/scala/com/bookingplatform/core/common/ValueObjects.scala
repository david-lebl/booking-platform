package com.bookingplatform.core.common

import zio.prelude.*
import zio.json.*

import java.util.Currency as JavaCurrency

// --- Email ---

type Email = Email.Type
object Email extends Newtype[String]:
  private val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".r

  override inline def assertion: Assertion[String] =
    Assertion.matches(emailRegex.pattern.pattern())

  given JsonEncoder[Email] = JsonEncoder[String].contramap(unwrap)
  given JsonDecoder[Email] = JsonDecoder[String].mapOrFail(s =>
    Email.make(s).toEither.left.map(_.head.render)
  )

// --- NonEmptyString ---

type NonEmptyString = NonEmptyString.Type
object NonEmptyString extends Newtype[String]:
  override inline def assertion: Assertion[String] =
    Assertion.hasLength(Assertion.greaterThan(0))

  given JsonEncoder[NonEmptyString] = JsonEncoder[String].contramap(unwrap)
  given JsonDecoder[NonEmptyString] = JsonDecoder[String].mapOrFail(s =>
    NonEmptyString.make(s).toEither.left.map(_.head.render)
  )

// --- PositiveInt ---

type PositiveInt = PositiveInt.Type
object PositiveInt extends Subtype[Int]:
  override inline def assertion: Assertion[Int] =
    Assertion.greaterThan(0)

  given JsonEncoder[PositiveInt] = JsonEncoder[Int].contramap(unwrap)
  given JsonDecoder[PositiveInt] = JsonDecoder[Int].mapOrFail(i =>
    PositiveInt.make(i).toEither.left.map(_.head.render)
  )

// --- NonNegativeInt ---

type NonNegativeInt = NonNegativeInt.Type
object NonNegativeInt extends Subtype[Int]:
  override inline def assertion: Assertion[Int] =
    Assertion.greaterThanOrEqualTo(0)

  given JsonEncoder[NonNegativeInt] = JsonEncoder[Int].contramap(unwrap)
  given JsonDecoder[NonNegativeInt] = JsonDecoder[Int].mapOrFail(i =>
    NonNegativeInt.make(i).toEither.left.map(_.head.render)
  )

// --- Money ---

final case class Money(amount: BigDecimal, currency: Currency):
  def +(other: Money): Either[String, Money] =
    if currency != other.currency then Left(s"Cannot add ${currency} and ${other.currency}")
    else Right(Money(amount + other.amount, currency))

  def *(factor: BigDecimal): Money = Money(amount * factor, currency)

  def isPositive: Boolean    = amount > 0
  def isNonNegative: Boolean = amount >= 0
  def isZero: Boolean        = amount == 0

object Money:
  def zero(currency: Currency): Money = Money(BigDecimal(0), currency)

  given JsonEncoder[Money] = DeriveJsonEncoder.gen[Money]
  given JsonDecoder[Money] = DeriveJsonDecoder.gen[Money]

// --- Currency ---

enum Currency:
  case CZK, EUR, USD, GBP

object Currency:
  given JsonEncoder[Currency] = JsonEncoder[String].contramap(_.toString)
  given JsonDecoder[Currency] = JsonDecoder[String].mapOrFail { s =>
    Currency.values.find(_.toString == s).toRight(s"Invalid currency: $s")
  }

// --- Phone ---

type Phone = Phone.Type
object Phone extends Newtype[String]:
  private val phoneRegex = "^\\+?[1-9]\\d{1,14}$".r

  override inline def assertion: Assertion[String] =
    Assertion.matches(phoneRegex.pattern.pattern())

  given JsonEncoder[Phone] = JsonEncoder[String].contramap(unwrap)
  given JsonDecoder[Phone] = JsonDecoder[String].mapOrFail(s =>
    Phone.make(s).toEither.left.map(_.head.render)
  )

// --- Timezone ---

type Timezone = Timezone.Type
object Timezone extends Newtype[String]:
  override inline def assertion: Assertion[String] =
    Assertion.hasLength(Assertion.greaterThan(0))

  given JsonEncoder[Timezone] = JsonEncoder[String].contramap(unwrap)
  given JsonDecoder[Timezone] = JsonDecoder[String].mapOrFail(s =>
    Timezone.make(s).toEither.left.map(_.head.render)
  )
