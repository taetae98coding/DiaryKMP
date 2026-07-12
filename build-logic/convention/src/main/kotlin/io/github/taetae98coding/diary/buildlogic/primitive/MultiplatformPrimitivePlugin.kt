package io.github.taetae98coding.diary.buildlogic.primitive

import org.gradle.api.Plugin
import org.gradle.api.Project

internal class MultiplatformPrimitivePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configurePlugin()
        }
    }

    private fun Project.configurePlugin() {
        pluginManager.apply(IosPrimitivePlugin::class.java)
        pluginManager.apply(JvmPrimitivePlugin::class.java)
        pluginManager.apply(WasmPrimitivePlugin::class.java)
    }
}
