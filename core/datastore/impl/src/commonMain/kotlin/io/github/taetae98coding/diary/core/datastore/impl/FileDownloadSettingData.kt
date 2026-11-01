package io.github.taetae98coding.diary.core.datastore.impl

import kotlinx.serialization.Serializable

@Serializable
internal data class FileDownloadSettingData(
    val directoryUri: String = "",
    val directoryName: String = "",
    val directoryBookmark: String = "",
)
