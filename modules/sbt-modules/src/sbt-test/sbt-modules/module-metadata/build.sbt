import com.alejandrohdezma.sbt.modules.{ModuleDependency, ModuleMetadata}

@transient val checkMetadata = taskKey[Unit]("Checks ModuleMetadata.from returns correct data")
@transient val checkMetadataKey =
  taskKey[Unit]("Checks moduleMetadata task key returns same data as ModuleMetadata.from")

lazy val core = module

lazy val api = module.dependsOn(core)

lazy val server = module.dependsOn(api)

lazy val consumer = module.dependsOn(core % Test)

checkMetadata := {
  val metadata = ModuleMetadata.from(state.value)

  val expected = Map(
    "core" -> ModuleMetadata(
      version = version.value,
      dependencies = Set.empty,
      transitiveDependencies = Set.empty,
      dependents = Set(ModuleDependency("api", "compile"), ModuleDependency("consumer", "test")),
      transitiveDependents = Set("api", "server", "consumer")
    ),
    "api" -> ModuleMetadata(
      version = version.value,
      dependencies = Set(ModuleDependency("core", "compile")),
      transitiveDependencies = Set("core"),
      dependents = Set(ModuleDependency("server", "compile")),
      transitiveDependents = Set("server")
    ),
    "server" -> ModuleMetadata(
      version = version.value,
      dependencies = Set(ModuleDependency("api", "compile")),
      transitiveDependencies = Set("core", "api"),
      dependents = Set.empty,
      transitiveDependents = Set.empty
    ),
    "consumer" -> ModuleMetadata(
      version = version.value,
      dependencies = Set(ModuleDependency("core", "test")),
      transitiveDependencies = Set("core"),
      dependents = Set.empty,
      transitiveDependents = Set.empty
    )
  )

  assert(metadata == expected, s"They're not equal.\nGot: $metadata\nExpected: $expected")
}

checkMetadataKey := {
  val fromKey    = moduleMetadata.value
  val fromMethod = ModuleMetadata.from(state.value)

  assert(fromKey == fromMethod, s"They're not equal.\nKey: $fromKey\nMethod: $fromMethod")
}
