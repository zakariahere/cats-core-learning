package learning.lesson01

import cats.Show

/** Lesson 01: a separate object knows how to turn a Policy into readable text.
  * Start with our own interface, then use the Cats interface for the same job.
  * Everything here is an explicit object construction or ordinary method call.
  */
object ExplicitShowLesson {

  final case class Policy(number: String, annualPremium: BigDecimal)

  // Step 1. Like a Java interface Formatter<A>.
  // A means "the type of value this formatter accepts".
  trait Formatter[A] {
    def format(value: A): String
  }

  // Step 2. An ordinary implementation for one particular type: Policy.
  final class PolicyFormatter extends Formatter[Policy] {
    override def format(value: Policy): String =
      s"Policy ${value.number}: EUR ${value.annualPremium}/year"
  }

  // Step 3. Cats already provides this kind of contract as Show[A].
  // Its method is called show. We still write an ordinary implementation.
  final class PolicyShow extends Show[Policy] {
    override def show(value: Policy): String =
      s"Policy ${value.number}: EUR ${value.annualPremium}/year"
  }

  def main(args: Array[String]): Unit = {
    val policy: Policy = Policy("POL-001", BigDecimal("600"))

    val ourFormatter: Formatter[Policy] = new PolicyFormatter()
    val catsFormatter: Show[Policy] = new PolicyShow()

    val ourText: String = ourFormatter.format(policy)
    val catsText: String = catsFormatter.show(policy)

    println("Lesson 01 - An explicit formatter, then Cats Show")
    println(s"Scala version: ${scala.util.Properties.versionNumberString}")
    println(s"Input: $policy")
    println(s"Our Formatter: $ourText")
    println(s"Cats Show:     $catsText")

    assert(ourText == "Policy POL-001: EUR 600/year")
    assert(catsText == ourText)

    // A different value goes through the same formatter object.
    val secondPolicy: Policy = Policy("POL-002", BigDecimal("840.50"))
    val secondText: String = catsFormatter.show(secondPolicy)
    println(s"Second input:  $secondPolicy")
    println(s"Cats Show:     $secondText")
    assert(secondText == "Policy POL-002: EUR 840.50/year")

    println("All lesson checks passed.")
  }
}
