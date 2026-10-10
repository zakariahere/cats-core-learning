# Lesson 14 - Start a generic total with Applicative.pure

```text
cart = [ Some[12], Some[3], Some[5] ]

pure(0)                     map2        map2        map2
Some[0] ──(+ Some[12])──► Some[12] ──(+ Some[3])──► Some[15] ──(+ Some[5])──► Some[20]
   ▲
   └── the starting box: Apply cannot make it, Applicative.pure can

empty cart = [ ]   ──►   Some[0]      (just the starting box)

Semigroup ──► Monoid          adds  empty   (a starting VALUE:  0)
Apply     ──► Applicative     adds  pure    (a starting BOX:    Some(0), List(0))
```

Lesson 13 combined two contextual amounts with `mapN`. A real cart has any
number of lines, and every line total might be unavailable:

```scala
val cart: List[Option[BigDecimal]] =
  List(Some(BigDecimal(12)), Some(BigDecimal(3)), Some(BigDecimal(5)))
```

The desired total is `Some(20)`. A line that is `None` makes the total `None`.
An empty cart has a known total: `Some(0)`.

## Where Apply gets stuck

Folding the cart with `mapN` needs a starting box, the running total before
any line was added. In code that only knows `Apply[F]`, nothing can build that
box: `Some(0)` would only work for Option, and we do not know what `F` is.
Apply can combine boxes that already exist, but it cannot create one.

## Ask for Applicative

```scala
def orderTotal[F[_]](lineTotals: List[F[BigDecimal]])(implicit applicativeF: Applicative[F]): F[BigDecimal] =
  lineTotals.foldLeft(applicativeF.pure(BigDecimal(0))) { (runningTotal, lineTotal) =>
    (runningTotal, lineTotal).mapN((total, line) => total + line)
  }
```

Applicative extends Apply and adds one method:

```text
pure: A => F[A]
```

`pure` puts one known value into the box, in the most neutral way for that box:

| F | `pure(BigDecimal(0))` | Meaning |
| --- | --- | --- |
| Option | `Some(0)` | present, not missing |
| List | `List(0)` | exactly one alternative |

Direct call and syntax:

```scala
Applicative[Option].pure(BigDecimal(0))   // Some(0)
BigDecimal(0).pure[List]                  // List(0)
```

Careful: `pure(0)` is not "empty". `List(0)` has one element; `List()` would
make every later `mapN` produce `List()`, because there would be nothing to pair.

## Trace

| Cart | F | Result |
| --- | --- | --- |
| `Some(12), Some(3), Some(5)` | Option | `Some(20)` |
| `Some(12), None, Some(5)` | Option | `None` |
| empty | Option | `Some(0)` |
| `List(12, 20), List(3, 5)` | List | `List(15, 17, 23, 25)` |
| empty | List | `List(0)` |

The same parallel as lessons 06-08: Monoid gave Semigroup a starting value
(`empty`) so `combineAll` works on an empty list. Applicative gives Apply a
starting box (`pure`) so a fold over boxes works on an empty list.

## Run it

```shell
sbt lesson14
```

Expected output after sbt's build messages:

```text
Lesson 14 - Start a generic total with Applicative.pure
pure in Option: Some(0)
pure in List: List(0)
All lines known: Some(20)
One line unknown: None
Empty cart: Some(0)
All alternatives: List(15, 17, 23, 25)
No lines as List: List(0)
All seven lesson checks passed.
```

Source: [ApplicativeLesson.scala](../src/main/scala/learning/lesson14/ApplicativeLesson.scala).
Reference: [Cats 2.13.0 Applicative source](https://github.com/typelevel/cats/blob/v2.13.0/core/src/main/scala/cats/Applicative.scala).
