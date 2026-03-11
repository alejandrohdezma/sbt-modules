lazy val a = module
lazy val b = module
lazy val c = module.settings(publish / skip := true)

lazy val d = project.dependsOn(allModules: _*)

TaskKey[Unit]("check", "Checks packageIsModule and `publish / skip` for every module in build") := {
  // packageIsModule should be true for modules, false for regular projects/root
  assert((a / packageIsModule).value)
  assert((b / packageIsModule).value)
  assert((c / packageIsModule).value)
  assert(!(d / packageIsModule).value)
  assert(!(LocalRootProject / packageIsModule).value)

  // publish / skip should be false for modules (unless explicitly overridden), true for others
  assert(!(a / publish / skip).value)
  assert(!(b / publish / skip).value)
  assert((c / publish / skip).value)
  assert((d / publish / skip).value)
  assert((LocalRootProject / publish / skip).value)
}
