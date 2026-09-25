package io.github.taetae98coding.diary.core.network.impl.file.entity

import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FileListRequestRemoteEntity(
    @SerialName("cursor") val cursor: FileCursorRemoteEntity?,
    @SerialName("size") val size: Int,
)
