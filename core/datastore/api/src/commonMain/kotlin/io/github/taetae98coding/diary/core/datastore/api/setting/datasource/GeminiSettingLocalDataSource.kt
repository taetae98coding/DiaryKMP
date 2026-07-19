package io.github.taetae98coding.diary.core.datastore.api.setting.datasource

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import kotlinx.coroutines.flow.Flow

public interface GeminiSettingLocalDataSource {
    public fun get(): Flow<GeminiSettingLocalEntity>

    public suspend fun upsert(setting: GeminiSettingLocalEntity)
}
