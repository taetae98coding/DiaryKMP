package io.github.taetae98coding.diary.core.network.api.webtag.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class WebTagRemoteEntity(
    @SerialName("webId") val webId: Uuid,
    @SerialName("tagId") val tagId: Uuid,
    @SerialName("isDeleted") val isDeleted: Boolean,
    @SerialName("updatedAt") val updatedAt: Instant,
    @SerialName("createdAt") val createdAt: Instant,
)
