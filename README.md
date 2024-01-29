# SBT plugin that simplifies modules creation

[![][github-action-badge]][github-action] [![][maven-badge]][maven] [![][steward-badge]][steward] 

```diff
- skip in publish := true
- 
lazy val docs = project
-  .settings(skip in publish := true)
-  .dependsOn(allProjects: _*)
+  .dependsOn(allModules)
  .in(file("docs"))

+ lazy val `my-library-core` = module
- lazy val core = project
-   .in(file("modules/core"))
-   .settings(name := "my-library-core")

+ lazy val `my-library-plugin` = module 
- lazy val plugin = project
-   .in(file("modules/plugin"))
-   .settings(name := "my-library-plugin")
-   .dependsOn(core)
+   .dependsOn(`my-library-core`)
-
- lazy val allProjects: Seq[ClasspathDep[ProjectReference]] = Seq(
-   core,
-   plugin
- )
```

## Installation

Add the following line to your `plugins.sbt` file:

```sbt
addSbtPlugin("com.alejandrohdezma" % "sbt-modules" % "0.3.0")
```

## Usage

Use `module` instead of `project` to create your SBT modules. Unlike `project`, `module` expects your modules to live in `modules` folder and uses the name of the variable for the project's ID and base folder (just like `project` does).

For example, the following SBT configuration:

```sbt
lazy val `my-library-core` = module

lazy val `my-library-plugin` = module.dependsOn(`my-library-core`) 
```

Would expect the following directory structure:

```
.
+-- modules
|   +-- my-library-core
|       +-- src
|   +-- my-library-plugin
|       +-- src
+-- build.sbt
+-- project
```

### Retrieveing all modules created with `module`

`sbt-modules` creates a special variable called `allModules` that aggregates all the modules created with `module`, so you can pass it along as a dependency to other projects in your build, like:

```sbt
lazy val documentation = project.dependsOn(allModules)

lazy val `my-library-core` = module

lazy val `my-library-plugin` = module.dependsOn(`my-library-core`)
```

> Important ‼️ The `allModules` variable is created by listing all the directories in the `modules` directory so ensure: (1) that all your modules have a corresponding directory inside `modules` and (2) that there are no directories inside `modules` that aren't a module.

### Auto-`skip in publish`

Forget about setting `skip in publish := true` again. Adding this plugin to your build will disable publishing for all the projects in the build (including the auto-generated root plugin), except for those created with `module`.

However, if you also want to exclude any of those created with `module` you can always add `.settings(skip in publish := true)`.

Example:

```sbt
// Will not be published
lazy val documentation = project.dependsOn(allmodules)

// Will be published
lazy val `my-library-plugin` = module.dependsOn(`my-library-core`)

// Will be published
lazy val `my-library-core` = module

// Will not be published
lazy val `my-library-util` = module.settings(skip in publish := true)
```

[github-action]: https://github.com/alejandrohdezma/sbt-modules/actions
[github-action-badge]: https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Falejandrohdezma%2Fsbt-modules%2Fbadge%3Fref%3Dmaster&style=flat

[maven]: https://search.maven.org/search?q=g:%20com.alejandrohdezma%20AND%20a:sbt-modules
[maven-badge]: https://maven-badges.herokuapp.com/maven-central/com.alejandrohdezma/sbt-modules/badge.svg?kill_cache=1

[steward]: https://scala-steward.org
[steward-badge]: https://img.shields.io/badge/Scala_Steward-helping-brightgreen.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=
