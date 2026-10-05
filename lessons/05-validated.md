# Lesson 05 - Collect useful errors with Validated

Lesson 04 produced None when either required field was absent. Now imagine a form
submission: we want the caller to see which fields need fixing, including both
messages when both fields fail their checks.

## A result that carries the explanation

Cats provides `Validated[E, A]`:

- `E` is the type of the error information.
- `A` is the type of the successful value.
- `Validated.Valid(value)` holds a successful value.
- `Validated.Invalid(errors)` holds error information.

For our policy number, we choose:

```scala
Validated[List[String], String]
```

Read it as "a checked String, or a list of error messages". For the premium we use
`Validated[List[String], BigDecimal]`. The completed policy has type
`Validated[List[String], Policy]`.

We choose an ordinary List to keep the error values familiar. Every invalid result
created by this example includes at least one message, though List itself allows
an empty list.

## Keep the same mapN operation

```scala
import cats.data.Validated
import cats.syntax.all._

val checkedNumber: Validated[List[String], String] =
  Validated.Invalid(List("Policy number is missing"))

val checkedPremium: Validated[List[String], BigDecimal] =
  Validated.Invalid(List("Annual premium is missing"))

val result: Validated[List[String], Policy] =
  (checkedNumber, checkedPremium).mapN(Policy.apply)
```

The result is:

```text
Invalid(List(Policy number is missing, Annual premium is missing))
```

Cats' Validated combination joins the error lists from both invalid inputs. Our
construction function is called only when both inputs are Valid.

| Checked number | Checked premium | Combined result |
| --- | --- | --- |
| `Valid("POL-001")` | `Valid(600)` | `Valid(Policy("POL-001", 600))` |
| `Invalid(List("number error"))` | `Valid(600)` | `Invalid(List("number error"))` |
| `Valid("POL-001")` | `Invalid(List("premium error"))` | `Invalid(List("premium error"))` |
| `Invalid(List("number error"))` | `Invalid(List("premium error"))` | `Invalid(List("number error", "premium error"))` |

The syntax is the same operation we used with Option. Its behavior follows the
surrounding type: Option returned None for missing data; Validated retains and
combines error information. The concrete List error type lets Cats concatenate the
messages in input order.

## Turn raw inputs into those results

The runnable example includes two independent functions:

```scala
def validateNumber(number: Option[String]): Validated[List[String], String]
def validatePremium(premium: Option[BigDecimal]): Validated[List[String], BigDecimal]
```

The illustrative lab rules are:

- A policy number must be present and contain a non-whitespace character.
- A premium must be present and greater than zero.

We define those rules and their messages. Cats supplies the result type and the
combination operation. Calling `Validated.Valid(value)` alone does not check a rule;
it records success after our code has checked the value.

Each function reports at most one error for its own field in this lesson. mapN
collects the errors those functions return; it does not invent or run additional rules.

```scala
val checkedNumber = validateNumber(number)
val checkedPremium = validatePremium(premium)

(checkedNumber, checkedPremium).mapN(Policy.apply)
```

The two field checks both run, and then mapN combines their results. Neither check
needs the successful value from the other. This independence is what lets us report
both problems together.

## Run it

```powershell
sbt lesson05
```

Full entry point: `sbt "runMain learning.lesson05.ValidatedLesson"`.

Expected application output:

```text
Lesson 05 - Collect independent field errors with Validated
Both valid: Valid(Policy(POL-001,600))
Number missing: Invalid(List(Policy number is missing))
Premium missing: Invalid(List(Annual premium is missing))
Both missing: Invalid(List(Policy number is missing, Annual premium is missing))
Blank number: Invalid(List(Policy number must not be blank))
Zero premium: Invalid(List(Annual premium must be positive))
Both invalid: Invalid(List(Policy number must not be blank, Annual premium must be positive))
All seven lesson checks passed.
```

Source: [ValidatedLesson.scala](../src/main/scala/learning/lesson05/ValidatedLesson.scala).
The checks compare each outcome with its exact expected value, including error order,
the zero boundary, and negative premiums.

Reference: [Typelevel's Validated documentation](https://typelevel.org/cats/datatypes/validated.html).
