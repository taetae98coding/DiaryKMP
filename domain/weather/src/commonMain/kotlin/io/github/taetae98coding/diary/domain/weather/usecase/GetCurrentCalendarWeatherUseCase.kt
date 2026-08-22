package io.github.taetae98coding.diary.domain.weather.usecase

import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.model.weather.WeatherReport
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.weather.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Factory
import kotlin.time.Clock
import kotlin.time.Instant

@Factory
public class GetCurrentCalendarWeatherUseCase internal constructor(
    private val weatherRepository: WeatherRepository,
    private val clock: Clock,
) : FlowUseCase<Unit, CalendarWeatherReport>() {
    override fun execute(parameter: Unit): Flow<Result<CalendarWeatherReport>> =
        weatherRepository
            .get()
            .map { weatherReport ->
                val timeZone = TimeZone.currentSystemDefault()
                val now = clock.now()
                Result.success(
                    CalendarWeatherReport(
                        weatherList =
                            weatherReport
                                ?.toCalendarWeatherList(
                                    now = now,
                                    timeZone = timeZone,
                                ).orEmpty(),
                        locationName = weatherReport?.locationName.orEmpty(),
                    ),
                )
            }
}

private fun WeatherReport.toCalendarWeatherList(
    now: Instant,
    timeZone: TimeZone,
): List<CalendarWeather> {
    val today: LocalDate = now.toLocalDateTime(timeZone).date

    return weatherList
        .groupBy { weather -> weather.dateTime.toLocalDateTime(timeZone).date }
        .entries
        .sortedBy { entry -> entry.key }
        .map { (date, weatherList) ->
            val sortedWeatherList = weatherList.sortedBy { weather -> weather.dateTime }
            CalendarWeather(
                date = date,
                temperature =
                    when {
                        date == today -> {
                            val closestWeather =
                                sortedWeatherList.minWith(
                                    compareBy<Weather> { weather -> (weather.dateTime - now).absoluteValue }
                                        .thenBy { weather -> weather.dateTime },
                                )
                            CalendarWeatherTemperature.Current(
                                value = closestWeather.temperature.current,
                            )
                        }

                        !date.isFullyCovered(coverage = coverage, timeZone = timeZone) -> CalendarWeatherTemperature.None

                        else ->
                            CalendarWeatherTemperature.MinMax(
                                min = sortedWeatherList.minOf { weather -> weather.temperature.min },
                                max = sortedWeatherList.maxOf { weather -> weather.temperature.max },
                            )
                    },
                weatherList = sortedWeatherList,
            )
        }
}

private fun LocalDate.isFullyCovered(
    coverage: OpenEndRange<Instant>,
    timeZone: TimeZone,
): Boolean =
    coverage.start <= atStartOfDayIn(timeZone) &&
        plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone) <= coverage.endExclusive
