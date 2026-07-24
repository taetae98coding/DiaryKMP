package io.github.taetae98coding.diary.core.network.api.tag.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TagPullRemoteEntity(
    @SerialName("tag") val tag: TagRemoteEntity,
    @SerialName("usn") val usn: Long,
)
