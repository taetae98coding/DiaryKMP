package io.github.taetae98coding.diary.core.network.impl.file.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FileDownloadResponseRemoteEntity(
    @SerialName("url") val url: String,
)
