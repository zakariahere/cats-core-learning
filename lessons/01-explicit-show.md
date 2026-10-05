# Lesson 01 - A familiar interface leads to Cats Show

Buddy, start with a small requirement: display this insurance policy as readable text.

```scala
val policy = Policy("POL-001", BigDecimal("600"))
```

Desired result:

```text
Policy POL-001: EUR 600/year
```

The policy holds the data. A separate formatter object holds the rule for displaying it.
That separation lets us supply formatting behavior without changing the Policy class.

## Start with the Java idea you already know

Think of a generic Java interface:

```java
interface Formatter<A> {
    String format(A value);
}
```

For a `Formatter<Policy>`, the input type `A` is `Policy` and the output is `String`.
The Scala equivalent is:

```scala
trait Formatter[A] {
  def format(value: A): String
}
```

Here, a Scala `trait` serves the role of the Java interface. Scala uses `[A]` where
Java uses `<A>`, and writes the return type after the method parameters.

We implement the contract in an ordinary class:

```scala
final class PolicyFormatter extends Formatter[Policy] {
  override def format(value: Policy): String =
    s"Policy ${value.number}: EUR ${value.annualPremium}/year"
}
```

Then construct an object and call its method:

```scala
val ourFormatter: Formatter[Policy] = new PolicyFormatter()
val text: String = ourFormatter.format(policy)
```

Follow the values: `policy` becomes the `value` parameter; `value.number` is
`"POL-001"`; `value.annualPremium` is `600`; the `s"..."` string inserts those
values and returns `"Policy POL-001: EUR 600/year"`.

## Bring in Cats for that same job

Cats has an existing contract called `Show[A]`. Its essential method is:

```scala
def show(value: A): String
```

So we can use that contract instead of maintaining our own `Formatter[A]`:

```scala
import cats.Show

final class PolicyShow extends Show[Policy] {
  override def show(value: Policy): String =
    s"Policy ${value.number}: EUR ${value.annualPremium}/year"
}

val catsFormatter: Show[Policy] = new PolicyShow()
val text: String = catsFormatter.show(policy)
```

`new PolicyShow()` constructs the formatter. `catsFormatter.show(policy)` passes
our policy into that object's method. The output is exactly the same.

Notice which class implements the contract: `PolicyShow` implements `Show[Policy]`.
`Policy` remains a data class. An implementation for a particular type is commonly
called a **type class instance**: here, `catsFormatter` is a `Show[Policy]` instance.
It supplies the capability "turn a Policy into readable text" outside Policy itself.

This small example establishes the mechanism. As we progress, Cats' shared contracts
will let reusable functions work with many types. We will introduce those functions
and Scala's implicit lookup in their own steps.

## Run the example

From the repository root:

```powershell
sbt lesson01
```

The equivalent full command is:

```powershell
sbt "runMain learning.lesson01.ExplicitShowLesson"
```

The runnable source is [ExplicitShowLesson.scala](../src/main/scala/learning/lesson01/ExplicitShowLesson.scala).

Expected application output, excluding sbt's own logs:

```text
Lesson 01 - An explicit formatter, then Cats Show
Scala version: 2.13.18
Input: Policy(POL-001,600)
Our Formatter: Policy POL-001: EUR 600/year
Cats Show:     Policy POL-001: EUR 600/year
Second input:  Policy(POL-002,840.50)
Cats Show:     Policy POL-002: EUR 840.50/year
All lesson checks passed.
```

The assertions check the stated outputs. They verify execution, not your understanding.
When experimenting with the inputs, adjust the corresponding expected strings too.

Pause here before adding shorthand. The key thing to follow is which object receives
the method call, which object is passed as its argument, and which String comes back.

Reference: [Typelevel's Show documentation](https://typelevel.org/cats/typeclasses/show.html).
