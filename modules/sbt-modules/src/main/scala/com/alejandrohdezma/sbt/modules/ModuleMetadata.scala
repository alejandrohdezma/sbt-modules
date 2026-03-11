/*
 * Copyright 2020-2024 Alejandro Hernández <https://github.com/alejandrohdezma>
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

import sbt._

import com.alejandrohdezma.sbt.modules.ModulesPlugin.autoImport.packageIsModule

/** Represents an SBT module created with the [[ModulesPlugin.autoImport.module module]] macro.
  *
  * @param version
  *   the current version from the SBT `version` setting
  * @param dependencies
  *   names of other modules this module depends on (via SBT `dependsOn`)
  * @param transitiveDependencies
  *   names of all modules this module transitively depends on
  * @param dependents
  *   names of modules that directly depend on this module (reverse of `internalDeps`)
  * @param transitiveDependents
  *   names of all modules that transitively depend on this module
  */
final case class ModuleMetadata(
    version: String,
    dependencies: Set[String],
    transitiveDependencies: Set[String],
    dependents: Set[String],
    transitiveDependents: Set[String]
)

object ModuleMetadata {

  /** Discovers all modules in the build.
    *
    * A module is any project whose `packageIsModule` setting is `true`. Inspects the SBT `buildDependencies` (the
    * compile-time `dependsOn` graph) to resolve internal dependencies between modules. Transitive dependencies and
    * transitive dependents are computed as closures over the dependency graph.
    *
    * @return
    *   map from module name to [[ModuleMetadata]]
    */
  def from(state: State): Map[String, ModuleMetadata] = {
    val extracted = Project.extract(state)

    val buildDeps = extracted.get(Keys.buildDependencies)

    val moduleRefs = extracted.structure.allProjectRefs.filter(ref => extracted.get(ref / packageIsModule))

    val namesByProject = moduleRefs.map(ref => ref.project -> extracted.get(ref / Keys.name)).toMap

    // First pass: build modules with empty dependents
    val initial = moduleRefs.map { ref =>
      val name         = namesByProject(ref.project)
      val version      = extracted.get(ref / Keys.version)
      val dependencies = buildDeps.classpath.getOrElse(ref, Nil)
      val internal     = dependencies.collect {
        case dep if namesByProject.contains(dep.project.project) => namesByProject(dep.project.project)
      }.toSet

      name -> ModuleMetadata(version = version, dependencies = internal, transitiveDependencies = Set.empty,
        dependents = Set.empty, transitiveDependents = Set.empty)
    }.toMap

    // Second pass: populate direct dependents from the reverse of dependencies
    val dependentsByModule = initial.foldLeft(Map.empty[String, Set[String]]) { case (acc, (name, info)) =>
      info.dependencies.foldLeft(acc) { (acc, dep) =>
        acc.updated(dep, acc.getOrElse(dep, Set.empty) + name)
      }
    }

    val withDependents = initial.map { case (name, info) =>
      name -> info.copy(dependents = dependentsByModule.getOrElse(name, Set.empty))
    }

    // Computes the transitive closure of a module graph via BFS.
    // Walks the graph in the direction given by `follow` (e.g. `_.dependencies` or `_.dependents`),
    // collecting all reachable nodes. `toVisit` is the frontier, `follow` extracts neighbors,
    // and `result` accumulates visited nodes (`.diff(next)` prevents revisiting).
    @tailrec
    def closure(
        toVisit: Set[String],
        follow: ModuleMetadata => Set[String],
        result: Set[String] = Set.empty
    ): Set[String] =
      if (toVisit.isEmpty) result
      else {
        val next       = result ++ toVisit
        val newToVisit = toVisit.flatMap(withDependents.get(_).map(follow).getOrElse(Set.empty)).diff(next)
        closure(newToVisit, follow, next)
      }

    withDependents.map { case (name, info) =>
      name -> info.copy(
        transitiveDependencies = closure(info.dependencies, _.dependencies),
        transitiveDependents = closure(info.dependents, _.dependents)
      )
    }
  }

}
