# Lesson 12 - Teach Cats to map our own Quote

Lesson 11 gave us `discountPrices`: a function that transforms prices inside
any `F` for which a `Functor[F]` is available. We used List and Option, whose
instances Cats already supplies. Now our application has a new requirement:
keep a price together with the source that supplied it.

```scala
final case class Quote[A](value: A, source: String)

val original: Quote[BigDecimal] = Quote(BigDecimal(12), "catalog")
```

We want to discount the value and retain its source:

```text
Quote(12,catalog) -> Quote(10.80,catalog)
```

The result is a new Quote; the original value remains unchanged. As in lesson
11, prices are decimal amounts in one currency. This example does not define
production rounding or currency conversion rules.

## Use the existing operation

The runnable lesson imports the actual functions from lesson 11:

```scala
import learning.lesson11.FunctorLesson.{discountPrices, tenPercentOff}
```

With the instance below in scope, the existing function accepts our new type:

```scala
val discounted: Quote[BigDecimal] = discountPrices(original)
// Quote(10.80,catalog)
```

`F` is now `Quote`. The function from lesson 11 still has this exact body:

```scala
def discountPrices[F[_]](prices: F[BigDecimal])(
    implicit functor: Functor[F]
): F[BigDecimal] =
  prices.map(tenPercentOff)
```

It never mentions Quote or its fields. The new instance supplies the mapping
behavior for Quote. The standalone lesson 12 entry point reuses lesson 11's
functions, so edits to those shared functions also affect this example.

## Supply the mapping rule

Use the agreed exploration imports, with one Cats syntax bundle:

```scala
import cats._
import cats.data._
import cats.syntax.all._
```

Our instance implements the operation required by `Functor`:

```scala
implicit val quoteFunctor: Functor[Quote] = new Functor[Quote] {
  override def map[A, B](fa: Quote[A])(f: A => B): Quote[B] =
    Quote(f(fa.value), fa.source)
}
```

`Functor[Quote]` describes mapping for Quote with any contained type. That is
why it takes `Quote`, rather than a particular `Quote[BigDecimal]`.

In `map`, `fa` is the incoming quote and `f` is the transformation supplied by
the caller. For our discount call, follow the pieces:

| Piece | Concrete value or type |
| --- | --- |
| `A` | `BigDecimal` |
| `B` | `BigDecimal` |
| `fa.value` | `12` |
| `f` | `tenPercentOff` |
| `f(fa.value)` | `10.80` |
| `fa.source` | `"catalog"` |
| New `Quote[B]` | `Quote(10.80,catalog)` |

Our implementation applies the supplied function to the value, then constructs
a new Quote using the original source. The discount rule stays outside the
instance: this same instance must also support other transformations.

The direct Cats call makes the type-class owner visible:

```scala
Functor[Quote].map(original)(tenPercentOff)
```

The program checks that this produces the same result as `discountPrices`.
Quote itself declares no `map` method: Cats' syntax uses our available
`Functor[Quote]` instance to support calls such as `original.map(tenPercentOff)`.

## The inner type can change

The instance's `A` and `B` can be different. Format the discounted price as text:

```scala
val label: BigDecimal => String = price => s"EUR $price"
val formatted: Quote[String] = discounted.map(label)
// Quote(EUR 10.80,catalog)
```

Here `Quote[BigDecimal]` becomes `Quote[String]`. The outer type and source are
preserved; the inner value and its type change. This is a simple label for the
example, rather than a general money formatter.

## Two rules keep this behavior dependable

A Functor instance must satisfy identity and composition for pure, total
mapping functions. The program illustrates both with concrete values; those
assertions alone are not a proof for every input and function.

**Identity:** mapping each value to itself preserves the whole quote.

```scala
original.map(price => price) == original
```

For any `Quote(value, source)`, our implementation produces
`Quote(value, source)` again. Equality here is value equality, not object identity.
An implementation that appended `" mapped"` to the source on every call would
break this law even when the value transformation did nothing.

**Composition:** applying two transformations in two maps agrees with applying
their composition in one map.

```scala
val discount: BigDecimal => BigDecimal = tenPercentOff

original.map(discount).map(label)
// Quote(EUR 10.80,catalog)

original.map(discount.andThen(label))
// Quote(EUR 10.80,catalog)
```

`discount.andThen(label)` means: first discount, then label the result. Both
paths change `12` into `10.80`, then into `"EUR 10.80"`.

For any pure functions `f` and `g`, both paths through our implementation yield
`Quote(g(f(value)), source)`. The unchanged source and identical transformed
value explain why this instance satisfies composition generally.

This is the same integration pattern as our custom Monoid: define a lawful
instance for an application type, then reuse functions written against that
capability. Here the capability is mapping; no combination rule or empty value
is needed.

## Run it

```shell
sbt lesson12
```

Full entry point: `sbt "runMain learning.lesson12.CustomFunctorLesson"`.

Expected output after sbt's build messages:

```text
Lesson 12 - Teach Cats to map our own Quote
Original: Quote(12,catalog)
Existing discountPrices: Quote(10.80,catalog)
Explicit Functor[Quote].map agrees with discountPrices.
Changed inner type: Quote(EUR 10.80,catalog)
Identity: Quote(12,catalog)
Two maps: Quote(EUR 10.80,catalog)
One composed map: Quote(EUR 10.80,catalog)
All six lesson checks passed.
```

The six checks cover the discounted quote including its source, the unchanged
original, agreement with the explicit owner, a changed inner type, identity,
and composition on the example values.

Source: [CustomFunctorLesson.scala](../src/main/scala/learning/lesson12/CustomFunctorLesson.scala).
Shared function: [FunctorLesson.scala](../src/main/scala/learning/lesson11/FunctorLesson.scala).
Reference: [Typelevel's Functor documentation](https://typelevel.org/cats/typeclasses/functor.html).
