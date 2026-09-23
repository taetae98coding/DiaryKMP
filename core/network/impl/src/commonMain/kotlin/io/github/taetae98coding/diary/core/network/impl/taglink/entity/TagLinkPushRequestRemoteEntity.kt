package io.github.taetae98coding.diary.core.network.impl.taglink.entity

import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TagLinkPushRequestRemoteEntity(
    @SerialName("tagLinkList") val tagLinkList: List<TagLinkRemoteEntity>,
)
