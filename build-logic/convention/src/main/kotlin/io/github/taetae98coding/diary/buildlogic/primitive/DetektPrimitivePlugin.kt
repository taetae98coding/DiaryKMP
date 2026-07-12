package io.github.taetae98coding.diary.buildlogic.primitive

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

internal class DetektPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureDetekt()
            configureDetektTask()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("dev.detekt")
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

    private fun Project.configureDetektTask() {
        tasks.withType<Detekt>().configureEach {
            include("**/*.kt")
            exclude("**/build/**")
            exclude("**/generated/resources/**")
            exclude {
                it.file.absolutePath
                    .replace('\\', '/')
                    .contains("/build/generated/compose/")
            }
            exclude {
                it.file.absolutePath
                    .replace('\\', '/')
                    .contains("/build/generated/source/buildkonfig/")
            }
        }
    }
}
