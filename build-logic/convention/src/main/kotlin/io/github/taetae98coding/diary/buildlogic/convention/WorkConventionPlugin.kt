package io.github.taetae98coding.diary.buildlogic.convention

import io.github.taetae98coding.diary.buildlogic.primitive.AndroidLibraryPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.KmpPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.KotestPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.MetroPrimitivePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class WorkConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(KmpPrimitivePlugin::class.java)
            pluginManager.apply(AndroidLibraryPrimitivePlugin::class.java)
            pluginManager.apply(MetroPrimitivePlugin::class.java)
            pluginManager.apply(KotestPrimitivePlugin::class.java)
        }
    }
}
