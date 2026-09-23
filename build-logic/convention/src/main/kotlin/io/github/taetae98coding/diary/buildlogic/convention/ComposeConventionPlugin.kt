package io.github.taetae98coding.diary.buildlogic.convention

import io.github.taetae98coding.diary.buildlogic.primitive.AndroidLibraryPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.ComposePrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.ComposeTestPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.KmpPrimitivePlugin
import io.github.taetae98coding.diary.buildlogic.primitive.KotestPrimitivePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

internal class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(KmpPrimitivePlugin::class.java)
            pluginManager.apply(AndroidLibraryPrimitivePlugin::class.java)
            pluginManager.apply(ComposePrimitivePlugin::class.java)
            pluginManager.apply(ComposeTestPrimitivePlugin::class.java)
            pluginManager.apply(KotestPrimitivePlugin::class.java)
        }
    }
}
