/*
 * Copyright 2020 Alejandro Hernández <https://github.com/alejandrohdezma>
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

import scala.collection.mutable
import scala.reflect.macros._

import sbt.Keys._
import sbt._

import scala.language.experimental.macros

@SuppressWarnings(Array("scalafix:Disable.scala.collection.mutable", "scalafix:DisableSyntax.implicitConversion"))
object ModulesPlugin extends AutoPlugin {

  override def trigger = allRequirements

  object autoImport {

    /** List of all modules created with [[module]] */
    val allModules: mutable.MutableList[Project] = mutable.MutableList[Project]()

    implicit def MutableListProject2ListClasspathDependency(
        list: mutable.MutableList[Project]
    ): List[ClasspathDep[ProjectReference]] =
      list.map(classpathDependency(_)).toList

    /**
     * Creates a new Project with `modules` as base directory.
     *
      * This is a macro that expects to be assigned directly to a `val`.
     *
      * The name of the val is used as the project ID and the name of its base
     * directory inside `modules`.
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
        val project = Project(name.splice, file("modules") / name.splice)
          .settings(skip in publish := false)

        ModulesPlugin.autoImport.allModules += project

        project
      }
    }

  }

  override def buildSettings: Seq[Def.Setting[_]] = Seq(publish / skip := true)

}
