# Lesson 02 - Pass the value and the behavior it needs

Last time, we constructed a `Show[Policy]` and called its `show` method directly.
Now we want a reusable function that formats a record and adds a `[record]` prefix.
We want to reuse that function for policies and customers.

## Start with familiar parameter passing

Imagine a function that handles only policies:

```scala
def renderPolicyLine(policy: Policy, formatter: Show[Policy]): String = {
  val text: String = formatter.show(policy)
  "[record] " + text
}
```

It takes two ordinary arguments: the data to display and an object that knows how
to display it. Passing a formatter is like passing a service implementation through
a Java interface. This example needs no dependency-injection container.

Look inside the method: it never reads `policy.number` or `policy.annualPremium`.
The formatter handles those details. That means the method can be generalized.

## Replace the particular type with A

```scala
def renderLine[A](value: A, formatter: Show[A]): String = {
  val text: String = formatter.show(value)
  "[record] " + text
}
```

The Java-shaped equivalent would be a generic method with type parameter `<A>`:

```java
static <A> String renderLine(A value, Show<A> formatter) {
    String text = formatter.show(value);
    return "[record] " + text;
}
```

That Java snippet illustrates the familiar method structure; the runnable lab is
Scala. In the Scala method, `[A]` declares the type parameter and `Show[A]` ties the
formatter's accepted type to the value's type. It is more precise than accepting
an arbitrary object and hoping its formatter can handle it.

## Trace the policy call

```scala
val policy: Policy = Policy("POL-001", BigDecimal("600"))
val policyShow: Show[Policy] = new PolicyShow()

val line: String = renderLine[Policy](policy, policyShow)
```

For this call, we explicitly choose `A = Policy`. The first parameter expects a
`Policy`, and the second expects a `Show[Policy]`.

1. `value` receives the reference to our policy object.
2. `formatter` receives the reference to our `PolicyShow` object.
3. `formatter.show(value)` invokes `PolicyShow.show` with that policy.
4. The formatter returns `"Policy POL-001: EUR 600/year"`.
5. `renderLine` adds its prefix and returns `"[record] Policy POL-001: EUR 600/year"`.

No formatter is discovered or constructed by `renderLine`. The caller already
constructed it and passed it in, exactly like any other object argument.

## Reuse the same function for a customer

The runnable example also defines:

```scala
final case class Customer(name: String)

final class CustomerShow extends Show[Customer] {
  override def show(value: Customer): String = s"Customer ${value.name}"
}
```

Then we call the same function:

```scala
val customer: Customer = Customer("Zakaria")
val customerShow: Show[Customer] = new CustomerShow()

renderLine[Customer](customer, customerShow)
// "[record] Customer Zakaria"
```

This time `A = Customer`. The function's implementation remains the same; ordinary
method dispatch invokes `CustomerShow.show` on the supplied formatter.

This is the concrete benefit of the shared contract: the general function handles
the prefix, and each `Show` implementation handles its own data type.

## Change the rule while keeping the data

The example has a second `Show[Policy]` implementation that displays only the policy
number. Passing that object into the same function gives:

```scala
renderLine[Policy](policy, compactPolicyShow)
// "[record] POL-001"
```

The policy has not changed. We supplied a different behavior object for this call.
Multiple implementations can coexist because we choose each one explicitly.

Both formatters here are valid for `Policy`. In contrast, this call is invalid:

```scala
renderLine[Policy](policy, customerShow)
```

With `A = Policy`, the second parameter requires `Show[Policy]`.
`customerShow` has type `Show[Customer]`, so the compiler rejects that mismatch.
A commented line in the runnable source lets you try the diagnostic yourself.

## Run it

```powershell
sbt lesson02
```

Or use the full entry point:

```powershell
sbt "runMain learning.lesson02.PassingShowLesson"
```

Expected application output:

```text
Lesson 02 - Pass the value and its Show instance
Policy:   [record] Policy POL-001: EUR 600/year
Customer: [record] Customer Zakaria
Compact:  [record] POL-001
All lesson checks passed.
```

Source: [PassingShowLesson.scala](../src/main/scala/learning/lesson02/PassingShowLesson.scala).
The assertions check these concrete outputs. If you change an example, update its
expected output too.

The next step will keep this same mechanism and show how Scala 2 can supply the
formatter argument implicitly. For now, follow the two objects entering `renderLine`.
