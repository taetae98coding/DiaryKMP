package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.GeminiSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.GeminiSettingData
import io.github.taetae98coding.diary.core.datastore.impl.di.GeminiSettingDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class GeminiSettingLocalDataSourceImpl(
    @GeminiSettingDataStore
    private val dataStore: DataStore<GeminiSettingData>,
) : GeminiSettingLocalDataSource {
    override fun get(): Flow<GeminiSettingLocalEntity> =
        dataStore
            .data
            .map { setting -> setting.toLocalEntity() }

    override suspend fun upsert(setting: GeminiSettingLocalEntity) {
        dataStore.updateData { setting.toData() }
    }
}

private fun GeminiSettingData.toLocalEntity(): GeminiSettingLocalEntity =
    GeminiSettingLocalEntity(
        apiKey = apiKey,
        model = model,
        systemPrompt = systemPrompt,
    )

private fun GeminiSettingLocalEntity.toData(): GeminiSettingData =
    GeminiSettingData(
        apiKey = apiKey,
        model = model,
        systemPrompt = systemPrompt,
    )
