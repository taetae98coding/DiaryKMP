package io.github.taetae98coding.diary.core.network.impl.integrity.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PlayIntegrityDecodeRequestRemoteEntity(
    @SerialName("token") val token: String,
    @SerialName("packageName") val packageName: String,
)
