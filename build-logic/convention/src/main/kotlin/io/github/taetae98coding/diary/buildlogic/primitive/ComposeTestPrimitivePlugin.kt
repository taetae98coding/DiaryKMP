@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package io.github.taetae98coding.diary.buildlogic.primitive

import io.github.taetae98coding.diary.buildlogic.library
import io.github.taetae98coding.diary.buildlogic.withPlugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class ComposeTestPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureDependencies()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply(AndroidHostTestPrimitivePlugin::class.java)
    }

    private fun Project.configureDependencies() {
        withPlugins(
            listOf(
                "org.jetbrains.kotlin.multiplatform",
                "com.android.kotlin.multiplatform.library",
            ),
        ) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    getByName("androidHostTest") {
                        dependencies {
                            implementation(library("jetbrains.compose.ui.test.junit4"))
                            implementation(library("androidx.compose.ui.test.manifest"))
                        }
                    }
                }
            }
        }
    }
}
