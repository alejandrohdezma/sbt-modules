ThisBuild / scalaVersion                  := _root_.scalafix.sbt.BuildInfo.scala212
ThisBuild / organization                  := "com.alejandrohdezma"
ThisBuild / pluginCrossBuild / sbtVersion := "1.2.8"
ThisBuild / versionPolicyIntention        := Compatibility.BinaryAndSourceCompatible

addCommandAlias("ci-test", "fix --check; versionPolicyCheck; mdoc; scripted")
addCommandAlias("ci-docs", "github; mdoc; headerCreateAll")
addCommandAlias("ci-publish", "versionCheck; github; ci-release")

lazy val documentation = project
  .enablePlugins(MdocPlugin)
  .dependsOn(allModules)
  .settings(mdocOut := file("."))

lazy val `sbt-modules` = module
  .enablePlugins(SbtPlugin)
  .settings(scriptedLaunchOpts += s"-Dplugin.version=${version.value}")
