![Zakaria's navy-hoodie mascot and a ginger cat building a bridge, one block at a time](docs/assets/cats-core-readme.png)

# Cats Core, one concrete example at a time 🐈

**Scala 2.13.18 · Cats Core 2.13.0 · sbt 1.12.15**

How do you build a value from optional fields? How do you report several validation
errors together? And how does Cats know how to combine your own error type?

This is the runnable companion to my [Cats Core blog series](https://blog.zakaria.lu/topics/cats-core).
We start with a small problem, use a Cats operation, then uncover the abstraction
behind it. Every lesson has explanatory notes, a standalone Scala program, and
assertions you can run and change.

**[Run your first example](#run-your-first-example) · [Browse the lessons](#the-lessons) · [Read the series](https://blog.zakaria.lu/topics/cats-core)**

## Run your first example

Install a **JDK 21** and an [sbt launcher](https://www.scala-sbt.org/download/).
The examples have been verified with JDK 21. You do not need a separate Scala
installation: the build downloads the pinned Scala and Cats versions.

```shell
git clone https://github.com/zakariahere/cats-core-learning.git
cd cats-core-learning
sbt lesson04
```

The first run downloads sbt and the project dependencies. Lesson 04 combines an
optional policy number and an optional annual premium with `mapN`. Among sbt's
build messages, you will see:

```text
Both present: Some(Policy(POL-001,600))
Number missing: None
Premium missing: None
Both missing: None
Scala, Cats, and shorthand agree in all four cases.
All lesson checks passed.
```

Read the [lesson notes](lessons/04-mapn.md) beside the
[Scala source](src/main/scala/learning/lesson04/MapNLesson.scala).
Then change an input and run it again.

Comfortable with case classes, generics, and implicit arguments? Start at lesson 04.
If type classes are new to you, start with `sbt lesson01` and follow lessons 01–03
first. Those examples make the Scala machinery explicit before we use the Cats
operations that depend on it.

## A taste: keep both validation errors

`Option` can tell us that a value is missing. For a form, we also want to explain
what needs fixing. Lesson 05 uses `Validated` and the same `mapN` operation to
collect the errors from independent field checks.

Start a console from the repository root:

```shell
sbt console
```

Then paste:

```scala
import learning.lesson05.ValidatedLesson.buildPolicy

buildPolicy(Some("POL-001"), Some(BigDecimal("600")))
// Valid(Policy(POL-001,600))

buildPolicy(None, None)
// Invalid(List(Policy number is missing, Annual premium is missing))
```

Our functions check the fields. Cats combines their results. Lessons 06–07 expose
the `Semigroup` used to combine errors, then supply a rule for our own structured
error type. Type `:quit` to leave the console.

## The lessons

Seven lessons are available. Read a note, run its program, inspect the values,
and make a small change before moving on.

| Lesson | Concrete question | Run | Source |
| --- | --- | --- | --- |
| [01 · An explicit formatter and `Show`](lessons/01-explicit-show.md) | How can formatting live outside the data class? | `sbt lesson01` | [Scala](src/main/scala/learning/lesson01/ExplicitShowLesson.scala) |
| [02 · Pass a `Show` instance](lessons/02-passing-show.md) | How can one function format different types? | `sbt lesson02` | [Scala](src/main/scala/learning/lesson02/PassingShowLesson.scala) |
| [03 · Implicit arguments](lessons/03-implicit-show.md) | How does the compiler supply the formatting helper? | `sbt lesson03` | [Scala](src/main/scala/learning/lesson03/ImplicitShowLesson.scala) |
| [04 · `mapN` with `Option`](lessons/04-mapn.md) | How do we construct a policy from two optional fields? | `sbt lesson04` | [Scala](src/main/scala/learning/lesson04/MapNLesson.scala) |
| [05 · `Validated`](lessons/05-validated.md) | How do we keep both field errors? | `sbt lesson05` | [Scala](src/main/scala/learning/lesson05/ValidatedLesson.scala) |
| [06 · `Semigroup`](lessons/06-semigroup.md) | Which operation combines those errors? | `sbt lesson06` | [Scala](src/main/scala/learning/lesson06/SemigroupLesson.scala) |
| [07 · A custom error `Semigroup`](lessons/07-custom-error-semigroup.md) | How do we retain field names and define our own combination rule? | `sbt lesson07` | [Scala](src/main/scala/learning/lesson07/CustomErrorSemigroupLesson.scala) |

Each lesson has its own package, so you can experiment with one example without
having to rewrite all the later lessons. The policy rules are illustrative:
a nonblank number and a positive annual premium.

## Run the checks

All examples contain assertions and print a success message when their checks
pass. Run the full sequence in one sbt session:

```shell
sbt lesson01 lesson02 lesson03 lesson04 lesson05 lesson06 lesson07
```

These checks are in the runnable programs. `sbt test` does not run them.

| Command | Purpose |
| --- | --- |
| `sbt compile` | Compile all seven examples |
| `sbt lesson07` | Run one lesson and its assertions |
| `sbt console` | Experiment with the compiled code interactively |
| `sbt run` | Run the configured starting example, lesson 01 |
| `sbt "show scalaVersion"` | Read the pinned Scala version |
| `sbt dependencyTree` | Inspect the resolved libraries |

You can also enter `sbt` once, then type `lesson04`, `lesson05`, and so on at its
prompt. This avoids restarting the build tool between examples.

## Find your way around

```text
build.sbt                     Dependencies, compiler settings, lesson aliases
project/build.properties      Pinned sbt version
src/main/scala/learning/      One package and runnable program per lesson
lessons/                      Explanations, types, values, and expected output
docs/assets/                  README illustrations and generation provenance
```

Coming from Maven? `build.sbt` plays the role of the build settings and dependency
sections of a `pom.xml`. `project/build.properties` pins the build tool itself.

```scala
libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0"
```

Here, `%%` selects the Scala binary artifact `cats-core_2.13`. **2.13.0 is the Cats
version; 2.13.18 is the Scala version.** Their similar numbers do not mean the
versions have to match.

## Keep learning

The lab grows alongside the [Cats Core series](https://blog.zakaria.lu/topics/cats-core).
The first article, [From Optional Fields to Useful Validation Errors](https://blog.zakaria.lu/cats-core-part-1-from-optional-fields-to-useful-validation-errors),
follows lessons 04–05. Lessons 06–07 continue from that error-accumulation example.

Useful references: [Typelevel Cats](https://typelevel.org/cats/),
[`Validated`](https://typelevel.org/cats/datatypes/validated.html), and
[`Semigroup`](https://typelevel.org/cats/typeclasses/semigroup.html).
This repository currently focuses on Cats Core in Scala 2.

Built one small example at a time by [Zakaria](https://zakaria.lu). 🙂
The opening illustration uses my canonical mascot;
[its prompt and provenance are here](docs/assets/readme-mascot-generation.md).
