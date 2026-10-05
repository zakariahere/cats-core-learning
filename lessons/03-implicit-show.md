# Lesson 03 - Let Scala supply the formatter argument

We already know which two objects are involved: a Policy containing data and a
Show[Policy] object containing its formatting behavior. This lesson changes how
that second object reaches the method.

## Keep the explicit starting point visible

From lesson 02:

```scala
def renderExplicit[A](value: A, formatter: Show[A]): String =
  "[record] " + formatter.show(value)

renderExplicit[Policy](policy, policyShow)
```

There is one parameter list with two parameters. Both arguments come from the caller.

## Split the parameters into two lists

Scala allows multiple parameter lists on a method:

```scala
def renderInTwoLists[A](value: A)(formatter: Show[A]): String =
  "[record] " + formatter.show(value)

renderInTwoLists[Policy](policy)(policyShow)
```

We now write two pairs of parentheses at the call site. Inside the method, `value`
and `formatter` are both available. The body and its output stay the same.

The example uses distinct method names so we can compare all three versions together.

## Mark the last parameter list implicit

```scala
def renderLine[A](value: A)(implicit formatter: Show[A]): String =
  "[record] " + formatter.show(value)
```

The parameter declaration says: if the caller omits this list, ask the compiler to
find an implicitly available value of the required type and supply it as the argument.
The caller can still write `renderLine[Policy](policy)(policyShow)` explicitly.

We make a candidate available where we call the method:

```scala
val policy: Policy = Policy("POL-001", BigDecimal("600"))
implicit val policyShow: Show[Policy] = new PolicyShow()

val line: String = renderLine[Policy](policy)
```

Here is what happens for that final line:

1. We explicitly choose `A = Policy`.
2. The omitted parameter therefore requires `Show[Policy]`.
3. At the call site, `policyShow` is an implicit value with that type.
4. The compiler supplies it, as if we wrote `renderLine[Policy](policy)(policyShow)`.
5. At runtime the method invokes `PolicyShow.show` on the object it received.
6. The result is `"[record] Policy POL-001: EUR 600/year"`.

The search and insertion happen during compilation. Constructing the object and
executing its `show` method happen at runtime.

## The two uses of implicit have separate roles

| Code | Role |
| --- | --- |
| `(implicit formatter: Show[A])` | Declares a parameter whose argument the compiler may supply |
| `implicit val policyShow: Show[Policy] = ...` | Makes this value eligible to supply a matching implicit argument |

The names `formatter` and `policyShow` differ deliberately: matching depends on the
required type and Scala's scope/resolution rules. Renaming `policyShow` to another
name does not change its type. A plain `val` remains usable as an explicit argument,
but does not become an implicit candidate merely because its type matches.

For this lesson the candidate is local. Imported candidates and associated companion
objects are other sources of implicits; we will explore those when we need them.
If resolution cannot find a suitable candidate, or equally applicable candidates
cannot be disambiguated, compilation fails.

## You can still choose explicitly

```scala
val compactPolicyShow: Show[Policy] = new CompactPolicyShow()

renderLine[Policy](policy)(compactPolicyShow)
// "[record] POL-001"
```

This call supplies its second argument directly. The method uses the supplied compact
formatter. The `policyShow` value remains available for other calls that omit the list.

## Run it

```powershell
sbt lesson03
```

Full entry point: `sbt "runMain learning.lesson03.ImplicitShowLesson"`.

Expected application output:

```text
Lesson 03 - The compiler supplies the formatter argument
One list:           [record] Policy POL-001: EUR 600/year
Two lists:          [record] Policy POL-001: EUR 600/year
Implicit, explicit: [record] Policy POL-001: EUR 600/year
Implicit, omitted:  [record] Policy POL-001: EUR 600/year
Explicit choice:    [record] POL-001
All lesson checks passed.
```

Source: [ImplicitShowLesson.scala](../src/main/scala/learning/lesson03/ImplicitShowLesson.scala).
An optional comment explains how removing `implicit` from the local value exposes
the missing-argument compiler error. Restore the keyword after experimenting.

Next we will connect this parameter-passing mechanism to Cats' `.show` extension
syntax. That syntax is not introduced in this example.

Reference: [Scala's implicit parameters documentation, Scala 2 examples](https://docs.scala-lang.org/tour/implicit-parameters.html).
