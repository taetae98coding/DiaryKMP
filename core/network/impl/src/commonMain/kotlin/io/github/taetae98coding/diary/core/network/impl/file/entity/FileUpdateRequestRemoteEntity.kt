package io.github.taetae98coding.diary.core.network.impl.file.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class FileUpdateRequestRemoteEntity(
    @SerialName("id") val id: Uuid,
    @SerialName("name") val name: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
)
