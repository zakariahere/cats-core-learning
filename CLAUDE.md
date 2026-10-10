# CLAUDE.md — Cats Core learning lab

@AGENTS.md

The tutor rules in AGENTS.md apply in full. This file adds the Claude-specific
rules and a quick orientation.

## Visuals first (mandatory)

Zakaria learns fastest from pictures. **Every explanation of a Cats concept in the
chat starts with a visual, before any prose or code.** No exceptions for "small"
concepts. If the explanation does not fit a visual, it probably is not small enough yet.

- **Order of a concept explanation:** visual → one or two sentences on what the
  visual shows → the Scala code → the trace of actual values.
- **Tool:** when an inline visual/diagram tool is available in the session
  (e.g. the visualize widget), use it. Otherwise (terminal / plain Claude Code) draw an
  ASCII/Unicode diagram inside a fenced code block. Never skip the visual because
  a tool is missing.
- **What to draw**, depending on the concept:
  - *Type flow*: boxes and arrows showing shapes, e.g. `F[A] --map(f: A => B)--> F[B]`,
    `(F[A], F[B]) --mapN--> F[C]`, `F[A] --flatMap(A => F[B])--> F[B]`.
  - *Container view*: the "context" drawn as a box around the value
    (Some[ 10 ], None[ ], List[ 1 | 2 ], Valid[ a ] / Invalid[ errs ]).
  - *Value traces*: a table or pipeline with real inputs and outputs from the lesson,
    not abstract `a, b, c`.
  - *Combination grids*: e.g. Apply/mapN over List as a cartesian grid.
  - *Hierarchy maps*: where the type class sits (Semigroup → Monoid;
    Functor → Apply → Applicative → Monad; Foldable → Traverse), with the
    current lesson highlighted and what each level **adds** (`combine`, `empty`,
    `map`, `ap/map2`, `pure`, `flatMap`).
  - *Comparisons*: side-by-side visual for contrasts (Either fail-fast vs Validated
    accumulate; Apply independent vs Monad dependent; Java/Spring equivalent vs Cats).
- Keep diagrams small and readable: one idea per diagram, labelled with the real
  types from the lesson (`Option[BigDecimal]`, `Validated[ValidationErrors, Policy]`).
- Lesson notes in `lessons/NN-*.md` should also contain at least one diagram
  (ASCII in a code block or Mermaid) near the top.

## Orientation

- Scala 2.13.18 (Scala 2 syntax: `implicit val`, implicit parameters, no givens),
  Cats core 2.13.0, sbt 1.12, JDK 21. No test framework: lessons self-check with `assert`s.
- One lesson = `src/main/scala/learning/lessonNN/XxxLesson.scala` (a runnable `object`)
  + `lessons/NN-topic.md` + an alias in `build.sbt`; run with `sbt lessonNN`.
- Imports: `import cats._`, `import cats.data._`, `import cats.syntax.all._`.
  **Never** also import `cats.implicits._`.
- Instances as `implicit val x: TC[T] = new TC[T] { override def ... }`; prefer a named
  implicit parameter `(implicit functor: Functor[F])`; show the direct type-class
  call (`Functor[List].map`, `Apply[Option].map2`) next to the syntax.
- Domains are business-flavoured (insurance `Policy`, `FieldError`/`ValidationErrors`,
  checkout `LineItem`/`CheckoutTotals`, `Quote`). Reuse them.
- Before continuing, read the "Resume here" section of `progress.md` (gitignored log).
- User uses PowerShell on Windows: give commands as one-liners, chained with `;`.
- Ask before committing or pushing.

## Blog series (blog.zakaria.lu)

- Series page: https://blog.zakaria.lu/topics/cats-core. It is the source of truth
  for what is already published. **Fetch it before proposing an article**, and compare it
  with the lessons covered so no lesson is written up twice and none is silently skipped.
- Track the mapping "lessons ↔ article part" in `progress.md`.
- When the lessons since the last article form one complete, useful idea (concrete
  problem, the Cats operation, the abstraction, runnable examples, and Zakaria has
  confirmed he understands them), propose the article scope and ask. Don't ask
  after every small step.
- Once Zakaria agrees, create the article **with the `/blog-article` skill only**.
  Don't hand-write the post outside that skill.
- **Never use the `zakaria-mascot` skill** (it generates images with GPT). Reuse the
  existing approved assets in `docs/assets/` if an image is needed.
- Articles keep the visuals-first rule: each Cats concept gets a diagram.
- Saving a draft and publishing are separate: **publishing needs Zakaria's explicit
  authorization**. Update an existing draft rather than creating a duplicate.
- Also flag stale series metadata (e.g. the "Next in this series" blurb) as an
  editorial follow-up.
