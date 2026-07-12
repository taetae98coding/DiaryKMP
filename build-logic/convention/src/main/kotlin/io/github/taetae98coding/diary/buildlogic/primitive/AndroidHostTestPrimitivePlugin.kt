@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package io.github.taetae98coding.diary.buildlogic.primitive

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import io.github.taetae98coding.diary.buildlogic.library
import io.github.taetae98coding.diary.buildlogic.withPlugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class AndroidHostTestPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureAndroidLibrary()
            configureDependencies()
            configureTestHeap()
        }
    }

    private fun Project.configureAndroidLibrary() {
        withPlugins(
            listOf(
                "org.jetbrains.kotlin.multiplatform",
                "com.android.kotlin.multiplatform.library",
            ),
        ) {
            extensions.configure<KotlinMultiplatformExtension> {
                extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                    withHostTest {
                        isIncludeAndroidResources = true
                    }
                }
            }
        }
    }

    // Robolectric은 테스트마다 Android 리소스와 Compose 컴포지션을 한 JVM에 쌓아 두므로 Gradle 기본 힙(512m)으로는 모듈 전체를 한 번에 실행할 수 없다.
    private fun Project.configureTestHeap() {
        tasks.withType<Test>().configureEach {
            maxHeapSize = HOST_TEST_MAX_HEAP_SIZE
        }
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
                            implementation(kotlin("test"))
                            implementation(library("robolectric"))
                        }
                    }
                }
            }
        }
    }
}

private const val HOST_TEST_MAX_HEAP_SIZE = "2g"
