package io.github.taetae98coding.diary.core.network.api.checklistitem.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class ChecklistItemRemoteEntity(
    @SerialName("id") val id: Uuid,
    @SerialName("checklistId") val checklistId: Uuid,
    @SerialName("content") val content: String,
    @SerialName("isChecked") val isChecked: Boolean,
    @SerialName("isDeleted") val isDeleted: Boolean,
    @SerialName("updatedAt") val updatedAt: Instant,
    @SerialName("createdAt") val createdAt: Instant,
)
