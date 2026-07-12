package io.github.taetae98coding.diary.buildlogic.primitive

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal class ComposePrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureComposeCompiler()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    }

    private fun Project.configureComposeCompiler() {
        extensions.configure<ComposeCompilerGradlePluginExtension> {
            metricsDestination.set(layout.buildDirectory.dir("compose/metrics"))
            reportsDestination.set(layout.buildDirectory.dir("compose/reports"))
            stabilityConfigurationFiles.add(layout.settingsDirectory.file("config/compose/stability.conf"))
        }
    }
}
