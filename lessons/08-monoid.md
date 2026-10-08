# Lesson 08 - Combine a batch of error reports, even an empty one

Lesson 07 defined how to combine two `ValidationErrors` values. Now imagine a
batch of already-produced error reports. It might contain three reports, one
report, or none. We want a single `ValidationErrors` result in every case.

The application types remain familiar:

```scala
final case class FieldError(field: String, message: String)
final case class ValidationErrors(items: List[FieldError])
```

## The Cats operation

With the instance defined below, Cats gives us:

```scala
import cats.syntax.all._

val reports: List[ValidationErrors] =
  List(numberErrors, premiumErrors, customerErrors)

val combined: ValidationErrors = reports.combineAll
```

The input is a `List[ValidationErrors]`; the output is one `ValidationErrors`.
The outer list contains reports. The resulting report contains the collected
`FieldError` items, in their original order, including any duplicates.

For an empty outer list, Cats still needs a value to return. The `Semigroup`
contract supplies `combine` without requiring a neutral value: without any
inputs, it cannot supply an `A` on its own. It can combine values whose contents
are empty, and it can return an optional result for an empty batch, as shown below.

## Add a neutral value

`Monoid[A]` extends `Semigroup[A]` with `empty: A`. For our append operation,
a report with no error items is the neutral value:

```scala
import cats.Monoid

implicit val validationErrorsMonoid: Monoid[ValidationErrors] =
  new Monoid[ValidationErrors] {
    override def empty: ValidationErrors = ValidationErrors(Nil)

    override def combine(
        left: ValidationErrors,
        right: ValidationErrors
    ): ValidationErrors =
      ValidationErrors(left.items ++ right.items)
  }
```

The `combine` implementation is unchanged from lesson 07. The new `empty` method
supplies an ordinary `ValidationErrors` value. `Nil` is Scala's empty list.

An arbitrary default would not be enough. The neutral value must satisfy both
identity laws for every report `x`:

```text
combine(empty, x) == x
combine(x, empty) == x
```

Our rule works because `Nil ++ x.items` and `x.items ++ Nil` both produce
`x.items`. The surrounding case class preserves equality. Associativity still
applies, with the same justification from list concatenation as in lesson 07.
The example assertions illustrate these laws; the list reasoning explains them
generally.

## Follow the actual values

The runnable example creates these three errors:

```scala
val numberError = FieldError("number", "is missing")
val premiumError = FieldError("annualPremium", "must be positive")
val customerError = FieldError("customerName", "is missing")

val numberErrors = ValidationErrors(List(numberError))
val premiumErrors = ValidationErrors(List(premiumError))
val customerErrors = ValidationErrors(List(customerError))
```

An explicit accumulation equivalent to `reports.combineAll` in this example is:

```scala
val monoid: Monoid[ValidationErrors] = Monoid[ValidationErrors]

val explicitFold: ValidationErrors =
  reports.foldLeft(monoid.empty) { (accumulated, nextReport) =>
    monoid.combine(accumulated, nextReport)
  }
```

The accumulated report's `items` change like this:

| Step | Accumulated items |
| --- | --- |
| Start with `empty` | `List()` |
| Combine the number report | `List(numberError)` |
| Combine the premium report | `List(numberError, premiumError)` |
| Combine the customer report | `List(numberError, premiumError, customerError)` |

With no reports, there are no combination steps. The result remains the initial
`ValidationErrors(Nil)`. With one report, combining it with `empty` preserves it.

Cats provides the reusable `combineAll` operation; our instance supplies the
starting value and combination rule for this application type. We can use it
without writing another accumulation loop for our error reports. The explicit
fold makes that behavior visible and is checked against Cats' result.

We are collecting error data here. An empty report means no errors were collected;
it does not prove that any validation ran. We do not construct an
`Invalid(ValidationErrors(Nil))` or change the validators from earlier lessons.
The accumulating `Validated` operation from lesson 07 still only needs a
`Semigroup` for its error type.

## Zero reports versus one empty report

These inputs have different meanings:

```scala
val noReports: List[ValidationErrors] = List.empty[ValidationErrors]
val emptyReport: ValidationErrors = ValidationErrors(Nil)
val oneEmptyReport: List[ValidationErrors] = List(emptyReport)
```

`noReports` contains zero reports. `oneEmptyReport` contains one report whose
`items` list has zero errors. A Semigroup can combine that empty report with
another report using our existing append rule.

Cats also provides `combineAllOption` on Semigroup. It returns `None` when no
inputs exist and `Some(combinedValue)` for a nonempty batch. It does not need to
invent a neutral report:

```scala
import cats.Semigroup

val semigroup: Semigroup[ValidationErrors] = validationErrorsMonoid

semigroup.combine(emptyReport, numberErrors)
// ValidationErrors(List(FieldError(number,is missing)))

semigroup.combineAllOption(noReports)
// None

semigroup.combineAllOption(oneEmptyReport)
// Some(ValidationErrors(List()))
```

A Monoid extends Semigroup, so we can view our same instance through the
`Semigroup[ValidationErrors]` type here. The operations above only need that
weaker contract; they do not use `empty`.

| Operation | No reports supplied | One empty report supplied |
| --- | --- | --- |
| Semigroup's `combineAllOption` | `None` | `Some(ValidationErrors(Nil))` |
| Monoid-based `combineAll` | `ValidationErrors(Nil)` | `ValidationErrors(Nil)` |

Choose the optional result when distinguishing absence of reports matters. A
Monoid lets the aggregation return an `A` directly for every batch size, using
the neutral value when the batch is empty.

## Run it

```shell
sbt lesson08
```

Full entry point: `sbt "runMain learning.lesson08.MonoidLesson"`.

The important outputs are:

```text
Three reports: ValidationErrors(List(FieldError(number,is missing), FieldError(annualPremium,must be positive), FieldError(customerName,is missing)))
One report: ValidationErrors(List(FieldError(number,is missing)))
No reports: ValidationErrors(List())
```

`List()` in the printed output is the same empty list written as `Nil` in the
source. The program also prints the unchanged reports when `empty` is combined
on either side and verifies the explicit fold:

```text
Explicit fold agrees with combineAll.
All six lesson checks passed.
```

Source: [MonoidLesson.scala](../src/main/scala/learning/lesson08/MonoidLesson.scala).
The six checks cover multiple reports, one report, zero reports, both identity
laws for the example report, and equivalence with the explicit fold. The
Semigroup comparison above is an additional explanatory snippet, separate from
the runnable program.

Reference: [Typelevel's Monoid documentation](https://typelevel.org/cats/typeclasses/monoid.html).
