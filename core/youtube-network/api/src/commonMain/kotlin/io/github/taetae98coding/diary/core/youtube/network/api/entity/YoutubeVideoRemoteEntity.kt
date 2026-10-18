package io.github.taetae98coding.diary.core.youtube.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class YoutubeVideoRemoteEntity(
    @SerialName("title") val title: String,
    @SerialName("author_name") val authorName: String,
)
