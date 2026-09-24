package io.github.taetae98coding.diary.core.network.api.fcm.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class FcmTokenRemoteEntity(
    @SerialName("token") val token: String,
    @SerialName("timeZone") val timeZone: String? = null,
    @SerialName("language") val language: String? = null,
)
