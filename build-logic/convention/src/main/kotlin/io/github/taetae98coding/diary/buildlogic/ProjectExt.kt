package io.github.taetae98coding.diary.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType
import java.util.Properties

internal fun Project.library(alias: String): Provider<MinimalExternalModuleDependency> =
    extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")
        .findLibrary(alias)
        .orElseThrow { IllegalArgumentException("Version catalog library not found: $alias") }

internal fun Project.withPlugins(
    ids: List<String>,
    action: () -> Unit,
) {
    if (ids.isEmpty()) {
        action()
        return
    }

    pluginManager.withPlugin(ids.first()) {
        withPlugins(ids.takeLast(ids.size - 1), action)
    }
}

public fun Project.namespace(): String = "${BuildLogic.NAMESPACE}.${path.removePrefix(":").replace(':', '.').replace('-', '.')}"

public fun Project.localProperties(): Properties =
    Properties().apply {
        val localPropertiesFile = layout.settingsDirectory.file("local.properties").asFile
        if (localPropertiesFile.isFile) {
            localPropertiesFile.inputStream().use(::load)
        }
    }
