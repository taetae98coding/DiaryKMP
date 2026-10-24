package io.github.taetae98coding.diary.core.network.api.checklist.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class ChecklistRemoteEntity(
    @SerialName("id") val id: Uuid,
    @SerialName("detail") val detail: ChecklistDetailRemoteEntity,
    @SerialName("isDeleted") val isDeleted: Boolean,
    @SerialName("updatedAt") val updatedAt: Instant,
    @SerialName("createdAt") val createdAt: Instant,
)
