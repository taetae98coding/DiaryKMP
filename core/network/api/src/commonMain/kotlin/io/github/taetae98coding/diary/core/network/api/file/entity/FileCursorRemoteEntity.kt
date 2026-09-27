package io.github.taetae98coding.diary.core.network.api.file.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class FileCursorRemoteEntity(
    @SerialName("createdAt") val createdAt: Instant,
    @SerialName("id") val id: Uuid,
)
