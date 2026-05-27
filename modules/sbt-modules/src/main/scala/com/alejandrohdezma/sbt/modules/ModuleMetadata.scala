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

import sbt._

import com.alejandrohdezma.sbt.modules.ModulesPlugin.autoImport.packageIsModule

/** A direct dependency edge between two modules.
  *
  * @param name
  *   the name of the module on the other end of the edge
  * @param configuration
  *   the Ivy configuration mapping of the `dependsOn` edge, as recorded by SBT and normalized so that a default
  *   `dependsOn` (which SBT leaves unset) is reported as `"compile"`. Examples: `"compile"`, `"test"`, `"test->test"`,
  *   `"compile->compile;test->test"`. Consumers decide what each scope means.
  */
final case class ModuleDependency(name: String, configuration: String)

/** Represents an SBT module created with the [[ModulesPlugin.autoImport.module module]] macro.
  *
  * @param version
  *   the current version from the SBT `version` setting
  * @param dependencies
  *   the modules this module directly depends on (via SBT `dependsOn`), each with the edge's configuration
  * @param transitiveDependencies
  *   names of all modules this module transitively depends on (over all scopes)
  * @param dependents
  *   the modules that directly depend on this module, each carrying the configuration of the incoming edge
  * @param transitiveDependents
  *   names of all modules that transitively depend on this module (over all scopes)
  */
final case class ModuleMetadata(
    version: String,
    dependencies: Set[ModuleDependency],
    transitiveDependencies: Set[String],
    dependents: Set[ModuleDependency],
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
        case dep if namesByProject.contains(dep.project.project) =>
          ModuleDependency(namesByProject(dep.project.project), dep.configuration.getOrElse("compile"))
      }.toSet

      name -> ModuleMetadata(version = version, dependencies = internal, transitiveDependencies = Set.empty,
        dependents = Set.empty, transitiveDependents = Set.empty)
    }.toMap

    // Second pass: populate direct dependents from the reverse of dependencies (carrying each edge's configuration)
    val dependentsByModule = initial.foldLeft(Map.empty[String, Set[ModuleDependency]]) { case (acc, (name, info)) =>
      info.dependencies.foldLeft(acc) { (acc, dep) =>
        acc.updated(dep.name, acc.getOrElse(dep.name, Set.empty) + ModuleDependency(name, dep.configuration))
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
        transitiveDependencies = closure(info.dependencies.map(_.name), _.dependencies.map(_.name)),
        transitiveDependents = closure(info.dependents.map(_.name), _.dependents.map(_.name))
      )
    }
  }

}
