lazy val a = module
lazy val b = module

lazy val c = project.dependsOn(allModules)

TaskKey[Unit]("check", "Checks c depends on a & b using `allModules`") := {
  val found = c.dependencies.map(dep => dep.project -> dep.configuration).toList

  val expected = List(LocalProject("a") -> None, LocalProject("b") -> None)

  assert(found == expected, s"Found: $found\nExpected: $expected")
}
