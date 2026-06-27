Compile / unmanagedSourceDirectories ++= {
  val main = (ThisBuild / baseDirectory).value.getParentFile / "modules" / "sbt-modules" / "src" / "main"
  Seq(main / "scala", main / "scala-2.12")
}
