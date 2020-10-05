ThisBuild / scalaVersion                  := "2.12.12"
ThisBuild / organization                  := "com.alejandrohdezma"
ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"

addCommandAlias("ci-test", "fix --check; mdoc; scripted")
addCommandAlias("ci-docs", "github; mdoc; headerCreateAll")
addCommandAlias("ci-publish", "github; ci-release")

lazy val documentation = project
  .enablePlugins(MdocPlugin)
  .dependsOn(allModules)
  .settings(mdocOut := file("."))

lazy val `sbt-modules` = module
  .enablePlugins(SbtPlugin)
  .settings(scriptedLaunchOpts += s"-Dplugin.version=${version.value}")
