# Lesson 10 - One checkout summary, different containers

Lesson 09 combined cart lines into a `CheckoutTotals` using `foldMap`. The cart
was a `List[LineItem]`. Now imagine another caller already has a
`Vector[LineItem]`. We want both callers to use the same summary function.

The calculation stays the same: two notebooks at 1200 cents and three pens at
200 cents contribute five items costing 3000 cents in total.

## Call the same function with either container

```scala
val notebooks = LineItem("Notebook", quantity = 2, unitPriceCents = 1200L)
val pens = LineItem("Pen", quantity = 3, unitPriceCents = 200L)

val listCart: List[LineItem] = List(notebooks, pens)
val vectorCart: Vector[LineItem] = Vector(notebooks, pens)

summarize(listCart)   // CheckoutTotals(5,3000)
summarize(vectorCart) // CheckoutTotals(5,3000)
```

The function returns `CheckoutTotals` in both cases. It does not return a List
or a Vector, and does not convert either input to another collection.

## Give Cats the capability to fold the container

We start with our agreed exploration imports:

```scala
import cats._
import cats.data._
import cats.syntax.all._
```

`cats.data._` is unused in this example and can be removed when optimizing
imports. The syntax import makes Cats' extension methods available for discovery.

Keep a single syntax bundle: do not also add `import cats.implicits._` alongside
`cats.syntax.all._`. They overlap, and conflicting syntax imports can make the
compiler report that `foldMap` is not a member even though Foldable is in scope.
Removing the extra import fixes that case; `toTotals` does not need changing.

Here is the new function, using the existing conversion and output Monoid:

```scala
def summarize[F[_]](cart: F[LineItem])(
    implicit foldable: Foldable[F]
): CheckoutTotals =
  cart.foldMap(toTotals)
```

The small piece of new notation is `F[_]`: **F accepts one type argument**.
`List` and `Vector` fit this shape. Applying one of them to `LineItem` gives the
complete input type. The name for this is a *type constructor*.

| Call | F | Input type | Required instance |
| --- | --- | --- | --- |
| `summarize(listCart)` | `List` | `List[LineItem]` | `Foldable[List]` |
| `summarize(vectorCart)` | `Vector` | `Vector[LineItem]` | `Foldable[Vector]` |

The implicit parameter says what capability the function requires from F.
Cats already provides these instances for List and Vector. We do not write
another collection loop or inspect the container's runtime class.

The function body knows its input as `F[LineItem]`. The syntax import provides
`.foldMap`, and `Foldable[F]` makes that operation available for this otherwise
unspecified F. An arbitrary container still needs a suitable Foldable instance.

## Keep the two responsibilities visible

We carry forward the same data and domain rules from lesson 09:

```scala
final case class LineItem(product: String, quantity: Int, unitPriceCents: Long)
final case class CheckoutTotals(itemCount: Int, totalCents: Long)

def toTotals(line: LineItem): CheckoutTotals =
  CheckoutTotals(line.quantity, line.quantity * line.unitPriceCents)

implicit val checkoutTotalsMonoid: Monoid[CheckoutTotals] =
  new Monoid[CheckoutTotals] {
    def empty: CheckoutTotals = CheckoutTotals(0, 0L)

    def combine(a: CheckoutTotals, b: CheckoutTotals): CheckoutTotals =
      CheckoutTotals(a.itemCount + b.itemCount, a.totalCents + b.totalCents)
  }
```

The standalone source defines these in its own lesson object so that changes
to earlier practice files do not change this example.

| Ingredient | Responsibility in this calculation |
| --- | --- |
| `Foldable[F]` | Fold the contents of the input container F |
| `toTotals` | Turn one LineItem into its CheckoutTotals contribution |
| `Monoid[CheckoutTotals]` | Combine contributions and supply the neutral total |

Our output type is fixed as CheckoutTotals, so its Monoid is already available
in the surrounding object. F varies between calls, so we ask for its Foldable
instance as a parameter.

The contribution values are the same for both example carts:

```text
Notebook line -> CheckoutTotals(2, 2400)
Pen line      -> CheckoutTotals(3,  600)
Result        = CheckoutTotals(5, 3000)
```

With no lines in either container, the result is `CheckoutTotals(0, 0L)`.
The neutral value still comes from the output Monoid. Foldable supplies the
ability to fold a container's contents; it does not choose our domain's totals.

## Show the owner behind the syntax

These direct calls make the Cats API easy to find in the IDE:

```scala
Foldable[List].foldMap(listCart)(toTotals)
Foldable[Vector].foldMap(vectorCart)(toTotals)
```

Both are checked against the generic function's results. Inside `summarize`,
we could equivalently write `foldable.foldMap(cart)(toTotals)` using its named
implicit parameter. The output Monoid is still supplied implicitly.

This is the practical contribution of Foldable here: the aggregation can depend
on a folding capability while callers choose List or Vector. A List-only
function remains fine when that is all the application needs.

## Run it

```shell
sbt lesson10
```

Expected output after sbt's build messages:

```text
Lesson 10 - One checkout summary, different containers
List cart: CheckoutTotals(5,3000)
Vector cart: CheckoutTotals(5,3000)
Empty List: CheckoutTotals(0,0)
Empty Vector: CheckoutTotals(0,0)
Explicit Foldable calls agree with the generic function.
All six lesson checks passed.
```

The six checks cover nonempty List and Vector inputs, empty List and Vector
inputs, and agreement with the two explicit Foldable calls.

Source: [FoldableLesson.scala](../src/main/scala/learning/lesson10/FoldableLesson.scala).
Reference: [Typelevel's Foldable documentation](https://typelevel.org/cats/typeclasses/foldable.html).
