package io.github.taetae98coding.diary.core.network.impl.tag.entity

import io.github.taetae98coding.diary.core.network.api.tag.entity.TagRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class TagPushRequestRemoteEntity(
    @SerialName("tagList") val tagList: List<TagRemoteEntity>,
)
