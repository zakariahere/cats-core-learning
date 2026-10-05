# Lesson 07 - Teach Cats to combine our validation errors

Lesson 06 exposed the Semigroup argument behind Validated's error accumulation.
Now a form needs each error's field name as well as its message, so it can identify
the input that needs attention. We define application types for those errors.

## Give the errors their own type

```scala
case class FieldError(field: String, message: String)
case class ValidationErrors(items: List[FieldError])
```

For example:

```scala
val numberErrors = ValidationErrors(List(FieldError("number", "is missing")))
val premiumErrors = ValidationErrors(List(FieldError("annualPremium", "must be positive")))
```

The error type `E` in our Validated values is now `ValidationErrors`. Its `items`
member happens to be a list, but the wrapper is a separate type. Cats needs a
`Semigroup[ValidationErrors]` to combine errors during mapN; the list instance
alone does not provide a combination rule for this wrapper.

## Supply the rule explicitly

```scala
import cats.Semigroup

implicit val validationErrorsSemigroup: Semigroup[ValidationErrors] =
  new Semigroup[ValidationErrors] {
    override def combine(
        left: ValidationErrors,
        right: ValidationErrors
    ): ValidationErrors =
      ValidationErrors(left.items ++ right.items)
  }
```

This is an ordinary object implementing Cats' contract. We chose to append the
left error items followed by the right items, then wrap the resulting list.
Field names, message text, order, and duplicate entries are retained.

Calling it directly:

```scala
validationErrorsSemigroup.combine(numberErrors, premiumErrors)
// ValidationErrors(List(
//   FieldError("number", "is missing"),
//   FieldError("annualPremium", "must be positive")
// ))
```

`implicit` makes this implementation available to the compiler's search in this
scope. The data class itself does not need to extend a Cats interface. Here we
keep the data and the instance in the lesson object to make their relationship
easy to see.

## Keep the mapN expression

```scala
import cats.data.Validated
import cats.syntax.all._

case class Policy(number: String, annualPremium: BigDecimal)

val checkedNumber: Validated[ValidationErrors, String] =
  Validated.Invalid(numberErrors)
val checkedPremium: Validated[ValidationErrors, BigDecimal] =
  Validated.Invalid(premiumErrors)

val result: Validated[ValidationErrors, Policy] =
  (checkedNumber, checkedPremium).mapN(Policy.apply)
```

The values are already-computed check results; we are focusing on their combination
rather than repeating lesson 05's validator functions.

The compiler sees `E = ValidationErrors`. Cats' Validated combination instance
requires a `Semigroup[E]`, and our implicit val satisfies that requirement. When
both inputs fail, the combination code calls our implementation with their error
values. The result is:

```scala
Validated.Invalid(ValidationErrors(List(
  FieldError("number", "is missing"),
  FieldError("annualPremium", "must be positive")
)))
```

We can expose that argument just as we did in lesson 06:

```scala
checkedNumber.product(checkedPremium)(validationErrorsSemigroup)
  .map { case (number, premium) => Policy(number, premium) }
```

Two invalid results use our combination rule. One invalid result retains its
errors. Two valid results call `Policy.apply`. Although an execution with two
valid values has no errors to combine, the accumulating operation still requires
the error Semigroup at compile time so it can handle all cases.

## Explain why the rule is associative

Let `a`, `b`, and `c` be ValidationErrors values. The items produced by the two
possible groupings are:

```scala
(a.items ++ b.items) ++ c.items
a.items ++ (b.items ++ c.items)
```

List concatenation gives the same ordered items in both cases. Wrapping either
result in ValidationErrors preserves that equality, so our combine is associative
for any three values. The runnable checks illustrate this reasoning with three
field errors; checking one triple alone would not prove a general law.

Cats and the Scala compiler do not prove this law for arbitrary implementations
we supply. Keeping the operation lawful is our responsibility.

## Run it

```powershell
sbt lesson07
```

Full entry point: `sbt "runMain learning.lesson07.CustomErrorSemigroupLesson"`.

The program checks direct combination, two failures through mapN, equivalence with
the explicit product/map call, each single failure, success, and both groupings of
three errors. The final line is `All eight lesson checks passed.`

Source: [CustomErrorSemigroupLesson.scala](../src/main/scala/learning/lesson07/CustomErrorSemigroupLesson.scala).

References: [Typelevel Semigroup](https://typelevel.org/cats/typeclasses/semigroup.html)
and the [Cats 2.13.0 Validated combination code](https://github.com/typelevel/cats/blob/v2.13.0/core/src/main/scala/cats/data/Validated.scala#L529-L534).

Together, lessons 06-07 explain the helper behind our error accumulation, supply a
rule for our own error type, and justify its associativity. Related articles are
available in the [Cats Core series](https://blog.zakaria.lu/topics/cats-core).
