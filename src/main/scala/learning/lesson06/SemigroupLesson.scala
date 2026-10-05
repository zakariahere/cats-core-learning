package learning.lesson06

import cats.Semigroup
import cats.data.Validated
import cats.syntax.all._

/** Lesson 06: reveal the error-combination rule used by Validated. */
object SemigroupLesson {

  final case class Policy(number: String, annualPremium: BigDecimal)

  def main(args: Array[String]): Unit = {
    val numberErrors: List[String] = List("Policy number is missing")
    val premiumErrors: List[String] = List("Annual premium is missing")
    val expectedErrors = List("Policy number is missing", "Annual premium is missing")

    // Obtain Cats' existing combination rule for whole lists.
    val errorCombiner: Semigroup[List[String]] = Semigroup[List[String]]
    val combined: List[String] = errorCombiner.combine(numberErrors, premiumErrors)

    println("Lesson 06 - The Semigroup behind our validation errors")
    println(s"Number errors: $numberErrors")
    println(s"Premium errors: $premiumErrors")
    println(s"Direct combine: $combined")
    assert(combined == expectedErrors)

    // The same error type lets Validated use that combination rule through mapN.
    val checkedNumber: Validated[List[String], String] =
      Validated.Invalid(numberErrors)
    val checkedPremium: Validated[List[String], BigDecimal] =
      Validated.Invalid(premiumErrors)

    val result: Validated[List[String], Policy] =
      (checkedNumber, checkedPremium).mapN(Policy.apply)

    println(s"Through mapN: $result")
    assert(result == Validated.Invalid(expectedErrors))

    // Expose the Semigroup argument normally supplied through Cats' instances.
    // product pairs two successful values, or combines two errors using this object.
    val paired: Validated[List[String], (String, BigDecimal)] =
      checkedNumber.product(checkedPremium)(errorCombiner)
    val explicitResult: Validated[List[String], Policy] =
      paired.map { case (number, premium) => Policy(number, premium) }

    println(s"Explicit product then map: $explicitResult")
    assert(explicitResult == result)

    // Associativity: changing the grouping preserves the result and input order.
    val customerErrors: List[String] = List("Customer name is missing")
    val groupedLeft = errorCombiner.combine(
      errorCombiner.combine(numberErrors, premiumErrors), customerErrors
    )
    val groupedRight = errorCombiner.combine(
      numberErrors, errorCombiner.combine(premiumErrors, customerErrors)
    )
    val expectedThree = List(
      "Policy number is missing", "Annual premium is missing", "Customer name is missing"
    )

    println(s"Grouped left: $groupedLeft")
    println(s"Grouped right: $groupedRight")
    assert(groupedLeft == expectedThree)
    assert(groupedRight == expectedThree)

    // Associativity does not require swapping the inputs to preserve the result.
    val reversed = errorCombiner.combine(premiumErrors, numberErrors)
    println(s"Inputs swapped: $reversed")
    assert(reversed == List("Annual premium is missing", "Policy number is missing"))

    println("All six lesson checks passed.")
  }
}
