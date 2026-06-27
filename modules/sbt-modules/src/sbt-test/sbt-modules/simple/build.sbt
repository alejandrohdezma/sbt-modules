@transient val checkModules = taskKey[Unit]("Checks all base directories are correct")
@transient val checkRoot    = taskKey[Unit]("Checks root project aggregates modules")

lazy val a = module
lazy val b = module

checkModules := {
  assertFiles(a.base, file("modules") / "a")
  assertFiles((a / baseDirectory).value, file("modules") / "a")

  assertFiles(b.base, file("modules") / "b")
  assertFiles((b / baseDirectory).value, file("modules") / "b")
}

checkRoot := {
  val aggregate = (LocalRootProject / thisProject).value.aggregate.map(_.project)

  val expected = List("a", "b")

  assert(aggregate == expected, s"Found: $aggregate}\nExpected: $expected")
}

def assertFiles(file1: File, file2: File): Unit =
  assert(
    file1.absolutePath == file2.absolutePath,
    s"Found: ${file1.absolutePath}\nExpected: ${file2.absolutePath}"
  )
