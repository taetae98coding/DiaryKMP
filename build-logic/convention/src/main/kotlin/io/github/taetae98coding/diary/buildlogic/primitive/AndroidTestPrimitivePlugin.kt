package io.github.taetae98coding.diary.buildlogic.primitive

import com.android.build.api.dsl.TestExtension
import io.github.taetae98coding.diary.buildlogic.BuildLogic
import io.github.taetae98coding.diary.buildlogic.namespace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

internal class AndroidTestPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureTest()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("com.android.test")
        pluginManager.apply(KotlinPrimitivePlugin::class.java)
    }

    private fun Project.configureTest() {
        extensions.configure<TestExtension> {
            namespace = namespace()
            compileSdk = BuildLogic.COMPILE_SDK

            defaultConfig {
                minSdk = BuildLogic.MIN_SDK
                targetSdk = BuildLogic.TARGET_SDK
            }
        }
    }
}
