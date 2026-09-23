package io.github.taetae98coding.diary.core.network.impl.authentication.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class GoogleIdTokenRequestRemoteEntity(
    @SerialName("idToken") val idToken: String,
    @SerialName("nonce") val nonce: String,
)
