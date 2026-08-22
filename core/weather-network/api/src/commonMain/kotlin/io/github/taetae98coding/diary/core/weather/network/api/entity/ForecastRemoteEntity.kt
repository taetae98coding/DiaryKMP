package io.github.taetae98coding.diary.core.weather.network.api.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ForecastRemoteEntity(
    @SerialName("list") val list: List<ForecastWeatherRemoteEntity>,
)
