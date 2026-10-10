package learning.lesson13

import cats._
import cats.data._
import cats.syntax.all._

/** Lesson 13: combine independent prices and delivery costs with Apply. */
object ApplyLesson {

  def checkoutTotal[F[_]](
      prices: F[BigDecimal],
      deliveryCosts: F[BigDecimal]
  )(implicit applyF: Apply[F]): F[BigDecimal] =
    (prices, deliveryCosts).mapN((price, delivery) => price + delivery)

  private def checkOption(
      label: String,
      price: Option[BigDecimal],
      delivery: Option[BigDecimal],
      expected: Option[BigDecimal]
  ): Unit = {
    val result = checkoutTotal(price, delivery)
    println(s"$label: $result")
    assert(result == expected, s"Unexpected result for $label: $result")
  }

  def main(args: Array[String]): Unit = {
    val price: Option[BigDecimal] = Some(BigDecimal(12))
    val delivery: Option[BigDecimal] = Some(BigDecimal(3))

    println("Lesson 13 - Combine independent inputs with Apply")

    val nested: Option[Option[BigDecimal]] =
      price.map(p => delivery.map(d => p + d))
    println(s"Two nested maps: $nested")

    checkOption("Both present", price, delivery, Some(BigDecimal(15)))
    checkOption("Price missing", None, delivery, None)
    checkOption("Delivery missing", price, None, None)
    checkOption("Both missing", None, None, None)
    checkOption("Free delivery", price, Some(BigDecimal(0)), Some(BigDecimal(12)))

    val prices = List(BigDecimal(12), BigDecimal(20))
    val deliveryCosts = List(BigDecimal(3), BigDecimal(5))
    val combinations: List[BigDecimal] = checkoutTotal(prices, deliveryCosts)
    println(s"All price/delivery combinations: $combinations")
    assert(combinations == List(BigDecimal(15), BigDecimal(17), BigDecimal(23), BigDecimal(25)))

    val noPrices: List[BigDecimal] = checkoutTotal(List.empty[BigDecimal], deliveryCosts)
    val noDeliveryCosts: List[BigDecimal] = checkoutTotal(prices, List.empty[BigDecimal])
    println(s"No prices: $noPrices")
    println(s"No delivery costs: $noDeliveryCosts")
    assert(noPrices.isEmpty)
    assert(noDeliveryCosts.isEmpty)

    // Make the owner visible: map2 combines two contexts with an ordinary function.
    val throughOwner: Option[BigDecimal] =
      Apply[Option].map2(price, delivery)((p, d) => p + d)
    assert(throughOwner == checkoutTotal(price, delivery))
    println("Explicit Apply[Option].map2 agrees with mapN.")

    println("All nine lesson checks passed.")
  }
}
