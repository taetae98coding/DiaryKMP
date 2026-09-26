package io.github.taetae98coding.diary.feature.calendar.ui

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.weather.CalendarWeather
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.core.model.weather.Weather
import io.github.taetae98coding.diary.core.model.weather.WeatherCondition
import io.github.taetae98coding.diary.core.model.weather.WeatherTemperature
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal fun previewWeatherCondition(): WeatherCondition = WeatherCondition(description = "맑음", imageUrl = "")

internal fun previewCalendarWeather(date: LocalDate = LocalDate(year = 2026, month = 7, day = 19)): CalendarWeather =
    CalendarWeather(
        date = date,
        temperature = CalendarWeatherTemperature.MinMax(min = 21.3, max = 29.8),
        weatherList =
            listOf(
                Weather(
                    dateTime = Instant.DISTANT_PAST,
                    conditionList = listOf(previewWeatherCondition()),
                    temperature = WeatherTemperature(current = 25.0, min = 21.3, max = 29.8),
                ),
            ),
    )

internal fun previewCalendarMemo(title: String = "메모 제목"): CalendarMemo =
    CalendarMemo(
        id = Uuid.random(),
        title = title,
        color = 0xFF3A7BD5,
        dateTime =
            MemoDateTime.AllDay(
                dateRange = LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 20),
            ),
    )

internal fun previewCalendarTimedMemo(title: String = "회의"): CalendarMemo =
    CalendarMemo(
        id = Uuid.random(),
        title = title,
        color = 0xFFE67E22,
        dateTime =
            MemoDateTime.DateTime(
                start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 9, minute = 0),
                endInclusive = LocalDateTime(year = 2026, month = 7, day = 19, hour = 10, minute = 30),
            ),
    )

internal fun previewCalendarContactBirthday(name: String = "홍길동"): CalendarContactBirthday =
    CalendarContactBirthday(
        contactId = Uuid.random(),
        name = name,
        date = LocalDate(year = 2026, month = 7, day = 22),
    )

internal fun previewTag(
    emoji: String,
    title: String,
    color: Long,
): Tag =
    Tag(
        id = Uuid.random(),
        detail = TagDetail(emoji = emoji, title = title, description = "", color = color),
        isFinished = false,
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal class TagListPreviewParameter : PreviewParameterProvider<List<Tag>> {
    override val values: Sequence<List<Tag>> =
        sequenceOf(
            emptyList(),
            listOf(
                previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5),
                previewTag(emoji = "🏃", title = "운동", color = 0xFFE57373),
                previewTag(emoji = "", title = "공부", color = 0xFF81C784),
            ),
        )
}
