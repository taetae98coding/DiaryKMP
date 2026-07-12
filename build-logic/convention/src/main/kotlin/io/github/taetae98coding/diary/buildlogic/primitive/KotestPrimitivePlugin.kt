package io.github.taetae98coding.diary.buildlogic.primitive

import io.github.taetae98coding.diary.buildlogic.library
import io.github.taetae98coding.diary.buildlogic.withPlugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class KotestPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureDependencies()
            configureJvmTest()
        }
    }

    private fun Project.configureDependencies() {
        withPlugins(listOf("org.jetbrains.kotlin.multiplatform")) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    jvmTest {
                        dependencies {
                            implementation(library("kotest.runner.junit5"))
                            implementation(library("kotlinx.coroutines.test"))
                            implementation(library("mockk"))
                            implementation(library("turbine"))
                            implementation(fixtureMonkeyDependency())
                        }
                    }
                }

                sourceSets.matching { it.name == "androidHostTest" }.configureEach {
                    dependencies {
                        implementation(library("kotest.assertions.core"))
                        implementation(library("mockk"))
                        implementation(library("turbine"))
                        implementation(fixtureMonkeyDependency())
                    }
                }
            }
        }
    }

    // 공용 FixtureMonkey 구성 모듈 자신은 그 모듈을 다시 의존할 수 없어 라이브러리를 그대로 쓴다.
    private fun Project.fixtureMonkeyDependency(): Any =
        if (path == FIXTURE_MONKEY_PATH) {
            library("fixture.monkey.kotlin")
        } else {
            dependencies.project(mapOf("path" to FIXTURE_MONKEY_PATH))
        }

    private fun Project.configureJvmTest() {
        tasks.withType<Test>().matching { it.name == "jvmTest" }.configureEach {
            useJUnitPlatform()
        }
    }
}

private const val FIXTURE_MONKEY_PATH = ":library:fixture-monkey"
