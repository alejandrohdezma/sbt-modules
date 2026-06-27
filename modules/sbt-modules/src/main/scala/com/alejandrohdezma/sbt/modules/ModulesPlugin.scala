/*
 * Copyright 2020-2026 Alejandro Hernández <https://github.com/alejandrohdezma>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alejandrohdezma.sbt.modules

import scala.language.implicitConversions

import sbt.Keys._
import sbt._

@SuppressWarnings(Array("scalafix:DisableSyntax.implicitConversion"))
object ModulesPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport extends ModuleMacroCompat {

    implicit class AnyOnOps(version: String) {

      /** Transforms the given value into an `Option`.
        *
        * The result will be `None` if the version string doesn't match the provided major version.
        */
      def on[A](major: Int)(a: => A): Option[A] =
        CrossVersion
          .partialVersion(version)
          .collect { case (`major`, _) => a }

      /** Transforms the given value into an `Option`.
        *
        * The result will be `None` if the version string doesn't match the provided major/minor versions.
        */
      def on[A](major: Int, minor: Int)(a: => A): Option[A] =
        CrossVersion
          .partialVersion(version)
          .collect { case (`major`, `minor`) => a }

    }

    implicit class ProjectOpsWithProjectReferenceList(private val project: Project) extends AnyVal {

      /** Adds classpath dependencies on internal or external projects. */
      def dependsOn(deps: List[ProjectReference]): Project =
        deps.foldLeft(project)(_.dependsOn(_))

      /** Adds projects to be aggregated. When a user requests a task to run on this project from the command line, the
        * task will also be run in aggregated projects.
        */
      def aggregate(refs: List[ProjectReference]): Project =
        refs.foldLeft(project)(_.aggregate(_))

    }

    /** List of all modules created with [[module]] */
    val allModules: List[ProjectReference] =
      Option(file("./modules"))
        .filter(_.isDirectory())
        .fold(List.empty[File])(_.listFiles.toList)
        .filter(_.isDirectory())
        .map(_.getName())
        .sorted
        .map(LocalProject(_))

    implicit def ListProject2ListClasspathDependency(
        list: List[ProjectReference]
    ): List[ClasspathDep[ProjectReference]] =
      list.map(classpathDependency(_))

    /** Whether this project is a module (i.e. created via the [[module]] macro). */
    val packageIsModule = settingKey[Boolean]("Whether the project is a module (created via the [[module]] macro)")

    /** Map from module name to its [[ModuleMetadata]], including versions, dependencies, and transitive closures. */
    @transient val moduleMetadata = taskKey[Map[String, ModuleMetadata]]("Metadata for all modules in the build")

  }

  import autoImport._

  override def buildSettings = Seq(publish / skip := true)

  override def globalSettings = Seq(
    moduleMetadata := ModuleMetadata.from(state.value)
  )

  override def projectSettings = Seq(
    packageIsModule                       := false,
    publish / skip                        := !packageIsModule.value,
    Compile / unmanagedSourceDirectories ++=
      scalaVersion.value.on(2, 13)(sourceDirectory.value / "main" / "scala-2.13+"),
    Compile / unmanagedSourceDirectories ++=
      scalaVersion.value.on(3)(sourceDirectory.value / "main" / "scala-2.13+")
  )

}
