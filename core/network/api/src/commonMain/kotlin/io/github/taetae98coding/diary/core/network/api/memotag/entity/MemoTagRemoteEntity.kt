package io.github.taetae98coding.diary.core.network.api.memotag.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class MemoTagRemoteEntity(
    @SerialName("memoId") val memoId: Uuid,
    @SerialName("tagId") val tagId: Uuid,
    @SerialName("isDeleted") val isDeleted: Boolean,
    @SerialName("updatedAt") val updatedAt: Instant,
    @SerialName("createdAt") val createdAt: Instant,
)
