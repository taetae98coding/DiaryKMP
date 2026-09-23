package io.github.taetae98coding.diary.core.network.impl.webtag.entity

import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class WebTagPushRequestRemoteEntity(
    @SerialName("webTagList") val webTagList: List<WebTagRemoteEntity>,
)
