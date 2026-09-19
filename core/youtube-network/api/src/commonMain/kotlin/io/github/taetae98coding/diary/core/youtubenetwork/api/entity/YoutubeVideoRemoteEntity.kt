package io.github.taetae98coding.diary.core.youtubenetwork.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class YoutubeVideoRemoteEntity(
    @SerialName("title") val title: String,
    @SerialName("author_name") val authorName: String,
    @SerialName("thumbnail_url") val thumbnailUrl: String,
)
