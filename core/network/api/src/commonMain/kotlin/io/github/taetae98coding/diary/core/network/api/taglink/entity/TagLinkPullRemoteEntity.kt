package io.github.taetae98coding.diary.core.network.api.taglink.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TagLinkPullRemoteEntity(
    @SerialName("tagLink") val tagLink: TagLinkRemoteEntity,
    @SerialName("usn") val usn: Long,
)
