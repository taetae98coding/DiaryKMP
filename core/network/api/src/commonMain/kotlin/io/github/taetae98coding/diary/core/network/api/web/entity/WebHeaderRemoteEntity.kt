package io.github.taetae98coding.diary.core.network.api.web.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class WebHeaderRemoteEntity(
    @SerialName("name") val name: String,
    @SerialName("value") val value: String,
)
