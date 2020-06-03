unmanagedSourceDirectories in Compile +=
  baseDirectory
    .in(ThisBuild)
    .value
    .getParentFile / "modules" / "sbt-modules" / "src" / "main" / "scala"
