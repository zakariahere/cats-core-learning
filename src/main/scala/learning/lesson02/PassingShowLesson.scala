package learning.lesson02

import cats.Show

/** Lesson 02: pass both the value and its formatter to a reusable function.
  * This example is self-contained so lesson 01 remains free for practice edits.
  */
object PassingShowLesson {

  final case class Policy(number: String, annualPremium: BigDecimal)
  final case class Customer(name: String)

  final class PolicyShow extends Show[Policy] {
    override def show(value: Policy): String =
      s"Policy ${value.number}: EUR ${value.annualPremium}/year"
  }

  final class CustomerShow extends Show[Customer] {
    override def show(value: Customer): String =
      s"Customer ${value.name}"
  }

  // The same Policy data can be displayed using a different rule.
  final class CompactPolicyShow extends Show[Policy] {
    override def show(value: Policy): String = value.number
  }

  // A connects the value's type to the type the formatter accepts.
  // The caller supplies an ordinary formatter object as the second argument.
  def renderLine[A](value: A, formatter: Show[A]): String = {
    val text: String = formatter.show(value)
    "[record] " + text
  }

  def main(args: Array[String]): Unit = {
    val policy: Policy = Policy("POL-001", BigDecimal("600"))
    val customer: Customer = Customer("Zakaria")

    val policyShow: Show[Policy] = new PolicyShow()
    val customerShow: Show[Customer] = new CustomerShow()
    val compactPolicyShow: Show[Policy] = new CompactPolicyShow()

    // We write [Policy] and [Customer] explicitly to make A visible.
    val policyLine: String = renderLine[Policy](policy, policyShow)
    val customerLine: String = renderLine[Customer](customer, customerShow)
    val compactLine: String = renderLine[Policy](policy, compactPolicyShow)

    println("Lesson 02 - Pass the value and its Show instance")
    println(s"Policy:   $policyLine")
    println(s"Customer: $customerLine")
    println(s"Compact:  $compactLine")

    assert(policyLine == "[record] Policy POL-001: EUR 600/year")
    assert(customerLine == "[record] Customer Zakaria")
    assert(compactLine == "[record] POL-001")

    // Deliberately invalid: CustomerShow does not implement Show[Policy].
    // Uncomment this line to ask the compiler to explain the mismatch,
    // then comment it out again before continuing.
    // renderLine[Policy](policy, customerShow)

    println("All lesson checks passed.")
  }
}
