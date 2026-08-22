package io.github.taetae98coding.diary.core.weather.network.api.entity

import io.github.taetae98coding.diary.core.weather.network.api.serializer.EpochSecondsInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
public data class ForecastWeatherRemoteEntity(
    @SerialName("dt")
    @Serializable(with = EpochSecondsInstantSerializer::class)
    val dateTime: Instant,
    @SerialName("main") val main: WeatherMainRemoteEntity,
    @SerialName("weather") val weather: List<WeatherConditionRemoteEntity>,
)
