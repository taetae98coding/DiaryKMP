package io.github.taetae98coding.diary.core.network.api.webtag.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class WebTagPullRemoteEntity(
    @SerialName("webTag") val webTag: WebTagRemoteEntity,
    @SerialName("usn") val usn: Long,
)
