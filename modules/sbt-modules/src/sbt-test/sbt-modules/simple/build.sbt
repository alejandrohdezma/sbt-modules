lazy val a = module
lazy val b = module

TaskKey[Unit]("check", "Checks all base directories are correct") := {
  assertFiles(a.base, file("modules") / "a")
  assertFiles((baseDirectory in a).value, file("modules") / "a")

  assertFiles(b.base, file("modules") / "b")
  assertFiles((baseDirectory in b).value, file("modules") / "b")
}

def assertFiles(file1: File, file2: File): Unit =
  assert(
    file1.absolutePath == file2.absolutePath,
    s"Found: ${file1.absolutePath}\nExpected: ${file2.absolutePath}"
  )
