package io.github.taetae98coding.diary.core.network.api.profile.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ProfileImageRemoteEntity(
    @SerialName("profileImage") val profileImage: String,
)
