# Lesson 09 - Turn cart lines into a checkout total with foldMap

In lesson 08, we already had a list of error reports. `combineAll` combined
those reports into one report, using our Monoid.

Now we have shopping-cart lines, and we want a different result: a single
checkout summary containing the number of individual items and the total price.
Two notebooks count as two items, even though they occupy one cart line.

## Start with the actual inputs

```scala
final case class LineItem(product: String, quantity: Int, unitPriceCents: Long)
final case class CheckoutTotals(itemCount: Int, totalCents: Long)

val cart: List[LineItem] = List(
  LineItem("Notebook", quantity = 2, unitPriceCents = 1200L),
  LineItem("Pen", quantity = 3, unitPriceCents = 200L)
)
```

Prices are stored as whole cents; `1200L` is a Scala `Long` literal.
The expected result is `CheckoutTotals(5, 3000L)`: five items costing 3000 cents.
This example assumes already-checked quantities and prices and omits tax and
discounts so that we can focus on aggregation.

## The Cats operation

With the Monoid below in scope, the calculation is:

```scala
import cats.syntax.all._

def toTotals(line: LineItem): CheckoutTotals =
  CheckoutTotals(line.quantity, line.quantity * line.unitPriceCents)

val totals: CheckoutTotals = cart.foldMap(toTotals)
// CheckoutTotals(5,3000)
```

Our function answers: **what does one cart line contribute?**

| Input line | Contribution |
| --- | --- |
| 2 notebooks at 1200 cents each | `CheckoutTotals(2, 2400L)` |
| 3 pens at 200 cents each | `CheckoutTotals(3, 600L)` |

Cats applies this transformation and combines the contributions. For this
pure function over a List, the result is the same as writing:

```scala
val lineTotals: List[CheckoutTotals] = cart.map(toTotals)
val totals: CheckoutTotals = lineTotals.combineAll
```

`foldMap` expresses both steps in one operation without requiring us to build
that intermediate list. It is also possible to write a Scala `foldLeft` for
this job; Cats lets us reuse the combination rule and neutral value already
defined by a Monoid.

## Which type needs a Monoid?

The result type, **CheckoutTotals**, needs it. We do not need a combination rule
for product names or a `Monoid[LineItem]`.

```scala
import cats.Monoid

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
```

Adding zero leaves both fields unchanged. Adding totals field by field is
associative, so this supplies the same Monoid structure as lesson 08 with a new
domain rule. The example uses small values that fit the numeric types.

The accumulation can be read as:

```text
Start:         CheckoutTotals(0,    0)
Add notebooks: CheckoutTotals(2, 2400)
Add pens:      CheckoutTotals(5, 3000)
```

For an empty cart, there are no contributions. Cats returns the Monoid's
`empty`: `CheckoutTotals(0, 0L)`.

The type flow is:

```text
Input:          List[LineItem]
Transformation: LineItem => CheckoutTotals
Combination:    Monoid[CheckoutTotals]
Output:         CheckoutTotals
```

`foldMap` belongs to Cats' `Foldable` type class. For this lesson, its practical
role is simply that Cats knows how to fold a List. Our function describes each
contribution, and our Monoid describes how contributions combine. We can explore
the broader abstraction after this operation is familiar.

## Run it

```shell
sbt lesson09
```

Expected output after sbt's build messages:

```text
Lesson 09 - Map cart lines and combine checkout totals
Cart totals: CheckoutTotals(5,3000)
Mapped line totals: List(CheckoutTotals(2,2400), CheckoutTotals(3,600))
map followed by combineAll agrees with foldMap.
One cart line: CheckoutTotals(2,2400)
Empty cart: CheckoutTotals(0,0)
All four lesson checks passed.
```

The checks cover the cart total, agreement with `map` followed by `combineAll`,
a single cart line, and an empty cart.

Source: [FoldMapLesson.scala](../src/main/scala/learning/lesson09/FoldMapLesson.scala).
Reference: [Typelevel's Foldable documentation](https://typelevel.org/cats/typeclasses/foldable.html).
