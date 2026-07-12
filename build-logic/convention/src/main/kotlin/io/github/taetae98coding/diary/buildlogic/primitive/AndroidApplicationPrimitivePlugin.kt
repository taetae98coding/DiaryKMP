package io.github.taetae98coding.diary.buildlogic.primitive

import com.android.build.api.dsl.ApplicationExtension
import io.github.taetae98coding.diary.buildlogic.BuildLogic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal class AndroidApplicationPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureApplication()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("com.android.application")
        pluginManager.apply(KotlinProjectPrimitivePlugin::class.java)
    }

    private fun Project.configureApplication() {
        extensions.configure<ApplicationExtension> {
            compileSdk = BuildLogic.COMPILE_SDK

            defaultConfig {
                minSdk = BuildLogic.MIN_SDK
                targetSdk = BuildLogic.TARGET_SDK
            }
        }
    }
}
