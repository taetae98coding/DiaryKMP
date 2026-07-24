package io.github.taetae98coding.diary.core.network.api.place.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PlaceDetailRemoteEntity(
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("color") val color: Long,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("address") val address: String,
)
