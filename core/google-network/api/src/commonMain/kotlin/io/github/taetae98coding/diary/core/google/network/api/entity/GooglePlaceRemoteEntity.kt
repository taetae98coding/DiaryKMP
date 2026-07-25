package io.github.taetae98coding.diary.core.google.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GooglePlaceRemoteEntity(
    @SerialName("id") val id: String,
    @SerialName("displayName") val displayName: GooglePlaceDisplayNameRemoteEntity,
    @SerialName("location") val location: GooglePlaceLocationRemoteEntity,
    @SerialName("types") val types: List<String> = emptyList(),
    @SerialName("formattedAddress") val formattedAddress: String = "",
    @SerialName("shortFormattedAddress") val shortFormattedAddress: String = "",
    @SerialName("googleMapsUri") val googleMapsUri: String = "",
)
