package io.github.taetae98coding.diary.domain.setting.repository

import io.github.taetae98coding.diary.core.model.map.MapProvider
import kotlinx.coroutines.flow.Flow

public interface MapSettingRepository {
    public fun getDefaultProvider(): Flow<MapProvider>

    public suspend fun setDefaultProvider(provider: MapProvider)
}
