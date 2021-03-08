/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import com.github.benmanes.caffeine.cache.Caffeine
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.ClassPathScanner
import it.unibo.alchemist.loader.Loader
import it.unibo.alchemist.loader.yaml.SimulationModel
import java.io.File

val cache = Caffeine.newBuilder().build<String, Loader> {
    SimulationModel.fromYaml(it)
}

class TestGuidedTourLoading : FreeSpec(
    {
        ClassPathScanner.resourcesMatching(".*\\.yml", "guidedTour").forEach { yaml ->
            "${File(yaml.file).name} should load with default parameters" {
                cache.get(yaml.readText())
                    ?.getDefault<Any, Nothing>() shouldNotBe null
            }
        }
        ClassPathScanner.resourcesMatching(".*[Vv]ariable.*\\.yml", "guidedTour").forEach { yaml ->
            "${File(yaml.file).name} should actually define variables" {
                val parsed = cache.get(yaml.readText())!!
                (parsed.variables + parsed.dependentVariables).size shouldBeGreaterThan 0
            }
        }
    }
)
