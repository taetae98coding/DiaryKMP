package io.github.taetae98coding.diary.core.network.impl.file.entity

import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FileListResponseRemoteEntity(
    @SerialName("fileList") val fileList: List<FileRemoteEntity>,
)
