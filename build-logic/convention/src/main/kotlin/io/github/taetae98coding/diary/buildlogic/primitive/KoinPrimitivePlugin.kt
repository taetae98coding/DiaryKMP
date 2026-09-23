package io.github.taetae98coding.diary.buildlogic.primitive

import io.github.taetae98coding.diary.buildlogic.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class KoinPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureDependencies()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("io.insert-koin.compiler.plugin")
    }

    private fun Project.configureDependencies() {
        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets {
                commonMain {
                    dependencies {
                        implementation(library("koin.annotations"))
                        implementation(library("koin.core"))
                    }
                }
            }
        }
    }
}
