package io.github.taetae98coding.diary.core.network.api.taglink.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class TagLinkRemoteEntity(
    @SerialName("fromTagId") val fromTagId: Uuid,
    @SerialName("toTagId") val toTagId: Uuid,
    @SerialName("isDeleted") val isDeleted: Boolean,
    @SerialName("updatedAt") val updatedAt: Instant,
    @SerialName("createdAt") val createdAt: Instant,
)
