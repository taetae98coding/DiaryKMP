package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.McpSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.impl.McpSettingData
import io.github.taetae98coding.diary.core.datastore.impl.di.McpSettingDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class McpSettingLocalDataSourceImpl(
    @McpSettingDataStore
    private val dataStore: DataStore<McpSettingData>,
) : McpSettingLocalDataSource {
    override fun getEnabled(): Flow<Boolean?> =
        dataStore
            .data
            .map { setting -> setting.isEnabled }

    override suspend fun upsertEnabled(isEnabled: Boolean) {
        dataStore.updateData { setting -> setting.copy(isEnabled = isEnabled) }
    }
}
