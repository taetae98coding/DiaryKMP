@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

package io.github.taetae98coding.diary.buildlogic.primitive

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import io.github.taetae98coding.diary.buildlogic.library
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
            configurePlugin()
            configureHostTest()
            configureDependencies()
            configureTestHeap()
            configureTestJvmArgs()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply(AndroidLibraryPrimitivePlugin::class.java)
    }

    private fun Project.configureHostTest() {
        extensions.configure<KotlinMultiplatformExtension> {
            extensions.configure<KotlinMultiplatformAndroidLibraryTarget> {
                withHostTest {
                    isIncludeAndroidResources = true
                }
            }
        }
    }

    private fun Project.configureDependencies() {
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

    private fun Project.configureTestHeap() {
        tasks.withType<Test>().configureEach {
            maxHeapSize = HOST_TEST_MAX_HEAP_SIZE
        }
    }

    // Robolectric이 SDK 37의 FileDescriptor 내부 접근을 jdk.internal.access로 흉내 내므로 JDK 17 이상에서는 이 패키지를 열어 줘야 한다.
    private fun Project.configureTestJvmArgs() {
        tasks.withType<Test>().matching { it.name == HOST_TEST_TASK_NAME }.configureEach {
            jvmArgs(HOST_TEST_JVM_ARG)
        }
    }
}

private const val HOST_TEST_MAX_HEAP_SIZE = "2g"
private const val HOST_TEST_TASK_NAME = "testAndroidHostTest"
private const val HOST_TEST_JVM_ARG = "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED"
