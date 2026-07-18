package io.github.taetae98coding.diary.core.model.weather

public sealed interface CalendarWeatherTemperature {
    public data class Current(
        val value: Double,
    ) : CalendarWeatherTemperature

    public data class MinMax(
        val min: Double,
        val max: Double,
    ) : CalendarWeatherTemperature

    public data object None : CalendarWeatherTemperature
}
