package io.github.taetae98coding.diary.core.mapper.gemini

import io.github.taetae98coding.diary.core.gemini.network.api.entity.GeminiModelRemoteEntity
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel

public fun GeminiModelRemoteEntity.toDomain(): GeminiModel =
    GeminiModel(
        id = id,
        displayName = displayName,
        description = description,
    )
