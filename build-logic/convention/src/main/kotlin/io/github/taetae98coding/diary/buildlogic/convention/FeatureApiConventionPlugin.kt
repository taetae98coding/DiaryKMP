package io.github.taetae98coding.diary.buildlogic.convention

import io.github.taetae98coding.diary.buildlogic.primitive.KmpPrimitivePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class FeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureDependencies()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply(KmpPrimitivePlugin::class.java)
        pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
    }

    private fun Project.configureDependencies() {
        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets {
                commonMain {
                    dependencies {
                        api(project(":core:navigation"))
                    }
                }
            }
        }
    }
}
