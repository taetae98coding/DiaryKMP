package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.GeminiSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.di.GeminiSettingDataStore
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
internal class GeminiSettingLocalDataSourceImpl(
    @GeminiSettingDataStore
    private val dataStore: DataStore<GeminiSettingLocalEntity>,
) : GeminiSettingLocalDataSource {
    override fun get(): Flow<GeminiSettingLocalEntity> = dataStore.data

    override suspend fun upsert(setting: GeminiSettingLocalEntity) {
        dataStore.updateData { setting }
    }
}
