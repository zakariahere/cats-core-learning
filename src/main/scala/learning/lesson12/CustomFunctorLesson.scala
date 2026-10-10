package learning.lesson12

import cats._
import cats.data._
import cats.syntax.all._

// Reuse the actual lesson 11 functions, without copying or changing them.
import learning.lesson11.FunctorLesson.{discountPrices, tenPercentOff}

/** Lesson 12: teach Cats to transform the value inside our own Quote type. */
object CustomFunctorLesson {

  final case class Quote[A](value: A, source: String)

  implicit val quoteFunctor: Functor[Quote] = new Functor[Quote] {
    override def map[A, B](fa: Quote[A])(f: A => B): Quote[B] =
      Quote(f(fa.value), fa.source)
  }

  def main(args: Array[String]): Unit = {
    val original: Quote[BigDecimal] = Quote(BigDecimal(12), "catalog")

    println("Lesson 12 - Teach Cats to map our own Quote")
    println(s"Original: $original")

    // F is now Quote; the function from lesson 11 only requires Functor[F].
    val discounted: Quote[BigDecimal] = discountPrices(original)
    println(s"Existing discountPrices: $discounted")
    assert(discounted == Quote(BigDecimal("10.80"), "catalog"))
    assert(original == Quote(BigDecimal(12), "catalog"))

    val throughOwner: Quote[BigDecimal] =
      Functor[Quote].map(original)(tenPercentOff)
    assert(throughOwner == discounted)
    println("Explicit Functor[Quote].map agrees with discountPrices.")

    val discount: BigDecimal => BigDecimal = tenPercentOff
    val label: BigDecimal => String = price => s"EUR $price"

    val formatted: Quote[String] = discounted.map(label)
    println(s"Changed inner type: $formatted")
    assert(formatted == Quote("EUR 10.80", "catalog"))

    // Identity: transforming a value into itself preserves the whole Quote.
    val unchanged: Quote[BigDecimal] = original.map(price => price)
    println(s"Identity: $unchanged")
    assert(unchanged == original)

    // Composition: two maps and one composed map give the same complete Quote.
    val twoMaps: Quote[String] = original.map(discount).map(label)
    val oneMap: Quote[String] = original.map(discount.andThen(label))
    println(s"Two maps: $twoMaps")
    println(s"One composed map: $oneMap")
    assert(twoMaps == oneMap)

    println("All six lesson checks passed.")
  }
}
