package learning.lesson05

import cats.data.Validated
import cats.syntax.all._

/** Lesson 05: combine independent field checks and collect their errors.
  * The nonblank-number and positive-premium rules are illustrative lab rules.
  */
object ValidatedLesson {

  final case class Policy(number: String, annualPremium: BigDecimal)

  // E = List[String] (errors), A = String (the successfully checked value).
  def validateNumber(number: Option[String]): Validated[List[String], String] =
    number match {
      case None =>
        Validated.Invalid(List("Policy number is missing"))
      case Some(value) if value.trim.isEmpty =>
        Validated.Invalid(List("Policy number must not be blank"))
      case Some(value) =>
        Validated.Valid(value)
    }

  // The error type stays the same; the successful value is now BigDecimal.
  def validatePremium(
      premium: Option[BigDecimal]
  ): Validated[List[String], BigDecimal] =
    premium match {
      case None =>
        Validated.Invalid(List("Annual premium is missing"))
      case Some(value) if value <= 0 =>
        Validated.Invalid(List("Annual premium must be positive"))
      case Some(value) =>
        Validated.Valid(value)
    }

  def buildPolicy(
      number: Option[String],
      premium: Option[BigDecimal]
  ): Validated[List[String], Policy] = {
    val checkedNumber: Validated[List[String], String] = validateNumber(number)
    val checkedPremium: Validated[List[String], BigDecimal] = validatePremium(premium)

    // Same operation as lesson 04, now using Validated's combination behavior.
    // Two Valid values construct a Policy. Invalid values contribute their errors.
    (checkedNumber, checkedPremium).mapN(Policy.apply)
  }

  private def checkCase(
      label: String,
      number: Option[String],
      premium: Option[BigDecimal],
      expected: Validated[List[String], Policy]
  ): Unit = {
    val result: Validated[List[String], Policy] = buildPolicy(number, premium)
    println(s"$label: $result")
    assert(result == expected, s"Unexpected result for $label: $result")
  }

  def main(args: Array[String]): Unit = {
    val number: Option[String] = Some("POL-001")
    val premium: Option[BigDecimal] = Some(BigDecimal("600"))

    println("Lesson 05 - Collect independent field errors with Validated")
    checkCase(
      "Both valid", number, premium,
      Validated.Valid(Policy("POL-001", BigDecimal("600")))
    )
    checkCase(
      "Number missing", None, premium,
      Validated.Invalid(List("Policy number is missing"))
    )
    checkCase(
      "Premium missing", number, None,
      Validated.Invalid(List("Annual premium is missing"))
    )
    checkCase(
      "Both missing", None, None,
      Validated.Invalid(List("Policy number is missing", "Annual premium is missing"))
    )
    checkCase(
      "Blank number", Some("   "), premium,
      Validated.Invalid(List("Policy number must not be blank"))
    )
    checkCase(
      "Zero premium", number, Some(BigDecimal("0")),
      Validated.Invalid(List("Annual premium must be positive"))
    )
    checkCase(
      "Both invalid", Some(""), Some(BigDecimal("-10")),
      Validated.Invalid(List("Policy number must not be blank", "Annual premium must be positive"))
    )
    println("All seven lesson checks passed.")
  }
}
