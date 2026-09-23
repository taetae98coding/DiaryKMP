package io.github.taetae98coding.diary.buildlogic.primitive

import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

internal class KotlinPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureExplicitApi()
            configureDetekt()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("dev.detekt")
    }

    private fun Project.configureExplicitApi() {
        plugins.withType<KotlinBasePlugin>().configureEach {
            kotlinExtension.explicitApi()
        }
    }

    private fun Project.configureDetekt() {
        extensions.configure<DetektExtension> {
            source.setFrom(layout.projectDirectory.dir("src"))
            config.setFrom(layout.settingsDirectory.file("config/detekt/detekt.yml"))
            basePath.set(layout.settingsDirectory)

            buildUponDefaultConfig.set(true)
            parallel.set(true)
        }
    }
}
