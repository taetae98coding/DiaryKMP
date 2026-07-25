package io.github.taetae98coding.diary.core.ipnetwork.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class IpRemoteEntity(
    @SerialName("lat") val latitude: Double,
    @SerialName("lon") val longitude: Double,
)
