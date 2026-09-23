package io.github.taetae98coding.diary.feature.calendar.ui.home.weather

import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import kotlin.math.abs
import kotlin.math.roundToInt

private const val TEMPERATURE_DECIMAL_SCALE = 10

internal fun CalendarWeatherTemperature.toText(): String? =
    when (this) {
        is CalendarWeatherTemperature.Current -> value.toTemperatureText()
        is CalendarWeatherTemperature.MinMax -> "${min.toTemperatureText()}/${max.toTemperatureText()}"
        is CalendarWeatherTemperature.None -> null
    }

private fun Double.toTemperatureText(): String {
    val roundedTenths = (this * TEMPERATURE_DECIMAL_SCALE).roundToInt()
    val absoluteTenths = abs(roundedTenths)
    val sign = if (roundedTenths < 0) "-" else ""

    return "$sign${absoluteTenths / TEMPERATURE_DECIMAL_SCALE}.${absoluteTenths % TEMPERATURE_DECIMAL_SCALE}°"
}
