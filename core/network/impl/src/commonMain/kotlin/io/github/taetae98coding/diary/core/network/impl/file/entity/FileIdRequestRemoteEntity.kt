package io.github.taetae98coding.diary.core.network.impl.file.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
internal data class FileIdRequestRemoteEntity(
    @SerialName("id") val id: Uuid,
)
