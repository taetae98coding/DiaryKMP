package io.github.taetae98coding.diary.core.gemini.network.impl.entity

import io.github.taetae98coding.diary.core.gemini.network.api.entity.GeminiModelRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ListModelsResponseRemoteEntity(
    @SerialName("models") val models: List<GeminiModelRemoteEntity> = emptyList(),
)
