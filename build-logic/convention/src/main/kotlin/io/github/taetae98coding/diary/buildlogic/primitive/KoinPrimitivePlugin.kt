package io.github.taetae98coding.diary.buildlogic.primitive

import io.github.taetae98coding.diary.buildlogic.library
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
            configureCompiler()
            configureDependencies()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("io.insert-koin.compiler.plugin")
    }

    // Koin 시작점은 앱 모듈에만 있어 나머지 모듈은 매번 '검증을 건너뛰었다'는 안내를 경고로 남긴다. 전체 그래프 검증은 시작점이 있는 모듈에서 하므로 안내만 info로 낮춘다. 의존성 누락 같은 진단은 이 설정과 무관하게 경고·오류로 남는다.
    private fun Project.configureCompiler() {
        extensions.configure<KoinGradleExtension> {
            logSeverity.set(KOIN_LOG_SEVERITY)
        }
    }

    private fun Project.configureDependencies() {
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

private const val KOIN_LOG_SEVERITY = "info"
