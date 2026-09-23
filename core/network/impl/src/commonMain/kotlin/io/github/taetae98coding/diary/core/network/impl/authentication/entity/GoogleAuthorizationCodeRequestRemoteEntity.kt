package io.github.taetae98coding.diary.core.network.impl.authentication.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GoogleAuthorizationCodeRequestRemoteEntity(
    @SerialName("authorizationCode") val authorizationCode: String,
    @SerialName("clientId") val clientId: String,
    @SerialName("redirectUri") val redirectUri: String,
    @SerialName("codeVerifier") val codeVerifier: String? = null,
)
