package io.github.taetae98coding.diary.core.network.impl.taglink.entity

import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TagLinkPullResponseRemoteEntity(
    @SerialName("tagLinkList") val tagLinkList: List<TagLinkPullRemoteEntity>,
)
