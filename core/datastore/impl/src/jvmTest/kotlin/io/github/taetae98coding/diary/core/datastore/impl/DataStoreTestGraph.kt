package io.github.taetae98coding.diary.core.datastore.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.GeminiSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.MapSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.McpSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.web.datasource.WebLoginLocalDataSource
import java.nio.file.Path

@DependencyGraph(scope = AppScope::class, excludes = [JvmDataStoreModule::class])
internal interface DataStoreTestGraph {
    val geminiSettingLocalDataSource: GeminiSettingLocalDataSource
    val holidaySettingLocalDataSource: HolidaySettingLocalDataSource
    val mapSettingLocalDataSource: MapSettingLocalDataSource
    val mcpSettingLocalDataSource: McpSettingLocalDataSource
    val webLoginLocalDataSource: WebLoginLocalDataSource

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides pathResolver: SettingPathResolver,
        ): DataStoreTestGraph
    }
}

internal fun createDataStoreTestGraph(directory: Path): DataStoreTestGraph =
    createGraphFactory<DataStoreTestGraph.Factory>()
        .create(pathResolver = SettingPathResolver { name -> directory.resolve(name).toString() })
