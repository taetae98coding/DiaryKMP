package io.github.taetae98coding.diary.buildlogic.convention

import io.github.taetae98coding.diary.buildlogic.primitive.KmpPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.KoinPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.KotestPrimitivePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class DataConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(KmpPrimitivePlugin::class.java)
            pluginManager.apply(KoinPrimitivePlugin::class.java)
            pluginManager.apply(KotestPrimitivePlugin::class.java)
        }
    }
}
