package io.github.taetae98coding.diary.buildlogic.primitive

import io.github.taetae98coding.diary.buildlogic.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class KotestPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureDependencies()
            configureJvmTest()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
    }

    private fun Project.configureDependencies() {
        extensions.configure<KotlinMultiplatformExtension> {
            // 테스트 데이터 생성은 모든 타깃에서 동작하므로 commonTest에 두어 jvmTest와 androidHostTest가 물려받게 한다.
            if (path != FIXTURE_PATH) {
                sourceSets.named("commonTest") {
                    dependencies {
                        implementation(project(FIXTURE_PATH))
                    }
                }
            }

            // jvm 타깃을 build 파일에서 직접 선언하는 모듈에서는 이 플러그인이 타깃보다 먼저 적용되므로 소스셋을 만들지 않고 기다린다.
            sourceSets.matching { it.name == "jvmTest" }.configureEach {
                dependencies {
                    implementation(library("kotest.runner.junit5"))
                    implementation(library("kotlinx.coroutines.test"))
                    implementation(library("mockk"))
                    implementation(library("turbine"))
                }
            }

            sourceSets.matching { it.name == "androidHostTest" }.configureEach {
                dependencies {
                    implementation(library("kotest.assertions.core"))
                    implementation(library("mockk"))
                    implementation(library("turbine"))
                }
            }
        }
    }

    private fun Project.configureJvmTest() {
        tasks.withType<Test>().matching { it.name == "jvmTest" }.configureEach {
            useJUnitPlatform()
        }
    }
}

private const val FIXTURE_PATH = ":library:fixture"
