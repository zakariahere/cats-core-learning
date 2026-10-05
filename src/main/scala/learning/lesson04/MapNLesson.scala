package learning.lesson04

import cats.syntax.all._

/** Lesson 04: combine two independent optional fields with Cats mapN.
  * Both inputs must be present before we can construct a Policy.
  */
object MapNLesson {

  final case class Policy(number: String, annualPremium: BigDecimal)

  // Familiar Scala: this has the same results as the Cats version below.
  def withScala(
      number: Option[String],
      premium: Option[BigDecimal]
  ): Option[Policy] =
    for {
      n <- number
      p <- premium
    } yield Policy(n, p)

  // Cats supplies mapN on the tuple. The lambda receives String and BigDecimal.
  def withCats(
      number: Option[String],
      premium: Option[BigDecimal]
  ): Option[Policy] =
    (number, premium).mapN { (n, p) =>
      Policy(n, p)
    }

  // The construction function can also be written as Policy.apply.
  def withCatsShorthand(
      number: Option[String],
      premium: Option[BigDecimal]
  ): Option[Policy] =
    (number, premium).mapN(Policy.apply)

  private def checkCase(
      label: String,
      number: Option[String],
      premium: Option[BigDecimal],
      expected: Option[Policy]
  ): Unit = {
    val scalaResult: Option[Policy] = withScala(number, premium)
    val catsResult: Option[Policy] = withCats(number, premium)
    val shorthandResult: Option[Policy] = withCatsShorthand(number, premium)

    println(s"$label: $catsResult")
    assert(scalaResult == expected, s"Scala result for $label: $scalaResult")
    assert(catsResult == expected, s"Cats result for $label: $catsResult")
    assert(shorthandResult == expected, s"Shorthand result for $label: $shorthandResult")
  }

  def main(args: Array[String]): Unit = {
    val number: Option[String] = Some("POL-001")
    val premium: Option[BigDecimal] = Some(BigDecimal("600"))

    println("Lesson 04 - Build a Policy with Cats mapN")
    checkCase("Both present", number, premium, Some(Policy("POL-001", BigDecimal("600"))))
    checkCase("Number missing", None, premium, None)
    checkCase("Premium missing", number, None, None)
    checkCase("Both missing", None, None, None)
    println("Scala, Cats, and shorthand agree in all four cases.")
    println("All lesson checks passed.")
  }
}
