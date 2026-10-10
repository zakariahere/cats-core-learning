package learning.lesson14

import cats._
import cats.data._
import cats.syntax.all._

/** Lesson 14: start a generic order total from a known zero with Applicative.pure. */
object ApplicativeLesson {

  // Apply (lesson 13) combines two boxes; Applicative adds pure for the starting box.
  def orderTotal[F[_]](lineTotals: List[F[BigDecimal]])(implicit applicativeF: Applicative[F]): F[BigDecimal] =
    lineTotals.foldLeft(applicativeF.pure(BigDecimal(0))) { (runningTotal, lineTotal) =>
      (runningTotal, lineTotal).mapN((total, line) => total + line)
    }

  def main(args: Array[String]): Unit = {
    println("Lesson 14 - Start a generic total with Applicative.pure")

    // pure puts one known value into the box: no missing value, exactly one alternative.
    val zeroOption: Option[BigDecimal] = Applicative[Option].pure(BigDecimal(0))
    val zeroList: List[BigDecimal] = BigDecimal(0).pure[List]
    println(s"pure in Option: $zeroOption")
    println(s"pure in List: $zeroList")
    assert(zeroOption == Some(BigDecimal(0)))
    assert(zeroList == List(BigDecimal(0)))

    val cart: List[Option[BigDecimal]] =
      List(Some(BigDecimal(12)), Some(BigDecimal(3)), Some(BigDecimal(5)))
    val total: Option[BigDecimal] = orderTotal(cart)
    println(s"All lines known: $total")
    assert(total == Some(BigDecimal(20)))

    val withUnknownLine: Option[BigDecimal] =
      orderTotal(List(Some(BigDecimal(12)), None, Some(BigDecimal(5))))
    println(s"One line unknown: $withUnknownLine")
    assert(withUnknownLine == None)

    // The empty cart is where Apply alone gets stuck: there is no box to start from.
    val emptyCart: Option[BigDecimal] = orderTotal(List.empty[Option[BigDecimal]])
    println(s"Empty cart: $emptyCart")
    assert(emptyCart == Some(BigDecimal(0)))

    // Same function, List context: every price paired with every delivery cost.
    val alternatives: List[BigDecimal] =
      orderTotal(List(List(BigDecimal(12), BigDecimal(20)), List(BigDecimal(3), BigDecimal(5))))
    println(s"All alternatives: $alternatives")
    assert(alternatives == List(BigDecimal(15), BigDecimal(17), BigDecimal(23), BigDecimal(25)))

    val noAlternatives: List[BigDecimal] = orderTotal(List.empty[List[BigDecimal]])
    println(s"No lines as List: $noAlternatives")
    assert(noAlternatives == List(BigDecimal(0)))

    println("All seven lesson checks passed.")
  }
}
