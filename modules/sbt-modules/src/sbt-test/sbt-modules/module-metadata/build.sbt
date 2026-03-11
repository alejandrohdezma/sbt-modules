import com.alejandrohdezma.sbt.modules.ModuleMetadata

lazy val core = module

lazy val api = module.dependsOn(core)

lazy val server = module.dependsOn(api)

TaskKey[Unit]("checkMetadata", "Checks ModuleMetadata.from returns correct data") := {
  val metadata = ModuleMetadata.from(state.value)

  val expected = Map(
    "core" -> ModuleMetadata(
      version = version.value,
      dependencies = Set.empty,
      transitiveDependencies = Set.empty,
      dependents = Set("api"),
      transitiveDependents = Set("api", "server")
    ),
    "api" -> ModuleMetadata(
      version = version.value,
      dependencies = Set("core"),
      transitiveDependencies = Set("core"),
      dependents = Set("server"),
      transitiveDependents = Set("server")
    ),
    "server" -> ModuleMetadata(
      version = version.value,
      dependencies = Set("api"),
      transitiveDependencies = Set("core", "api"),
      dependents = Set.empty,
      transitiveDependents = Set.empty
    )
  )

  assert(metadata == expected, s"They're not equal.\nGot: $metadata\nExpected: $expected")
}

TaskKey[Unit]("checkMetadataKey", "Checks moduleMetadata task key returns same data as ModuleMetadata.from") := {
  val fromKey    = moduleMetadata.value
  val fromMethod = ModuleMetadata.from(state.value)

  assert(fromKey == fromMethod, s"They're not equal.\nKey: $fromKey\nMethod: $fromMethod")
}
