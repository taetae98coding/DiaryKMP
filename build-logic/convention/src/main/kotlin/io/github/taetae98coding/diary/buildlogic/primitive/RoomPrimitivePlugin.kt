package io.github.taetae98coding.diary.buildlogic.primitive

import androidx.room3.gradle.RoomExtension
import io.github.taetae98coding.diary.buildlogic.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

internal class RoomPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureRoom()
            configureCompiler()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply("org.jetbrains.kotlin.multiplatform")
        pluginManager.apply("com.google.devtools.ksp")
        pluginManager.apply("androidx.room3")
    }

    private fun Project.configureRoom() {
        extensions.configure<RoomExtension> {
            schemaDirectory(
                layout.projectDirectory
                    .dir("schemas")
                    .asFile.path,
            )
        }
    }

    private fun Project.configureCompiler() {
        extensions.configure<KotlinMultiplatformExtension> {
            targets.configureEach {
                if (platformType != KotlinPlatformType.common) {
                    dependencies.add("ksp${targetName.replaceFirstChar(Char::uppercaseChar)}", library("androidx.room3.compiler"))
                }
            }

            // Room이 데이터베이스 구현을 expect/actual 클래스로 생성한다.
            compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}
