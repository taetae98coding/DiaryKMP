package io.github.taetae98coding.diary.core.datastore.api.setting.datasource

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.MapProviderLocalEntity
import kotlinx.coroutines.flow.Flow

public interface MapSettingLocalDataSource {
    public fun getDefaultProvider(): Flow<MapProviderLocalEntity?>

    public suspend fun setDefaultProvider(provider: MapProviderLocalEntity)
}
