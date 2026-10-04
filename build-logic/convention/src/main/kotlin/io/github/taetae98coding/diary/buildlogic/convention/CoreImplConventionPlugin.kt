package io.github.taetae98coding.diary.buildlogic.convention

import io.github.taetae98coding.diary.buildlogic.primitive.KmpPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.KoinPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.KotestPrimitivePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal class CoreImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
            configureDependencies()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply(KmpPrimitivePlugin::class.java)
        pluginManager.apply(KoinPrimitivePlugin::class.java)
        pluginManager.apply(KotestPrimitivePlugin::class.java)
    }

    private fun Project.configureDependencies() {
        require(path.endsWith(IMPL_SUFFIX)) { "convention.core.impl은 :impl 모듈에만 적용한다: $path" }

        extensions.configure<KotlinMultiplatformExtension> {
            sourceSets {
                commonMain {
                    dependencies {
                        implementation(project(path.removeSuffix(IMPL_SUFFIX) + API_SUFFIX))
                    }
                }
            }
        }
    }
}

private const val IMPL_SUFFIX = ":impl"
private const val API_SUFFIX = ":api"
