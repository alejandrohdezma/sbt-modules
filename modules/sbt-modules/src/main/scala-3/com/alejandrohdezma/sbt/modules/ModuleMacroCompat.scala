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

import scala.annotation.tailrec
import scala.quoted._

import sbt._

import com.alejandrohdezma.sbt.modules.ModulesPlugin.autoImport.packageIsModule

trait ModuleMacroCompat {

  /** Creates a new Project with `modules` as base directory.
    *
    * This is a macro that expects to be assigned directly to a `val`.
    *
    * The name of the val is used as the project ID and the name of its base directory inside `modules`.
    */
  inline def module: Project = ${ Macros.projectMacroImpl }

}

/** Copied as-is from [[https://github.com/sbt/sbt SBT]]'s `sbt.std.KeyMacro`. */
private[modules] object Macros {

  def projectMacroImpl(using Quotes): Expr[Project] = {
    val name = definingValName

    '{ Project($name, file("modules") / $name).settings(packageIsModule := true) }
  }

  private def definingValName(using Quotes): Expr[String] = {
    import quotes.reflect._

    val term = enclosingTerm

    if (term.isValDef) Expr(term.name)
    else report.errorAndAbort("module must be directly assigned to a val, such as `val x = module`.")
  }

  private def enclosingTerm(using Quotes) = {
    import quotes.reflect._

    @tailrec def loop(symbol: Symbol): Symbol =
      symbol match {
        case s if s.flags.is(Flags.Macro)     => loop(s.owner)
        case s if s.flags.is(Flags.Synthetic) => loop(s.owner)
        case s if !s.isTerm                   => loop(s.owner)
        case s                                => s
      }

    loop(Symbol.spliceOwner)
  }

}
