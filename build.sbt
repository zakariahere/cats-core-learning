ThisBuild / scalaVersion := "2.13.18"
ThisBuild / organization := "lu.zakaria.learning"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "cats-scala-learning",
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.13.0",
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
    Compile / run / mainClass := Some("learning.lesson01.ExplicitShowLesson")
  )

addCommandAlias("lesson01", "runMain learning.lesson01.ExplicitShowLesson")
addCommandAlias("lesson02", "runMain learning.lesson02.PassingShowLesson")
addCommandAlias("lesson03", "runMain learning.lesson03.ImplicitShowLesson")
addCommandAlias("lesson04", "runMain learning.lesson04.MapNLesson")
addCommandAlias("lesson05", "runMain learning.lesson05.ValidatedLesson")
addCommandAlias("lesson06", "runMain learning.lesson06.SemigroupLesson")
addCommandAlias("lesson07", "runMain learning.lesson07.CustomErrorSemigroupLesson")
