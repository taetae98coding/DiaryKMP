package io.github.taetae98coding.diary.core.gemini.network.impl.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ErrorResponseRemoteEntity(
    @SerialName("error") val error: ErrorRemoteEntity = ErrorRemoteEntity(),
)

@Serializable
internal data class ErrorRemoteEntity(
    @SerialName("details") val details: List<ErrorDetailRemoteEntity> = emptyList(),
)

@Serializable
internal data class ErrorDetailRemoteEntity(
    @SerialName("reason") val reason: String = "",
)
