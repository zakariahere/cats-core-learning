# Lesson 06 - The rule behind our validation errors

In lesson 05, two invalid fields produced two error messages. This lesson exposes
the operation responsible for joining those messages.

## Start with the actual error values

```scala
val numberErrors: List[String] = List("Policy number is missing")
val premiumErrors: List[String] = List("Annual premium is missing")
```

Plain Scala can concatenate them with `numberErrors ++ premiumErrors`. Cats makes
that behavior available through a common contract so generic code such as
`Validated` can combine an error type without knowing that it is a list.

```scala
import cats.Semigroup

val errorCombiner: Semigroup[List[String]] = Semigroup[List[String]]
val combined: List[String] = errorCombiner.combine(numberErrors, premiumErrors)
```

Result:

```scala
List("Policy number is missing", "Annual premium is missing")
```

`Semigroup[List[String]]` obtains Cats' existing instance. Its `combine` method
concatenates whole lists, retaining message order. It does not join the message
strings into one string or remove duplicate messages.

## Name the contract

The essential method of `Semigroup[A]` has this shape:

```scala
def combine(left: A, right: A): A
```

Here `A` is the entire `List[String]`, so the concrete shape is:

```text
(List[String], List[String]) => List[String]
```

A semigroup pairs this operation with a rule: combination must be associative.
Changing the grouping of three inputs must preserve the result, while keeping
the inputs in the same order:

```text
combine(combine(a, b), c) == combine(a, combine(b, c))
```

For lists, both groupings append the elements of `a`, then `b`, then `c`. The
runnable lesson prints the same three error messages for both groupings. Those
checks demonstrate one case; the law is required for all values of the type.

Swapping the inputs is a different operation. List concatenation preserves their
order, so `combine(a, b)` and `combine(b, a)` can differ. A semigroup does not
require commutativity.

## Connect it to mapN

```scala
import cats.data.Validated
import cats.syntax.all._

case class Policy(number: String, annualPremium: BigDecimal)

val checkedNumber: Validated[List[String], String] =
  Validated.Invalid(numberErrors)
val checkedPremium: Validated[List[String], BigDecimal] =
  Validated.Invalid(premiumErrors)

val result: Validated[List[String], Policy] =
  (checkedNumber, checkedPremium).mapN(Policy.apply)
```

When both inputs are invalid, Validated's combination uses a `Semigroup` for its
error type `E`. Our `E` is `List[String]`, whose rule is list concatenation. That
produces:

```text
Invalid(List(Policy number is missing, Annual premium is missing))
```

`mapN` does not receive our `errorCombiner` local variable explicitly. Cats resolves
the required instance implicitly, just as the explicit `Semigroup[List[String]]`
lookup did. The named variable lets us inspect the operation directly.

### Make the hidden argument visible

We can call Validated's `product` method with the Semigroup supplied explicitly:

```scala
val paired: Validated[List[String], (String, BigDecimal)] =
  checkedNumber.product(checkedPremium)(errorCombiner)

val explicitResult: Validated[List[String], Policy] =
  paired.map { case (number, premium) => Policy(number, premium) }
```

Here `product` combines two checked results. Two successes become a successful
tuple. Two failures become one failure whose contents are combined by the supplied
`errorCombiner`. One failure is retained unchanged. The subsequent `map` constructs
a Policy only for a successful tuple; an Invalid passes through unchanged.

This has the same result as our two-input `mapN`; it exposes the underlying
combination operation, not a literal compiler rewrite of the syntax.

The key two-failure branch, with its parameter renamed for clarity, is:

```scala
case (Validated.Invalid(leftErrors), Validated.Invalid(rightErrors)) =>
  Validated.Invalid(errorCombiner.combine(leftErrors, rightErrors))
```

For our values, `leftErrors` is `List("Policy number is missing")` and
`rightErrors` is `List("Annual premium is missing")`. The combined list is wrapped
in Invalid again. No Policy is built on this path.

With `mapN`, the compiler resolves Cats' combination instance for Validated, which
requires a `Semigroup[E]`. For `E = List[String]`, Cats supplies the list instance.
Our plain `val errorCombiner` is only used when passed explicitly above; its name
does not make it available to implicit search.

Verified against [Cats 2.13.0's implementation](https://github.com/typelevel/cats/blob/v2.13.0/core/src/main/scala/cats/data/Validated.scala#L529-L534).

The roles are distinct: our validators decide which fields fail; Validated handles
the success/failure cases; the error semigroup combines two failures' contents.
The success function `Policy.apply` runs only when both values are valid.

We need `Semigroup[List[String]]` for these errors, not `Semigroup[Policy]`.
There are no two policies to merge: the successful case constructs one policy
from a number and a premium.

## Run it

```powershell
sbt lesson06
```

Full entry point: `sbt "runMain learning.lesson06.SemigroupLesson"`.

Expected application output:

```text
Lesson 06 - The Semigroup behind our validation errors
Number errors: List(Policy number is missing)
Premium errors: List(Annual premium is missing)
Direct combine: List(Policy number is missing, Annual premium is missing)
Through mapN: Invalid(List(Policy number is missing, Annual premium is missing))
Explicit product then map: Invalid(List(Policy number is missing, Annual premium is missing))
Grouped left: List(Policy number is missing, Annual premium is missing, Customer name is missing)
Grouped right: List(Policy number is missing, Annual premium is missing, Customer name is missing)
Inputs swapped: List(Annual premium is missing, Policy number is missing)
All six lesson checks passed.
```

Source: [SemigroupLesson.scala](../src/main/scala/learning/lesson06/SemigroupLesson.scala).

References: [Typelevel Semigroup](https://typelevel.org/cats/typeclasses/semigroup.html)
and [Validated](https://typelevel.org/cats/datatypes/validated.html).

Stop here first. Once the contract is clear, the next small step can be defining
the combination rule for our own error type.
