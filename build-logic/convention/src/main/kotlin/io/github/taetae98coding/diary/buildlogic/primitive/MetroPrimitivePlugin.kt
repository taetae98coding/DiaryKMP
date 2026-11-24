package io.github.taetae98coding.diary.buildlogic.primitive

import dev.zacsweers.metro.gradle.MetroPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal class MetroPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureCompiler()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("dev.zacsweers.metro")
    }

    private fun Project.configureCompiler() {
        extensions.configure<MetroPluginExtension> {
            generateContributionProviders.set(true)
        }
    }
}
