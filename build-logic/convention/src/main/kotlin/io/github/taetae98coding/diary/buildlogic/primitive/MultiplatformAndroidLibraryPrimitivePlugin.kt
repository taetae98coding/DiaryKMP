@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package io.github.taetae98coding.diary.buildlogic.primitive

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

internal class MultiplatformAndroidLibraryPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureKotlinHierarchyTemplate()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply(MultiplatformPrimitivePlugin::class.java)
        pluginManager.apply(AndroidLibraryPrimitivePlugin::class.java)
    }

    private fun Project.configureKotlinHierarchyTemplate() {
        extensions.configure<KotlinMultiplatformExtension> {
            applyDefaultHierarchyTemplate {
                common {
                    group("androidJvm") {
                        withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
                        withJvm()
                    }

                    group("nonWasm") {
                        withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
                        withJvm()
                        withIos()
                    }

                    group("jvmWasm") {
                        withJvm()
                        withWasmJs()
                    }

                    group("nonAndroid") {
                        withJvm()
                        withIos()
                        withWasmJs()
                    }
                }
            }
        }
    }
}
