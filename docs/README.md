# @DESCRIPTION@

[![][github-action-badge]][github-action] [![][maven-badge]][maven] [![][steward-badge]][steward] 

```diff
lazy val docs = project
  .in(file("docs"))

+ lazy val `my-library-core` = module
- lazy val core = project
-   .in(file("modules/core"))
-   .settings(name := "my-library-core")

+ lazy val `my-library-plugin` = module 
- lazy val plugin = project
-   .in(file("modules/plugin"))
-   .settings(name := "my-library-plugin")
```

## Installation

Add the following line to your `plugins.sbt` file:

```sbt
addSbtPlugin("com.alejandrohdezma" % "sbt-modules" % "@VERSION@")
```

## Usage

Use `module` instead of `project` to create your SBT modules. Unlike `project`, `module` expects your modules to live in `modules` folder and uses the name of the variable for the project's ID and base folder (just like `project` does).

For example, the following SBT configuration:

```scala
lazy val `my-library-core` = module

lazy val `my-library-plugin` = module 
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

[github-action]: https://github.com/alejandrohdezma/sbt-modules/actions
[github-action-badge]: https://img.shields.io/endpoint.svg?url=https%3A%2F%2Factions-badge.atrox.dev%2Falejandrohdezma%2Fsbt-modules%2Fbadge%3Fref%3Dmaster&style=flat

[maven]: https://search.maven.org/search?q=g:%20com.alejandrohdezma%20AND%20a:sbt-modules
[maven-badge]: https://maven-badges.herokuapp.com/maven-central/com.alejandrohdezma/sbt-modules/badge.svg?kill_cache=1

[steward]: https://scala-steward.org
[steward-badge]: https://img.shields.io/badge/Scala_Steward-helping-brightgreen.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=
