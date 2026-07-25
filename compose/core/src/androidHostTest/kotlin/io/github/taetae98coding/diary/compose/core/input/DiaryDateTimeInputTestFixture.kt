package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

private val DEFAULT_MONTH_NAMES =
    listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

internal object DiaryDateTimeInputTestFixture {
    const val START_DATE_TEXT = "Jul 19, 2026"
    const val START_DAY_CELL_TEXT = "July 19, 2026"
    const val END_DATE_TEXT = "Jul 20, 2026"
    const val ALL_DAY_END_DATE_TEXT = "Jul 28, 2026"
    const val ALL_DAY_END_DAY_CELL_TEXT = "July 28, 2026"
    const val PICK_DAY_CELL_TEXT = "July 25, 2026"
    const val PICKED_START_DATE_TEXT = "Jul 25, 2026"
    const val EARLIER_DAY_CELL_TEXT = "July 15, 2026"
    const val EARLIER_DATE_TEXT = "Jul 15, 2026"
    const val START_TIME_TEXT = "1:30 PM"
    const val START_HOUR_12_TEXT = "01"
    const val START_HOUR_24_TEXT = "13"
    const val START_PERIOD_TEXT = "PM"
    const val END_TIME_TEXT = "9:00 AM"
    const val END_HOUR_TEXT = "09"
    const val END_PERIOD_TEXT = "AM"
    const val PICK_HOUR_DESCRIPTION = "10 o'clock"
    const val PICKED_PM_TIME_TEXT = "10:30 PM"
    const val PICKED_AM_TIME_TEXT = "10:30 AM"
    const val PICK_EARLIER_HOUR_DESCRIPTION = "8 o'clock"
    const val PICKED_EARLIER_TIME_TEXT = "8:00 AM"
    const val KOREAN_START_DATE_TEXT = "2026. 7. 19."
    const val KOREAN_END_DATE_TEXT = "2026. 7. 20."
    const val KOREAN_START_TIME_TEXT = "오후 1:30"
    const val KOREAN_END_TIME_TEXT = "오전 9:00"
    const val KOREAN_LABEL = "날짜·시간"
    const val DEFAULT_LABEL = "Date & time"
    const val KOREAN_ALL_DAY = "종일"
    const val DEFAULT_ALL_DAY = "All day"
    const val KOREAN_START = "시작"
    const val DEFAULT_START = "Start"
    const val KOREAN_END = "종료"
    const val DEFAULT_END = "End"
    const val KOREAN_CONFIRM = "확인"
    const val DEFAULT_CONFIRM = "OK"
    const val KOREAN_CANCEL = "취소"
    const val DEFAULT_CANCEL = "Cancel"

    fun hasRole(role: Role): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

    fun LocalDate.toDefaultDisplayText(): String = "${DEFAULT_MONTH_NAMES[month.number - 1]} $day, $year"

    fun defaultTimeText(): String =
        Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .time
            .toDefaultTime()
            .toDefaultDisplayText()

    private fun LocalTime.toDefaultDisplayText(): String {
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        val period = if (hour < 12) "AM" else "PM"

        return "$displayHour:${minute.toString().padStart(2, '0')} $period"
    }

    fun allDayValue(): DiaryDateTimeInputValue.AllDay = DiaryDateTimeInputValue.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 28))

    fun dateTimeValue(): DiaryDateTimeInputValue.DateTime =
        DiaryDateTimeInputValue.DateTime(
            start = LocalDateTime(year = 2026, month = 7, day = 19, hour = 13, minute = 30),
            endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 9, minute = 0),
        )
}
