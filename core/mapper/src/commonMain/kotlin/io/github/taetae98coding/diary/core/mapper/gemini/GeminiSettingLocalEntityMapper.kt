package io.github.taetae98coding.diary.core.mapper.gemini

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting

public fun GeminiSettingLocalEntity.toDomain(): GeminiSetting =
    GeminiSetting(
        apiKey = apiKey,
        model = model,
        systemPrompt = systemPrompt,
    )

public fun GeminiSetting.toLocal(): GeminiSettingLocalEntity =
    GeminiSettingLocalEntity(
        apiKey = apiKey,
        model = model,
        systemPrompt = systemPrompt,
    )
