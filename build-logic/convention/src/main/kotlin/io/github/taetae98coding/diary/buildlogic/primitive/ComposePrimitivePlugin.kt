package io.github.taetae98coding.diary.buildlogic.primitive

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import io.github.taetae98coding.diary.buildlogic.library
import io.github.taetae98coding.diary.buildlogic.namespace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.compose.resources.ResourcesExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class ComposePrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureComposeCompiler()
            configureComposeResources()
            configureMultiplatform()
            configureAndroidLibrary()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("org.jetbrains.compose")
        pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
    }

    private fun Project.configureComposeCompiler() {
        extensions.configure<ComposeCompilerGradlePluginExtension> {
            // 리포트는 컴파일마다 파일을 써서 빌드를 느리게 하므로 -PenableComposeCompilerMetrics=true, -PenableComposeCompilerReports=true로 요청할 때만 만든다.
            if (providers.gradleProperty(COMPOSE_COMPILER_METRICS_PROPERTY).orNull.toBoolean()) {
                metricsDestination.set(layout.buildDirectory.dir("compose/metrics"))
            }
            if (providers.gradleProperty(COMPOSE_COMPILER_REPORTS_PROPERTY).orNull.toBoolean()) {
                reportsDestination.set(layout.buildDirectory.dir("compose/reports"))
            }
            stabilityConfigurationFiles.add(layout.settingsDirectory.file("config/compose/stability.conf"))
        }
    }

    private fun Project.configureComposeResources() {
        extensions.configure<ComposeExtension> {
            extensions.configure<ResourcesExtension>("resources") {
                publicResClass = false
                packageOfResClass = namespace()
            }
        }
    }

    // Android 애플리케이션(:app:android)에도 적용되므로 KMP 확장은 그 플러그인이 있을 때만 다룬다.
    private fun Project.configureMultiplatform() {
        pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    commonMain {
                        dependencies {
                            implementation(library("jetbrains.compose.components.resources"))
                            implementation(library("jetbrains.compose.ui.tooling.preview"))
                        }
                    }
                }
            }
        }
    }

    private fun Project.configureAndroidLibrary() {
        pluginManager.withPlugin("com.android.kotlin.multiplatform.library") {
            extensions.configure<KotlinMultiplatformExtension> {
                extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                    androidResources {
                        enable = true
                    }
                }
            }
            dependencies.add("androidRuntimeClasspath", library("jetbrains.compose.ui.tooling"))
        }
    }
}

private const val COMPOSE_COMPILER_METRICS_PROPERTY = "enableComposeCompilerMetrics"
private const val COMPOSE_COMPILER_REPORTS_PROPERTY = "enableComposeCompilerReports"
