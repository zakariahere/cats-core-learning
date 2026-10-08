# Lesson 11 - Transform prices and keep their container

Lesson 10 folded a List or Vector of cart lines into one CheckoutTotals. Now
we have a different requirement: apply a ten-percent discount to the prices we
have, while keeping the result as a collection or an optional price.

A catalog supplies several prices in a List. Another caller supplies one
possibly missing price in an Option. Both callers should reuse the discount
rule and the same generic function.

## Start from actual prices

```scala
val catalog: List[BigDecimal] = List(BigDecimal(12), BigDecimal(2))
val knownPrice: Option[BigDecimal] = Some(BigDecimal(12))
val missingPrice: Option[BigDecimal] = None
val emptyCatalog: List[BigDecimal] = List.empty[BigDecimal]
```

The amounts in this example use one currency. They are decimal prices rather
than the integer cents used in the previous checkout lesson.

Here is the ordinary transformation of a single price:

```scala
def tenPercentOff(price: BigDecimal): BigDecimal =
  price * BigDecimal("0.90")
```

The string literal constructs the decimal multiplier directly. Our inputs
produce 10.80 and 1.80 exactly; a real pricing rule would also define rounding
when a result has more decimal places.

## The Cats operation

We use the agreed exploration imports, with one Cats syntax bundle:

```scala
import cats._
import cats.data._
import cats.syntax.all._
```

The generic operation is `map`:

```scala
def discountPrices[F[_]](prices: F[BigDecimal])(
    implicit functor: Functor[F]
): F[BigDecimal] =
  prices.map(tenPercentOff)
```

The calls give us:

| Input | Result | Result type |
| --- | --- | --- |
| `List(BigDecimal(12), BigDecimal(2))` | `List(10.80, 1.80)` | `List[BigDecimal]` |
| `Some(BigDecimal(12))`, typed as Option | `Some(10.80)` | `Option[BigDecimal]` |
| `None`, typed as Option | `None` | `Option[BigDecimal]` |
| An empty List of prices | `List()` | `List[BigDecimal]` |

For List, each price changes and the list keeps its order and number of
elements. For Option, a present price changes inside Some. None remains None:
there is no price to transform. The mapping function is not called for None
or for an empty List.

## What Cats contributes

Scala's List and Option already have their own `map` methods. The contribution
here is a common capability we can request for an otherwise unspecified F:
`Functor[F]`.

Inside `discountPrices`, the input is known as `F[BigDecimal]`. Cats' syntax and
the Functor instance make `map` available at that generic call site. Cats supplies
the standard Functor instances for List and Option; we supply the price rule.

The generic map contract is:

```text
Input:          F[A]
Transformation: A => B
Output:         F[B]
```

F stays the same while the contained type may change from A to B. In our
discount example, both A and B happen to be BigDecimal.

The `F[_]` declaration has the same meaning as lesson 10: F accepts one type
argument, and the underscore leaves that parameter unnamed. Here F becomes
List or Option. The explicitly typed Option inputs keep the intended outer
type visible, even when a particular value is Some or None.

## Compare the two capabilities

| Calculation | Input | Output | Required capability |
| --- | --- | --- | --- |
| Summarize the cart (lesson 10) | `F[LineItem]` | `CheckoutTotals` | `Foldable[F]` and `Monoid[CheckoutTotals]` |
| Discount the prices (lesson 11) | `F[BigDecimal]` | `F[BigDecimal]` | `Functor[F]` |

The discount operation transforms values in their context. It does not combine
prices into a total, so it needs no Monoid, no combine rule, and no neutral price.
The Functor instance determines how mapping behaves for the chosen F, including
preserving absence for Option.

Functor and Foldable describe separate capabilities. We are choosing the
capability required by the operation we want to perform.

## Make the owner visible

These direct calls expose the Cats API behind the generic extension syntax:

```scala
Functor[List].map(catalog)(tenPercentOff)
Functor[Option].map(knownPrice)(tenPercentOff)
```

The example checks both against `discountPrices`. Inside the generic function,
`functor.map(prices)(tenPercentOff)` is the corresponding direct form using its
named implicit parameter.

Lawful Functor instances preserve identity and composition. This first lesson
uses Cats' existing instances to focus on what map does; implementing instances
and working through their laws can follow once the operation is familiar.

## Run it

```shell
sbt lesson11
```

Expected output after sbt's build messages:

```text
Lesson 11 - Transform prices and keep their container
List prices: List(10.80, 1.80)
Known price: Some(10.80)
Missing price: None
Empty catalog: List()
Explicit Functor calls agree with the generic function.
All six lesson checks passed.
```

The checks cover the discounted List, present Option, absent Option, empty
List, and agreement with both explicit Functor calls.

Source: [FunctorLesson.scala](../src/main/scala/learning/lesson11/FunctorLesson.scala).
Reference: [Typelevel's Functor documentation](https://typelevel.org/cats/typeclasses/functor.html).
