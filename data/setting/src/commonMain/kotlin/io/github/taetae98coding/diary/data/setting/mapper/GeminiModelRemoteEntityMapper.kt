package io.github.taetae98coding.diary.data.setting.mapper

import io.github.taetae98coding.diary.core.gemini.network.api.entity.GeminiModelRemoteEntity
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel

internal fun GeminiModelRemoteEntity.toDomain(): GeminiModel =
    GeminiModel(
        id = id,
        displayName = displayName,
        description = description,
    )
