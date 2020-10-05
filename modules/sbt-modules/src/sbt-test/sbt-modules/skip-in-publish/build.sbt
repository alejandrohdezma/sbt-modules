lazy val a = module
lazy val b = module

lazy val c = project.dependsOn(allModules: _*)

TaskKey[Unit]("check", "Checks c depends on a & b using `allModules`") := {
  val expected = List(
    ClasspathDependency(LocalProject("a"), None),
    ClasspathDependency(LocalProject("b"), None)
  )

  assert(c.dependencies == expected, s"Found: ${c.dependencies}\nExpected: $expected")
}
