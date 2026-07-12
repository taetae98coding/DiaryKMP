package io.github.taetae98coding.diary.buildlogic.primitive

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import io.github.taetae98coding.diary.buildlogic.library
import io.github.taetae98coding.diary.buildlogic.namespace
import io.github.taetae98coding.diary.buildlogic.withPlugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class ComposeResourcesPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureComposeResources()
            configureAndroidResources()
            configureDependencies()
        }
    }

    private fun Project.configureComposeResources() {
        withPlugins(listOf("org.jetbrains.compose")) {
            extensions.configure<ComposeExtension> {
                extensions.configure<ResourcesExtension>("resources") {
                    publicResClass = false
                    packageOfResClass = namespace()
                    generateResClass = always
                }
            }
        }
    }

    private fun Project.configureAndroidResources() {
        withPlugins(
            listOf(
                "org.jetbrains.kotlin.multiplatform",
                "com.android.kotlin.multiplatform.library",
            ),
        ) {
            extensions.configure<KotlinMultiplatformExtension> {
                extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                    androidResources {
                        enable = true
                    }
                }
            }
        }
    }

    private fun Project.configureDependencies() {
        withPlugins(listOf("org.jetbrains.kotlin.multiplatform")) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    commonMain {
                        dependencies {
                            implementation(library("jetbrains.compose.components.resources"))
                        }
                    }
                }
            }
        }
    }
}
