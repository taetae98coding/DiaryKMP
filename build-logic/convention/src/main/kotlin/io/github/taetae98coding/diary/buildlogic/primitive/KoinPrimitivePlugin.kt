package io.github.taetae98coding.diary.buildlogic.primitive

import io.github.taetae98coding.diary.buildlogic.library
import io.github.taetae98coding.diary.buildlogic.withPlugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.koin.compiler.plugin.KoinGradleExtension

internal class KoinPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureKoinCompiler()
            configureDependencies()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("io.insert-koin.compiler.plugin")
    }

    private fun Project.configureKoinCompiler() {
        extensions.configure<KoinGradleExtension> {
            // Koin 진입점은 :app:shared에만 있어 나머지 모듈은 매 컴파일마다 "no Koin entry point" 로그를 남긴다.
            // 실제 진단(KOIN-Dxxx)은 자체 severity를 유지하므로 정보성 로그만 info로 낮춘다.
            logSeverity.set("info")

            // koin-compiler-plugin이 검증한 Kotlin 버전보다 프로젝트 Kotlin이 앞서 있을 때 나오는 경고를 낮춘다.
            versionCheckSeverity.set("info")
        }
    }

    private fun Project.configureDependencies() {
        withPlugins(listOf("org.jetbrains.kotlin.multiplatform")) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    commonMain {
                        dependencies {
                            implementation(library("koin.annotations"))
                            implementation(library("koin.core"))
                        }
                    }
                }
            }
        }
    }
}
