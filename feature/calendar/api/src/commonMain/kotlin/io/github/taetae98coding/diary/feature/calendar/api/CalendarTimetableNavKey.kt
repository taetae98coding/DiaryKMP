package io.github.taetae98coding.diary.feature.calendar.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CalendarTimetableNavKey(
    val type: Type,
    val date: LocalDate,
) : ScreenNavKey {
    override val screenName: String get() = "CalendarTimetable"

    @Serializable
    public enum class Type {
        @SerialName("day")
        DAY,

        @SerialName("week")
        WEEK,
    }
}
