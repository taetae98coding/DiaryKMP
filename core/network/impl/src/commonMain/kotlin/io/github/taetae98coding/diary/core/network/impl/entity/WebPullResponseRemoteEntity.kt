package io.github.taetae98coding.diary.core.network.impl.entity

import io.github.taetae98coding.diary.core.network.api.web.entity.WebPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class WebPullResponseRemoteEntity(
    @SerialName("webList") val webList: List<WebPullRemoteEntity>,
)
