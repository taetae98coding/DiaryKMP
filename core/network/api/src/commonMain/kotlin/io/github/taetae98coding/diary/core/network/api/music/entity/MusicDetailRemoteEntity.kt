package io.github.taetae98coding.diary.core.network.api.music.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MusicDetailRemoteEntity(
    @SerialName("title") val title: String,
    @SerialName("artist") val artist: String,
)
