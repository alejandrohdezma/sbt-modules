/*
 * Copyright 2020-2021 Alejandro Hernández <https://github.com/alejandrohdezma>
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

import scala.language.experimental.macros
import scala.reflect.macros._

import sbt.Keys._
import sbt._

@SuppressWarnings(Array("scalafix:DisableSyntax.implicitConversion"))
object ModulesPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {

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
        project.dependsOn(deps: _*)

      /** Adds projects to be aggregated. When a user requests a task to run on this project from the command line, the
        * task will also be run in aggregated projects.
        */
      def aggregate(refs: List[ProjectReference]): Project =
        project.aggregate(refs: _*)

    }

    /** List of all modules created with [[module]] */
    val allModules: List[ProjectReference] =
      Option(file("./modules"))
        .filter(_.isDirectory())
        .fold(List.empty[File])(_.listFiles.toList)
        .filter(_.isDirectory())
        .map(_.getName())
        .map(LocalProject(_))

    implicit def ListProject2ListClasspathDependency(
        list: List[ProjectReference]
    ): List[ClasspathDep[ProjectReference]] =
      list.map(classpathDependency(_))

    /** Creates a new Project with `modules` as base directory.
      *
      * This is a macro that expects to be assigned directly to a `val`.
      *
      * The name of the val is used as the project ID and the name of its base directory inside `modules`.
      */
    def module: Project = macro Macros.projectMacroImpl

  }

  private[modules] class Macros(val c: blackbox.Context) {

    import c.universe._

    def projectMacroImpl: c.Expr[Project] = {

      val enclosingValName =
        KeyMacro.definingValName(
          c,
          n => s"""$n must be directly assigned to a val, such as `val x = $n`."""
        )

      val name = c.Expr[String](Literal(Constant(enclosingValName)))

      reify {
        Project(name.splice, file("modules") / name.splice).settings(publish / skip := false)
      }
    }

  }

  override def buildSettings: Seq[Def.Setting[_]] = Seq(publish / skip := true)

}
