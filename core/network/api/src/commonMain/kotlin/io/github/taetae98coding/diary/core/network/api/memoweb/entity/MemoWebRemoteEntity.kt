package io.github.taetae98coding.diary.core.network.api.memoweb.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class MemoWebRemoteEntity(
    @SerialName("memoId") val memoId: Uuid,
    @SerialName("webId") val webId: Uuid,
    @SerialName("isDeleted") val isDeleted: Boolean,
    @SerialName("updatedAt") val updatedAt: Instant,
    @SerialName("createdAt") val createdAt: Instant,
)
