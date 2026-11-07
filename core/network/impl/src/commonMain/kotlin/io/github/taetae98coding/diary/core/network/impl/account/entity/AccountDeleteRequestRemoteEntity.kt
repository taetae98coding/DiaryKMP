package io.github.taetae98coding.diary.core.network.impl.account.entity

import io.github.taetae98coding.diary.core.network.api.account.entity.AppleAuthorizationRemoteEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AccountDeleteRequestRemoteEntity(
    @SerialName("apple") val apple: AppleAuthorizationRemoteEntity?,
)
