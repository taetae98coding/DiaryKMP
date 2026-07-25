package io.github.taetae98coding.diary.core.gemini.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GeminiModelRemoteEntity(
    @SerialName("name") val id: String,
    @SerialName("displayName") val displayName: String = "",
    @SerialName("description") val description: String = "",
    @SerialName("supportedGenerationMethods") val supportedGenerationMethods: List<String> = emptyList(),
)
