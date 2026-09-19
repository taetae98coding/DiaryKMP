package io.github.taetae98coding.diary.core.network.api.music.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MusicDetailRemoteEntity(
    @SerialName("link") val link: String,
    @SerialName("title") val title: String,
    @SerialName("artist") val artist: String,
    @SerialName("thumbnail") val thumbnail: String,
)
