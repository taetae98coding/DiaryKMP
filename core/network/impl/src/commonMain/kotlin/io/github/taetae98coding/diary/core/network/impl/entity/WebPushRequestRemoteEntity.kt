package io.github.taetae98coding.diary.core.network.impl.entity

import io.github.taetae98coding.diary.core.network.api.web.entity.WebRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class WebPushRequestRemoteEntity(
    @SerialName("webList") val webList: List<WebRemoteEntity>,
)
