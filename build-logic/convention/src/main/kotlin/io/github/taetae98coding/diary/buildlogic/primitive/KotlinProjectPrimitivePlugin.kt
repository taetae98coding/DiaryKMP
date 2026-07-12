package io.github.taetae98coding.diary.buildlogic.primitive

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

internal class KotlinProjectPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureExplicitApi()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply(DetektPrimitivePlugin::class.java)
    }

    private fun Project.configureExplicitApi() {
        plugins.withType<KotlinBasePlugin>().configureEach {
            kotlinExtension.explicitApi()
        }
    }
}
