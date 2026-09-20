package io.github.taetae98coding.diary.data.setting.mapper

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting

internal fun GeminiSettingLocalEntity.toDomain(): GeminiSetting =
    GeminiSetting(
        apiKey = apiKey,
        model = model,
        systemPrompt = systemPrompt,
    )

internal fun GeminiSetting.toLocal(): GeminiSettingLocalEntity =
    GeminiSettingLocalEntity(
        apiKey = apiKey,
        model = model,
        systemPrompt = systemPrompt,
    )
