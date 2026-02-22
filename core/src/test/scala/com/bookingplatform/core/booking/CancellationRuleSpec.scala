package com.bookingplatform.core.booking

import com.bookingplatform.shared.models.CancellationFeeType
import zio.test.*

object CancellationRuleSpec extends ZIOSpecDefault:

  def spec = suite("CancellationRule")(
    test("NoFee returns zero") {
      val rule = CancellationRule(60, CancellationFeeType.NoFee, None)
      assertTrue(rule.calculateFee(BigDecimal(100)) == BigDecimal(0))
    },
    test("FixedFee returns fixed amount") {
      val rule = CancellationRule(30, CancellationFeeType.FixedFee, Some(BigDecimal(50)))
      assertTrue(rule.calculateFee(BigDecimal(100)) == BigDecimal(50))
    },
    test("Percentage calculates correctly") {
      val rule = CancellationRule(15, CancellationFeeType.Percentage, Some(BigDecimal(25)))
      assertTrue(rule.calculateFee(BigDecimal(200)) == BigDecimal(50))
    },
    test("FullPrice returns service price") {
      val rule = CancellationRule(0, CancellationFeeType.FullPrice, None)
      assertTrue(rule.calculateFee(BigDecimal(150)) == BigDecimal(150))
    },
    test("CreditForfeit returns zero monetary fee") {
      val rule = CancellationRule(0, CancellationFeeType.CreditForfeit, None)
      assertTrue(rule.calculateFee(BigDecimal(100)) == BigDecimal(0))
    }
  )
