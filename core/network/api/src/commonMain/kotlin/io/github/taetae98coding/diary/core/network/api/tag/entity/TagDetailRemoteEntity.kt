package io.github.taetae98coding.diary.core.network.api.tag.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TagDetailRemoteEntity(
    @SerialName("emoji") val emoji: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("color") val color: Long,
)
