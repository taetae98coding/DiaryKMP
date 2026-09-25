package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Stable
public class DiaryDateTimeInputState internal constructor(
    hasDateTime: Boolean,
    isAllDay: Boolean,
    start: LocalDateTime,
    endInclusive: LocalDateTime,
    private val clock: Clock = Clock.System,
) {
    public var hasDateTime: Boolean by mutableStateOf(hasDateTime)

    public var isAllDay: Boolean by mutableStateOf(isAllDay)
        private set

    public var start: LocalDateTime by mutableStateOf(start)
        private set

    public var endInclusive: LocalDateTime by mutableStateOf(endInclusive)
        private set

    public val value: DiaryDateTimeInputValue?
        get() =
            when {
                !hasDateTime -> null
                isAllDay -> DiaryDateTimeInputValue.AllDay(dateRange = start.date..endInclusive.date)
                else -> DiaryDateTimeInputValue.DateTime(start = start, endInclusive = endInclusive)
            }

    // 항목별 선택 함수를 거치면 한쪽을 먼저 바꾸는 순간 이전 기간과 비교한 보정이 끼어들어 값이 틀어지므로 두 끝을 한 번에 넣는다.
    public fun select(value: DiaryDateTimeInputValue) {
        when (value) {
            is DiaryDateTimeInputValue.AllDay -> {
                isAllDay = true
                start = LocalDateTime(date = value.dateRange.start, time = Midnight)
                endInclusive = LocalDateTime(date = value.dateRange.endInclusive, time = Midnight)
            }

            is DiaryDateTimeInputValue.DateTime -> {
                isAllDay = false
                start = value.start
                endInclusive = value.endInclusive
            }
        }

        coerceEndInclusive()
        hasDateTime = true
    }

    public fun selectAllDay(isAllDay: Boolean) {
        if (this.isAllDay == isAllDay) return

        this.isAllDay = isAllDay
        if (isAllDay) {
            start = LocalDateTime(date = start.date, time = Midnight)
            endInclusive = LocalDateTime(date = endInclusive.date, time = Midnight)
        } else {
            updateToDefaultPeriod()
        }
    }

    public fun selectStartDate(date: LocalDate) {
        start = LocalDateTime(date = date, time = start.time)
        coerceEndInclusive()
    }

    public fun selectStartTime(time: LocalTime) {
        start = LocalDateTime(date = start.date, time = time)
        coerceEndInclusive()
    }

    public fun selectEndDate(date: LocalDate) {
        endInclusive = LocalDateTime(date = date, time = endInclusive.time)
        coerceStart()
    }

    public fun selectEndTime(time: LocalTime) {
        endInclusive = LocalDateTime(date = endInclusive.date, time = time)
        coerceStart()
    }

    private fun updateToDefaultPeriod() {
        val defaultTime =
            clock
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .time
                .toDefaultTime()
        val endDate = endInclusive.date

        start = LocalDateTime(date = start.date, time = defaultTime)
        endInclusive =
            if (start.date == endDate) {
                start.plus(DefaultDuration)
            } else {
                LocalDateTime(date = endDate, time = defaultTime)
            }
    }

    private fun coerceEndInclusive() {
        if (start > endInclusive) {
            endInclusive = start
        }
    }

    private fun coerceStart() {
        if (endInclusive < start) {
            start = endInclusive
        }
    }

    internal companion object {
        private const val HAS_DATE_TIME_KEY = "hasDateTime"
        private const val IS_ALL_DAY_KEY = "isAllDay"
        private const val START_DATE_KEY = "startDate"
        private const val START_TIME_KEY = "startTime"
        private const val END_DATE_KEY = "endDate"
        private const val END_TIME_KEY = "endTime"

        val Saver: Saver<DiaryDateTimeInputState, Any> =
            mapSaver(
                save = { state ->
                    mapOf(
                        HAS_DATE_TIME_KEY to state.hasDateTime,
                        IS_ALL_DAY_KEY to state.isAllDay,
                        START_DATE_KEY to state.start.date.toEpochDays(),
                        START_TIME_KEY to state.start.time.toMillisecondOfDay(),
                        END_DATE_KEY to state.endInclusive.date.toEpochDays(),
                        END_TIME_KEY to state.endInclusive.time.toMillisecondOfDay(),
                    )
                },
                restore = { map ->
                    DiaryDateTimeInputState(
                        hasDateTime = checkNotNull(map[HAS_DATE_TIME_KEY]) as Boolean,
                        isAllDay = checkNotNull(map[IS_ALL_DAY_KEY]) as Boolean,
                        start =
                            LocalDateTime(
                                date = LocalDate.fromEpochDays(checkNotNull(map[START_DATE_KEY]) as Long),
                                time = LocalTime.fromMillisecondOfDay(checkNotNull(map[START_TIME_KEY]) as Int),
                            ),
                        endInclusive =
                            LocalDateTime(
                                date = LocalDate.fromEpochDays(checkNotNull(map[END_DATE_KEY]) as Long),
                                time = LocalTime.fromMillisecondOfDay(checkNotNull(map[END_TIME_KEY]) as Int),
                            ),
                    )
                },
            )
    }
}

@Composable
public fun rememberDiaryDateTimeInputState(initialValue: DiaryDateTimeInputValue? = null): DiaryDateTimeInputState =
    rememberSaveable(initialValue, saver = DiaryDateTimeInputState.Saver) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val defaultTime = now.time.toDefaultTime()

        when (initialValue) {
            null ->
                DiaryDateTimeInputState(
                    hasDateTime = false,
                    isAllDay = true,
                    start = LocalDateTime(date = now.date, time = defaultTime),
                    endInclusive = LocalDateTime(date = now.date, time = defaultTime),
                )

            is DiaryDateTimeInputValue.AllDay ->
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = true,
                    start = LocalDateTime(date = initialValue.dateRange.start, time = Midnight),
                    endInclusive = LocalDateTime(date = initialValue.dateRange.endInclusive, time = Midnight),
                )

            is DiaryDateTimeInputValue.DateTime ->
                DiaryDateTimeInputState(
                    hasDateTime = true,
                    isAllDay = false,
                    start = initialValue.start,
                    endInclusive = initialValue.endInclusive,
                )
        }
    }

internal fun LocalTime.toDefaultTime(): LocalTime {
    val unit = DefaultTimeUnit.inWholeNanoseconds
    val nanosecondOfDay = (toNanosecondOfDay() + unit - 1) / unit * unit

    return LocalTime.fromNanosecondOfDay(nanosecondOfDay % NanosecondsPerDay)
}

private fun LocalDateTime.plus(duration: Duration): LocalDateTime {
    val nanosecondOfDay = time.toNanosecondOfDay() + duration.inWholeNanoseconds

    return LocalDateTime(
        date = date.plus((nanosecondOfDay / NanosecondsPerDay).toInt(), DateTimeUnit.DAY),
        time = LocalTime.fromNanosecondOfDay(nanosecondOfDay % NanosecondsPerDay),
    )
}

private val Midnight = LocalTime(hour = 0, minute = 0)
private val DefaultTimeUnit = 30.minutes
private val DefaultDuration = 1.hours
private val NanosecondsPerDay = 1.days.inWholeNanoseconds
