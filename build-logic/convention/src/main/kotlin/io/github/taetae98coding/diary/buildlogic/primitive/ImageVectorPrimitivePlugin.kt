package io.github.taetae98coding.diary.buildlogic.primitive

import io.github.taetae98coding.diary.buildlogic.imagevector.GenerateImageVectorTask
import io.github.taetae98coding.diary.buildlogic.namespace
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class ImageVectorPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val generateImageVector =
                tasks.register<GenerateImageVectorTask>("generateImageVector") {
                    iconDirectory.set(layout.projectDirectory.dir("icons"))
                    packageName.set("${namespace()}.icon")
                    objectName.set("DiaryIcons")
                    outputDirectory.set(layout.buildDirectory.dir("generated/imageVector"))
                }

            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                extensions.configure<KotlinMultiplatformExtension> {
                    sourceSets.named("commonMain") {
                        kotlin.srcDir(generateImageVector.map { task -> task.outputDirectory })
                    }
                }
            }
        }
    }
}
