@file:OptIn(ExperimentalWasmDsl::class)

package io.github.taetae98coding.diary.buildlogic.primitive

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class WasmPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureWasm()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply(KotlinProjectPrimitivePlugin::class.java)
    }

    private fun Project.configureWasm() {
        extensions.configure<KotlinMultiplatformExtension> {
            wasmJs {
                browser()
            }
        }
    }
}
