package io.github.taetae98coding.diary.core.datastore.impl.datasource

import androidx.datastore.core.DataStore
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.MapSettingLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.MapProviderLocalEntity
import io.github.taetae98coding.diary.core.datastore.impl.MapSettingData
import io.github.taetae98coding.diary.core.datastore.impl.di.MapSettingDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class MapSettingLocalDataSourceImpl(
    @MapSettingDataStore
    private val dataStore: DataStore<MapSettingData>,
) : MapSettingLocalDataSource {
    override fun getDefaultProvider(): Flow<MapProviderLocalEntity?> =
        dataStore
            .data
            .map { setting -> MapProviderLocalEntity.fromPersistentValue(setting.defaultProvider) }

    override suspend fun setDefaultProvider(provider: MapProviderLocalEntity) {
        dataStore.updateData { setting -> setting.copy(defaultProvider = provider.persistentValue) }
    }
}
