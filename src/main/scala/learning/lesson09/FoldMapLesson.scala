package learning.lesson09
import cats.Monoid
import cats.syntax.all._

/** Lesson 09: turn cart lines into checkout totals, then combine the totals. */
object FoldMapLesson {

  final case class LineItem(product: String, quantity: Int, unitPriceCents: Long)
  final case class CheckoutTotals(itemCount: Int, totalCents: Long)

  implicit val checkoutTotalsMonoid: Monoid[CheckoutTotals] =
    new Monoid[CheckoutTotals] {
      override def empty: CheckoutTotals = CheckoutTotals(0, 0L)

      override def combine(
          left: CheckoutTotals,
          right: CheckoutTotals
      ): CheckoutTotals =
        CheckoutTotals(
          left.itemCount + right.itemCount,
          left.totalCents + right.totalCents
        )
    }

  def toTotals(line: LineItem): CheckoutTotals =
    CheckoutTotals(line.quantity, line.quantity * line.unitPriceCents)

  def main(args: Array[String]): Unit = {
    val cart: List[LineItem] = List(
      LineItem("Notebook", quantity = 2, unitPriceCents = 1200L),
      LineItem("Pen", quantity = 3, unitPriceCents = 200L)
    )

    println("Lesson 09 - Map cart lines and combine checkout totals")

    // Cats needs a Monoid for CheckoutTotals, not for the input LineItem.
    val totals: CheckoutTotals = cart.foldMap(toTotals)
    println(s"Cart totals: $totals")
    assert(totals == CheckoutTotals(5, 3000L))

    // Separate the same calculation into familiar map and combineAll steps.
    val lineTotals: List[CheckoutTotals] = cart.map(toTotals)
    println(s"Mapped line totals: $lineTotals")
    assert(totals == lineTotals.combineAll)
    println("map followed by combineAll agrees with foldMap.")

    val singleLine: CheckoutTotals = List(cart.head).foldMap(toTotals)
    println(s"One cart line: $singleLine")
    assert(singleLine == CheckoutTotals(2, 2400L))

    val emptyCart: List[LineItem] = List.empty[LineItem]
    val emptyTotals: CheckoutTotals = emptyCart.foldMap(toTotals)
    println(s"Empty cart: $emptyTotals")
    assert(emptyTotals == CheckoutTotals(0, 0L))

    println("All four lesson checks passed.")
  }
}
