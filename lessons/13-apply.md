# Lesson 13 - Combine independent inputs with Apply

```text
Functor only (map inside map)          Apply (map2 / mapN)

Some[12] ─┐                            Some[12] ─┐
          ├─ p + d ─► Some[ Some[15] ]           ├─ p + d ─► Some[15]
Some[3]  ─┘           box in a box     Some[3]  ─┘           one box

map2(fa, fb)(f) = map(product(fa, fb))(f.tupled)
                      └ Semigroupal ┘  └ Functor ┘
product(Some(12), Some(3)) = Some((12, 3))  ->  map  ->  Some(15)
```

Lesson 11 mapped one contextual value, and lesson 12 supplied a Functor for
our own Quote. Now a checkout needs two inputs: a price and a delivery cost.
Both are already available as optional values, and we need both to calculate
the total.

```scala
val price: Option[BigDecimal] = Some(BigDecimal(12))
val delivery: Option[BigDecimal] = Some(BigDecimal(3))
```

These are illustrative decimal amounts in one currency. The desired result
is `Some(15)`; if either amount is missing, the total is `None`.

## Start with the operation we already know

With the agreed exploration imports:

```scala
import cats._
import cats.data._
import cats.syntax.all._

val total: Option[BigDecimal] =
  (price, delivery).mapN((p, d) => p + d)
// Some(15)
```

The lambda receives two ordinary BigDecimal values. Cats handles their outer
Option context. This is the same operation we used to build a Policy in lesson
04; our new task is understanding the capability behind generic combination.

Two nested maps instead produce a different type:

```scala
val nested: Option[Option[BigDecimal]] =
  price.map(p => delivery.map(d => p + d))
// Some(Some(15))
```

The inner map returns an Option, and the outer map wraps that result in another
Option. A Functor can transform one contextual value but does not, by itself,
supply a rule for combining two contexts into one.

## Ask for Apply

Our generic checkout function requests `Apply[F]`:

```scala
def checkoutTotal[F[_]](
    prices: F[BigDecimal],
    deliveryCosts: F[BigDecimal]
)(implicit applyF: Apply[F]): F[BigDecimal] =
  (prices, deliveryCosts).mapN((price, delivery) => price + delivery)
```

Apply extends Functor with the ability to combine contextual inputs. It
supports `map2`, whose shape is:

```text
Inputs:         F[A], F[B]
Transformation: (A, B) => C
Output:         F[C]
```

Both inputs use the same F. Their contained types may differ; this example
uses BigDecimal for A, B, and C.

For two inputs, tuple `mapN` is convenient syntax for this combination. Cats
can also express that syntax through Functor and Semigroupal together; Apply
supplies the capabilities we need here in one type class.

The direct call makes the owner visible:

```scala
Apply[Option].map2(price, delivery)((p, d) => p + d)
// Some(15)
```

Inside our generic function, the corresponding direct form would be
`applyF.map2(prices, deliveryCosts)((p, d) => p + d)`.

The instance decides how contexts combine. Our lambda decides what to do with
the values obtained from them. No Monoid for prices is needed: addition is the
ordinary function we explicitly supply.

## Follow Option's behavior

| Price | Delivery | Total |
| --- | --- | --- |
| `Some(12)` | `Some(3)` | `Some(15)` |
| `None` | `Some(3)` | `None` |
| `Some(12)` | `None` | `None` |
| `None` | `None` | `None` |
| `Some(12)` | `Some(0)` | `Some(12)` |

A missing delivery cost is not free delivery. Zero is a known amount; None
means the amount is unavailable.

"Independent" means both inputs can be supplied without using a successful
value from one to choose or construct the other. It does not promise parallel
execution. These inputs are already constructed values.

## The same function accepts alternatives in List

Suppose every listed price can be paired with every listed delivery cost:

```scala
val prices = List(BigDecimal(12), BigDecimal(20))
val deliveryCosts = List(BigDecimal(3), BigDecimal(5))

checkoutTotal(prices, deliveryCosts)
// List(15, 17, 23, 25)
```

| Price | Delivery | Total |
| --- | --- | --- |
| 12 | 3 | 15 |
| 12 | 5 | 17 |
| 20 | 3 | 23 |
| 20 | 5 | 25 |

Cats' standard List instance produces every combination, in this order. It
does not pair elements by their positions. An empty list on either side
produces an empty result because there are no pairs to combine. Use this
behavior only when every listed pairing makes sense for the application.

Our existing Functor[Quote] supports mapping one Quote. Combining two Quotes
would require an additional decision about their two sources; a Functor
instance alone does not make that decision. We use Cats' existing Apply
instances in this lesson and leave custom Apply implementations for later.

## Run it

```shell
sbt lesson13
```

Expected output after sbt's build messages:

```text
Lesson 13 - Combine independent inputs with Apply
Two nested maps: Some(Some(15))
Both present: Some(15)
Price missing: None
Delivery missing: None
Both missing: None
Free delivery: Some(12)
All price/delivery combinations: List(15, 17, 23, 25)
No prices: List()
No delivery costs: List()
Explicit Apply[Option].map2 agrees with mapN.
All nine lesson checks passed.
```

The nine checks cover five Option outcomes, all List combinations, an empty
List on each side, and agreement with the explicit Apply owner.

Source: [ApplyLesson.scala](../src/main/scala/learning/lesson13/ApplyLesson.scala).
Reference: [Cats 2.13.0 Apply source and examples](https://github.com/typelevel/cats/blob/v2.13.0/core/src/main/scala/cats/Apply.scala).
