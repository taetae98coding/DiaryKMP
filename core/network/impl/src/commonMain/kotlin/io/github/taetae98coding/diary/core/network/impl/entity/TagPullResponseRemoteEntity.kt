package io.github.taetae98coding.diary.core.network.impl.entity

import io.github.taetae98coding.diary.core.network.api.tag.entity.TagPullRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TagPullResponseRemoteEntity(
    @SerialName("tagList") val tagList: List<TagPullRemoteEntity>,
)
