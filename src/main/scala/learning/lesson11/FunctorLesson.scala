package learning.lesson11

// Exploration imports: use this syntax bundle without also importing cats.implicits._.
import cats._
import cats.data._
import cats.syntax.all._

/** Lesson 11: apply one price transformation inside List or Option. */
object FunctorLesson {

  def tenPercentOff(price: BigDecimal): BigDecimal =
    price * BigDecimal("0.90")

  def discountPrices[F[_]](prices: F[BigDecimal])(
      implicit functor: Functor[F]
  ): F[BigDecimal] =
    prices.map(tenPercentOff)

  def main(args: Array[String]): Unit = {
    val catalog: List[BigDecimal] = List(BigDecimal(12), BigDecimal(2))
    val knownPrice: Option[BigDecimal] = Some(BigDecimal(12))
    val missingPrice: Option[BigDecimal] = None
    val emptyCatalog: List[BigDecimal] = List.empty[BigDecimal]

    println("Lesson 11 - Transform prices and keep their container")

    val discountedCatalog: List[BigDecimal] = discountPrices(catalog)
    println(s"List prices: $discountedCatalog")
    assert(discountedCatalog == List(BigDecimal("10.80"), BigDecimal("1.80")))

    val discountedKnown: Option[BigDecimal] = discountPrices(knownPrice)
    println(s"Known price: $discountedKnown")
    assert(discountedKnown == Some(BigDecimal("10.80")))

    val discountedMissing: Option[BigDecimal] = discountPrices(missingPrice)
    println(s"Missing price: $discountedMissing")
    assert(discountedMissing == None)

    val discountedEmpty: List[BigDecimal] = discountPrices(emptyCatalog)
    println(s"Empty catalog: $discountedEmpty")
    assert(discountedEmpty == List.empty[BigDecimal])

    // Make the owner of the generic map operation visible in the IDE.
    val throughListOwner = Functor[List].map(catalog)(tenPercentOff)
    val throughOptionOwner = Functor[Option].map(knownPrice)(tenPercentOff)
    assert(throughListOwner == discountedCatalog)
    assert(throughOptionOwner == discountedKnown)
    println("Explicit Functor calls agree with the generic function.")

    println("All six lesson checks passed.")
  }
}
