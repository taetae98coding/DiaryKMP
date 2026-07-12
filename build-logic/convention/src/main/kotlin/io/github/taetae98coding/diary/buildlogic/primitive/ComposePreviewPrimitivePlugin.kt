package io.github.taetae98coding.diary.buildlogic.primitive

import io.github.taetae98coding.diary.buildlogic.library
import io.github.taetae98coding.diary.buildlogic.withPlugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class ComposePreviewPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureDependencies()
        }
    }

    private fun Project.configureDependencies() {
        withPlugins(listOf("org.jetbrains.kotlin.multiplatform")) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    commonMain {
                        dependencies {
                            implementation(library("jetbrains.compose.ui.tooling.preview"))
                        }
                    }
                }
            }
        }

        withPlugins(listOf("com.android.kotlin.multiplatform.library")) {
            dependencies.add("androidRuntimeClasspath", library("jetbrains.compose.ui.tooling"))
        }
    }
}
