package io.github.taetae98coding.diary.core.datastore.impl

import kotlinx.serialization.Serializable

@Serializable
internal data class MusicDownloadProxySettingData(
    val address: String = "",
)
