lazy val a = module
lazy val b = module
lazy val c = module.settings(skip in publish := true)

lazy val d = project.dependsOn(allModules: _*)

TaskKey[Unit]("check", "Checks skip in publish for every module in build") := {
  assert(!(a / publish / skip).value)
  assert(!(b / publish / skip).value)
  assert((c / publish / skip).value)
  assert((d / publish / skip).value)
  assert((LocalRootProject / publish / skip).value)
}
