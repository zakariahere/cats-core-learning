package learning.lesson08

import cats.Monoid
import cats.syntax.all._

/** Lesson 08: combine a batch of error reports, including an empty batch. */
object MonoidLesson {

  final case class FieldError(field: String, message: String)
  final case class ValidationErrors(items: List[FieldError])

  // Same combination rule as lesson 07, plus a value that contributes no errors.
  implicit val validationErrorsMonoid: Monoid[ValidationErrors] =
    new Monoid[ValidationErrors] {
      override def empty: ValidationErrors = ValidationErrors(Nil)

      override def combine(
          left: ValidationErrors,
          right: ValidationErrors
      ): ValidationErrors =
        ValidationErrors(left.items ++ right.items)
    }

  def main(args: Array[String]): Unit = {
    val numberError = FieldError("number", "is missing")
    val premiumError = FieldError("annualPremium", "must be positive")
    val customerError = FieldError("customerName", "is missing")

    val numberErrors = ValidationErrors(List(numberError))
    val premiumErrors = ValidationErrors(List(premiumError))
    val customerErrors = ValidationErrors(List(customerError))
    val reports: List[ValidationErrors] =
      List(numberErrors, premiumErrors, customerErrors)

    println("Lesson 08 - Combine any number of error reports with Monoid")

    // Cats uses our Monoid[ValidationErrors] to reduce the whole collection.
    val combined: ValidationErrors = reports.combineAll
    val expected = ValidationErrors(List(numberError, premiumError, customerError))
    println(s"Three reports: $combined")
    assert(combined == expected)

    val single: ValidationErrors = List(numberErrors).combineAll
    println(s"One report: $single")
    assert(single == numberErrors)

    val noReports: List[ValidationErrors] = List.empty[ValidationErrors]
    val noErrors: ValidationErrors = noReports.combineAll
    println(s"No reports: $noErrors")
    assert(noErrors == ValidationErrors(Nil))

    val monoid: Monoid[ValidationErrors] = Monoid[ValidationErrors]

    // Identity means empty changes neither side of the combination.
    val emptyOnLeft = monoid.combine(monoid.empty, combined)
    val emptyOnRight = monoid.combine(combined, monoid.empty)
    println(s"Empty on the left: $emptyOnLeft")
    println(s"Empty on the right: $emptyOnRight")
    assert(emptyOnLeft == expected)
    assert(emptyOnRight == expected)

    // The familiar accumulation expressed explicitly for this List example.
    val explicitFold: ValidationErrors =
      reports.foldLeft(monoid.empty) { (accumulated, nextReport) =>
        monoid.combine(accumulated, nextReport)
      }
    assert(explicitFold == combined)
    println("Explicit fold agrees with combineAll.")

    println("All six lesson checks passed.")
  }
}
