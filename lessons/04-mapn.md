# Lesson 04 - Build a value from independent optional fields

Our requirement: construct a Policy when both its number and annual premium are
available. Each field may be absent, so the inputs use Option.

```scala
final case class Policy(number: String, annualPremium: BigDecimal)

val number: Option[String] = Some("POL-001")
val premium: Option[BigDecimal] = Some(BigDecimal("600"))
```

The two inputs are independent here: we already have both optional values, and
obtaining one does not require extracting the value inside the other.

## The familiar Scala solution

```scala
val result: Option[Policy] =
  for {
    n <- number
    p <- premium
  } yield Policy(n, p)
```

This is valid Scala and remains a good solution. Cats offers a direct way to express
combining these inputs with a function.

## The Cats operation

```scala
import cats.syntax.all._

val result: Option[Policy] =
  (number, premium).mapN { (n, p) =>
    Policy(n, p)
  }
```

The tuple groups the two inputs. The import enables Cats' `mapN` syntax. Cats has
the Option instances needed for this operation; we do not write a custom instance.

Follow the types and values:

| Part | Type | Value in this example |
| --- | --- | --- |
| `number` | `Option[String]` | `Some("POL-001")` |
| `premium` | `Option[BigDecimal]` | `Some(BigDecimal("600"))` |
| `n` inside the function | `String` | `"POL-001"` |
| `p` inside the function | `BigDecimal` | `600` |
| `Policy(n, p)` | `Policy` | `Policy("POL-001", 600)` |
| Whole `mapN` expression | `Option[Policy]` | `Some(Policy("POL-001", 600))` |

With these two Options, Cats calls our function when both values are present and
returns the constructed policy inside Some. If either is absent, the result is None
and the construction function is not called.

| Number | Premium | Result |
| --- | --- | --- |
| `Some("POL-001")` | `Some(600)` | `Some(Policy("POL-001", 600))` |
| `None` | `Some(600)` | `None` |
| `Some("POL-001")` | `None` | `None` |
| `None` | `None` | `None` |

The lambda receives plain String and BigDecimal values. Its result is a plain Policy.
Cats handles the Option combination around that function.

The shorter constructor form is equivalent:

```scala
(number, premium).mapN(Policy.apply)
```

Here `Policy.apply` supplies the same two-argument construction function.

## Where mapN helps

Use this shape when several independent values in the same kind of context must
be combined by one function. A normal Option.map handles one wrapped input;
mapN can combine several. With a third optional input, the syntax becomes
`(first, second, third).mapN((a, b, c) => ...)`.

The library provides this operation across supported contexts, with behavior determined
by their type class instances. This example specifically demonstrates Option's
all-values-present requirement. Independence describes data dependencies; mapN alone
does not promise parallel execution.

When a later input must be computed using an earlier unwrapped value, flatMap or a
for comprehension naturally expresses that dependency. We will compare those cases
when introducing the underlying abstractions.

## Run it

```powershell
sbt lesson04
```

Full command: `sbt "runMain learning.lesson04.MapNLesson"`.

Expected application output:

```text
Lesson 04 - Build a Policy with Cats mapN
Both present: Some(Policy(POL-001,600))
Number missing: None
Premium missing: None
Both missing: None
Scala, Cats, and shorthand agree in all four cases.
All lesson checks passed.
```

Source: [MapNLesson.scala](../src/main/scala/learning/lesson04/MapNLesson.scala).
All four cases compare the Scala implementation, the explicit Cats lambda, and the
constructor shorthand against their expected results.

For now, focus on what mapN receives, what the function receives, and what comes
back. The next practical question is how to preserve explanations for missing or
invalid fields, which Option alone cannot carry.

Reference: [Typelevel's mapN introduction](https://typelevel.org/cats/jump_start_guide.html#tuples).
