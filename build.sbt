ThisBuild / scalaVersion := "2.12.11"
ThisBuild / organization := "com.alejandrohdezma"

addCommandAlias("ci-test", "fix --check; mdoc; scripted")
addCommandAlias("ci-docs", "github; mdoc; headerCreateAll")
addCommandAlias("ci-publish", "github; ci-release")

lazy val documentation = project
  .enablePlugins(MdocPlugin)
  .dependsOn(allModules: _*)
  .settings(mdocOut := file("."))

lazy val `sbt-modules` = module
  .enablePlugins(SbtPlugin)
  .settings(scriptedLaunchOpts += s"-Dplugin.version=${version.value}")
