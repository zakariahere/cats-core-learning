package learning.lesson07

import cats.Semigroup
import cats.data.Validated
import cats.syntax.all._

/** Lesson 07: supply the combination rule for our own structured errors. */
object CustomErrorSemigroupLesson {

  final case class Policy(number: String, annualPremium: BigDecimal)
  final case class FieldError(field: String, message: String)
  final case class ValidationErrors(items: List[FieldError])

  // This implicit object tells Cats how to combine our application-owned type.
  implicit val validationErrorsSemigroup: Semigroup[ValidationErrors] =
    new Semigroup[ValidationErrors] {
      override def combine(
          left: ValidationErrors,
          right: ValidationErrors
      ): ValidationErrors =
        ValidationErrors(left.items ++ right.items)
    }

  def main(args: Array[String]): Unit = {
    val numberError = FieldError("number", "is missing")
    val premiumError = FieldError("annualPremium", "must be positive")
    val numberErrors = ValidationErrors(List(numberError))
    val premiumErrors = ValidationErrors(List(premiumError))
    val expectedErrors = ValidationErrors(List(numberError, premiumError))

    println("Lesson 07 - Teach Cats to combine our validation errors")

    val combined: ValidationErrors =
      validationErrorsSemigroup.combine(numberErrors, premiumErrors)
    println(s"Our combine: $combined")
    assert(combined == expectedErrors)

    // These are already-computed check results, as in lesson 06.
    val checkedNumber: Validated[ValidationErrors, String] =
      Validated.Invalid(numberErrors)
    val checkedPremium: Validated[ValidationErrors, BigDecimal] =
      Validated.Invalid(premiumErrors)

    // Same mapN; the error type is now ValidationErrors, so Cats uses our instance.
    val bothFailed: Validated[ValidationErrors, Policy] =
      (checkedNumber, checkedPremium).mapN(Policy.apply)
    println(s"Both invalid: $bothFailed")
    assert(bothFailed == Validated.Invalid(expectedErrors))

    // Make the helper argument visible again to connect this with lesson 06.
    val explicitResult: Validated[ValidationErrors, Policy] =
      checkedNumber.product(checkedPremium)(validationErrorsSemigroup)
        .map { case (number, premium) => Policy(number, premium) }
    println(s"Explicit product then map: $explicitResult")
    assert(explicitResult == bothFailed)

    val validNumber: Validated[ValidationErrors, String] = Validated.Valid("POL-001")
    val validPremium: Validated[ValidationErrors, BigDecimal] =
      Validated.Valid(BigDecimal("600"))

    val onlyNumberFailed = (checkedNumber, validPremium).mapN(Policy.apply)
    val onlyPremiumFailed = (validNumber, checkedPremium).mapN(Policy.apply)
    val bothValid = (validNumber, validPremium).mapN(Policy.apply)
    println(s"Only number invalid: $onlyNumberFailed")
    println(s"Only premium invalid: $onlyPremiumFailed")
    println(s"Both valid: $bothValid")
    assert(onlyNumberFailed == Validated.Invalid(numberErrors))
    assert(onlyPremiumFailed == Validated.Invalid(premiumErrors))
    assert(bothValid == Validated.Valid(Policy("POL-001", BigDecimal("600"))))

    // Associativity follows from List concatenation; these values illustrate it.
    val customerError = FieldError("customerName", "is missing")
    val customerErrors = ValidationErrors(List(customerError))
    val expectedThree = ValidationErrors(List(numberError, premiumError, customerError))
    val groupedLeft = validationErrorsSemigroup.combine(
      validationErrorsSemigroup.combine(numberErrors, premiumErrors), customerErrors
    )
    val groupedRight = validationErrorsSemigroup.combine(
      numberErrors, validationErrorsSemigroup.combine(premiumErrors, customerErrors)
    )
    println(s"Grouped left: $groupedLeft")
    println(s"Grouped right: $groupedRight")
    assert(groupedLeft == expectedThree)
    assert(groupedRight == expectedThree)

    println("All eight lesson checks passed.")
  }
}
