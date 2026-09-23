@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package io.github.taetae98coding.diary.buildlogic.primitive

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import io.github.taetae98coding.diary.buildlogic.BuildLogic
import io.github.taetae98coding.diary.buildlogic.namespace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

internal class AndroidLibraryPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureAndroidLibrary()
            configureKotlinHierarchyTemplate()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.android.kotlin.multiplatform.library")
        pluginManager.apply(KotlinPrimitivePlugin::class.java)
    }

    private fun Project.configureAndroidLibrary() {
        extensions.configure<KotlinMultiplatformExtension> {
            extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                namespace = namespace()
                compileSdk = BuildLogic.COMPILE_SDK
                minSdk = BuildLogic.MIN_SDK
            }
        }
    }

    private fun Project.configureKotlinHierarchyTemplate() {
        extensions.configure<KotlinMultiplatformExtension> {
            applyDefaultHierarchyTemplate {
                common {
                    group("androidJvm") {
                        withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
                        withJvm()
                    }

                    // iOS는 기본 템플릿의 ios 그룹을 다시 두어 iosMain이 이 그룹의 자식이 되게 한다.
                    // withIos()만 두면 타깃 소스셋만 자식이 되어 iosMain의 actual이 이 그룹의 expect를 보지 못한다.
                    group("nonWasm") {
                        withCompilations { it.target.platformType == KotlinPlatformType.androidJvm }
                        withJvm()
                        group("ios") {
                            withIos()
                        }
                    }

                    group("jvmWasm") {
                        withJvm()
                        withWasmJs()
                    }

                    group("nonAndroid") {
                        withJvm()
                        withWasmJs()
                        group("ios") {
                            withIos()
                        }
                    }
                }
            }
        }
    }
}
