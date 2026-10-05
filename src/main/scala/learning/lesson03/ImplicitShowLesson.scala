package learning.lesson03

import cats.Show

/** Lesson 03: Scala supplies an ordinary formatter argument at the call site.
  * Read the three method signatures in order, then compare their calls in main.
  */
object ImplicitShowLesson {

  final case class Policy(number: String, annualPremium: BigDecimal)

  final class PolicyShow extends Show[Policy] {
    override def show(value: Policy): String =
      s"Policy ${value.number}: EUR ${value.annualPremium}/year"
  }

  final class CompactPolicyShow extends Show[Policy] {
    override def show(value: Policy): String = value.number
  }

  // Step 1. The familiar method from lesson 02: one parameter list.
  def renderExplicit[A](value: A, formatter: Show[A]): String =
    "[record] " + formatter.show(value)

  // Step 2. Two parameter lists; both arguments are still supplied explicitly.
  def renderInTwoLists[A](value: A)(formatter: Show[A]): String =
    "[record] " + formatter.show(value)

  // Step 3. The last list is implicit. The body uses formatter exactly as before.
  def renderLine[A](value: A)(implicit formatter: Show[A]): String =
    "[record] " + formatter.show(value)

  def main(args: Array[String]): Unit = {
    val policy: Policy = Policy("POL-001", BigDecimal("600"))

    // This is an ordinary object, also available for implicit argument lookup.
    // Its type Show[Policy] makes it a candidate for the parameter above.
    implicit val policyShow: Show[Policy] = new PolicyShow()

    // Kept explicit so we choose compact formatting deliberately for one call.
    val compactPolicyShow: Show[Policy] = new CompactPolicyShow()

    val oneList: String = renderExplicit[Policy](policy, policyShow)
    val twoLists: String = renderInTwoLists[Policy](policy)(policyShow)
    val explicitImplicit: String = renderLine[Policy](policy)(policyShow)

    // At this call site, the compiler finds the implicit val policyShow.
    // The call is equivalent to renderLine[Policy](policy)(policyShow).
    val insertedArgument: String = renderLine[Policy](policy)

    // An implicit parameter can still be supplied explicitly.
    val compact: String = renderLine[Policy](policy)(compactPolicyShow)

    println("Lesson 03 - The compiler supplies the formatter argument")
    println(s"One list:           $oneList")
    println(s"Two lists:          $twoLists")
    println(s"Implicit, explicit: $explicitImplicit")
    println(s"Implicit, omitted:  $insertedArgument")
    println(s"Explicit choice:    $compact")

    val expected: String = "[record] Policy POL-001: EUR 600/year"
    assert(oneList == expected)
    assert(twoLists == expected)
    assert(explicitImplicit == expected)
    assert(insertedArgument == expected)
    assert(compact == "[record] POL-001")

    // Optional compiler experiment: remove `implicit` from policyShow above.
    // The call that omits the formatter then fails to compile; explicit calls
    // still have the argument they need. Restore it before continuing.
    println("All lesson checks passed.")
  }
}
