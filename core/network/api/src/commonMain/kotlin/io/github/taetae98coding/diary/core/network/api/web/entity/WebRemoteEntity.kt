package io.github.taetae98coding.diary.core.network.api.web.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class WebRemoteEntity(
    @SerialName("id") val id: Uuid,
    @SerialName("detail") val detail: WebDetailRemoteEntity,
    @SerialName("isDeleted") val isDeleted: Boolean,
    @SerialName("updatedAt") val updatedAt: Instant,
    @SerialName("createdAt") val createdAt: Instant,
)
