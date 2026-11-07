package io.github.taetae98coding.diary.core.network.api.account.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AppleAuthorizationRemoteEntity(
    @SerialName("authorizationCode") val authorizationCode: String,
    @SerialName("clientId") val clientId: String,
)
