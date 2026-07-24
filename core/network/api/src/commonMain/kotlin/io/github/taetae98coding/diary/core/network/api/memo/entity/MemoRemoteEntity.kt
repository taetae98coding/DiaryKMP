package io.github.taetae98coding.diary.core.network.api.memo.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class MemoRemoteEntity(
    @SerialName("id") val id: Uuid,
    @SerialName("detail") val detail: MemoDetailRemoteEntity,
    @SerialName("primaryTagId") val primaryTagId: Uuid?,
    @SerialName("isFinished") val isFinished: Boolean,
    @SerialName("isDeleted") val isDeleted: Boolean,
    @SerialName("updatedAt") val updatedAt: Instant,
    @SerialName("createdAt") val createdAt: Instant,
)
