package io.github.taetae98coding.diary.core.network.api.file.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
public data class FileRemoteEntity(
    @SerialName("id") val id: Uuid,
    @SerialName("name") val name: String,
    @SerialName("mimeType") val mimeType: String,
    @SerialName("size") val size: Long,
    @SerialName("createdAt") val createdAt: Instant,
)
