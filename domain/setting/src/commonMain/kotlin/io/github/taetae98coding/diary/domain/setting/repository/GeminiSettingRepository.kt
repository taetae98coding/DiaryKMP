package io.github.taetae98coding.diary.domain.setting.repository

import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import kotlinx.coroutines.flow.Flow

public interface GeminiSettingRepository {
    public fun get(): Flow<GeminiSetting>

    public suspend fun upsert(setting: GeminiSetting)
}
