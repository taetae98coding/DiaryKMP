package io.github.taetae98coding.diary.core.weather.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class LocationNameRemoteEntity(
    @SerialName("name") val name: String,
    @SerialName("local_names") val localNames: Map<String, String> = emptyMap(),
)
