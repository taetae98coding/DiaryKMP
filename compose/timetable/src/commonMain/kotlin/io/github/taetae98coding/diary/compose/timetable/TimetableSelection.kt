package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus

internal const val SELECT_SLOT_MINUTES: Int = 30
internal const val SELECT_SLOTS_PER_DAY: Int = MINUTES_PER_DAY / SELECT_SLOT_MINUTES

internal data class TimetableTimeSlot(
    val date: LocalDate,
    val index: Int,
) : Comparable<TimetableTimeSlot> {
    override fun compareTo(other: TimetableTimeSlot): Int = compareValuesBy(this, other, { it.date }, { it.index })
}

internal fun timetableTimeSlotAt(
    date: LocalDate,
    minuteOfDay: Int,
): TimetableTimeSlot = TimetableTimeSlot(date = date, index = (minuteOfDay / SELECT_SLOT_MINUTES).coerceIn(0, SELECT_SLOTS_PER_DAY - 1))

internal sealed interface TimetableSelection {
    fun toEvent(): TimetableEvent

    data class Time(
        val anchor: TimetableTimeSlot,
        val current: TimetableTimeSlot,
    ) : TimetableSelection {
        val first: TimetableTimeSlot get() = minOf(anchor, current)
        val last: TimetableTimeSlot get() = maxOf(anchor, current)

        fun slotIndexRangeOn(date: LocalDate): IntRange? {
            if (date < first.date || date > last.date) return null

            val start = if (date == first.date) first.index else 0
            val endInclusive = if (date == last.date) last.index else SELECT_SLOTS_PER_DAY - 1

            return start..endInclusive
        }

        override fun toEvent(): TimetableEvent.SelectTime =
            TimetableEvent.SelectTime(
                start = LocalDateTime(date = first.date, time = timeOfMinute(first.index * SELECT_SLOT_MINUTES)),
                endInclusive = LocalDateTime(date = last.date, time = timeOfMinute((last.index + 1) * SELECT_SLOT_MINUTES)),
            )
    }

    data class Date(
        val anchor: LocalDate,
        val current: LocalDate,
    ) : TimetableSelection {
        val dateRange: LocalDateRange get() = minOf(anchor, current)..maxOf(anchor, current)

        override fun toEvent(): TimetableEvent.SelectDate = TimetableEvent.SelectDate(dateRange = dateRange)
    }
}

internal fun interface TimetableSelectionResolver {
    fun resolve(
        position: Offset,
        size: IntSize,
        anchor: TimetableSelection?,
    ): TimetableSelection?
}

@Stable
internal class TimetableSelectState {
    var selection: TimetableSelection? by mutableStateOf(null)
        private set

    fun select(selection: TimetableSelection) {
        this.selection = selection
    }

    fun clear() {
        selection = null
    }
}

private fun timeOfMinute(minuteOfDay: Int): LocalTime =
    if (minuteOfDay >= MINUTES_PER_DAY) {
        LocalTime(hour = HOURS_PER_DAY - 1, minute = MINUTES_PER_HOUR - 1)
    } else {
        LocalTime(hour = minuteOfDay / MINUTES_PER_HOUR, minute = minuteOfDay % MINUTES_PER_HOUR)
    }

internal fun LocalDateRange.dateSelectionAt(
    position: Offset,
    size: IntSize,
    startInset: Float,
    anchor: TimetableSelection?,
): TimetableSelection? {
    if (anchor == null && position.x < startInset) return null

    val date = dateAt(x = position.x - startInset, width = size.width - startInset)

    return TimetableSelection.Date(anchor = (anchor as? TimetableSelection.Date)?.anchor ?: date, current = date)
}

internal fun LocalDateRange.timeSelectionAt(
    position: Offset,
    size: IntSize,
    visibleTop: Float,
    visibleBottom: Float,
    anchor: TimetableSelection?,
): TimetableSelection {
    val y = position.y.coerceIn(visibleTop, visibleBottom)
    val minuteOfDay = (y / size.height * MINUTES_PER_DAY).toInt()
    val slot = timetableTimeSlotAt(date = dateAt(x = position.x, width = size.width.toFloat()), minuteOfDay = minuteOfDay)

    return TimetableSelection.Time(anchor = (anchor as? TimetableSelection.Time)?.anchor ?: slot, current = slot)
}

private fun LocalDateRange.dateAt(
    x: Float,
    width: Float,
): LocalDate {
    val dayCount = count()
    val index = (x / (width / dayCount)).toInt().coerceIn(0, dayCount - 1)

    return start.plus(index, DateTimeUnit.DAY)
}
