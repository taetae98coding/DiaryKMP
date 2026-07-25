package io.github.taetae98coding.diary.core.google.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GooglePlaceDisplayNameRemoteEntity(
    @SerialName("text") val text: String = "",
)
