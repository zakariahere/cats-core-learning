package learning.lesson10

// Our exploration imports; unused imports can be removed after experimenting.
import cats._
import cats.data._
import cats.syntax.all._

/** Lesson 10: one checkout summary function for different foldable containers. */
object FoldableLesson {

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

  // F is the container type constructor, such as List or Vector.
  def summarize[F[_]](cart: F[LineItem])(
      implicit foldable: Foldable[F]
  ): CheckoutTotals =
    cart.foldMap(toTotals)

  def main(args: Array[String]): Unit = {
    val notebooks = LineItem("Notebook", quantity = 2, unitPriceCents = 1200L)
    val pens = LineItem("Pen", quantity = 3, unitPriceCents = 200L)

    val listCart: List[LineItem] = List(notebooks, pens)
    val vectorCart: Vector[LineItem] = Vector(notebooks, pens)
    val expected = CheckoutTotals(5, 3000L)

    println("Lesson 10 - One checkout summary, different containers")

    // F = List: Cats supplies Foldable[List].
    val listTotals: CheckoutTotals = summarize(listCart)
    println(s"List cart: $listTotals")
    assert(listTotals == expected)

    // F = Vector: Cats supplies Foldable[Vector].
    val vectorTotals: CheckoutTotals = summarize(vectorCart)
    println(s"Vector cart: $vectorTotals")
    assert(vectorTotals == expected)

    val emptyListTotals = summarize(List.empty[LineItem])
    val emptyVectorTotals = summarize(Vector.empty[LineItem])
    println(s"Empty List: $emptyListTotals")
    println(s"Empty Vector: $emptyVectorTotals")
    assert(emptyListTotals == CheckoutTotals(0, 0L))
    assert(emptyVectorTotals == CheckoutTotals(0, 0L))

    // Expose the owner of the foldMap extension method for discovery.
    val throughListOwner = Foldable[List].foldMap(listCart)(toTotals)
    val throughVectorOwner = Foldable[Vector].foldMap(vectorCart)(toTotals)
    assert(throughListOwner == listTotals)
    assert(throughVectorOwner == vectorTotals)
    println("Explicit Foldable calls agree with the generic function.")

    println("All six lesson checks passed.")
  }
}
