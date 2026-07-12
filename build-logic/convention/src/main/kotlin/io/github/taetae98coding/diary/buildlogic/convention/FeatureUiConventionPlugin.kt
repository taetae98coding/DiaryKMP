package io.github.taetae98coding.diary.buildlogic.convention

import io.github.taetae98coding.diary.buildlogic.library
import io.github.taetae98coding.diary.buildlogic.primitive.ComposePreviewPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.ComposePrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.ComposeResourcesPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.ComposeTestPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.KoinPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.KotestPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.MultiplatformAndroidLibraryPrimitivePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class FeatureUiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureDependencies()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply(MultiplatformAndroidLibraryPrimitivePlugin::class.java)
        pluginManager.apply(ComposePrimitivePlugin::class.java)
        pluginManager.apply(ComposePreviewPrimitivePlugin::class.java)
        pluginManager.apply(ComposeResourcesPrimitivePlugin::class.java)
        pluginManager.apply(ComposeTestPrimitivePlugin::class.java)
        pluginManager.apply(KoinPrimitivePlugin::class.java)
        pluginManager.apply(KotestPrimitivePlugin::class.java)
    }

    private fun Project.configureDependencies() {
        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets {
                commonMain {
                    dependencies {
                        implementation(project(":compose:core"))
                        implementation(library("koin.compose.viewmodel"))
                    }
                }
            }
        }
    }
}
