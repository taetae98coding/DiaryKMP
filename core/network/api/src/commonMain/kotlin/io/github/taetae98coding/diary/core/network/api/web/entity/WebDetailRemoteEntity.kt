package io.github.taetae98coding.diary.core.network.api.web.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class WebDetailRemoteEntity(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("url") val url: String,
    @SerialName("headerList") val headerList: List<WebHeaderRemoteEntity>,
)
