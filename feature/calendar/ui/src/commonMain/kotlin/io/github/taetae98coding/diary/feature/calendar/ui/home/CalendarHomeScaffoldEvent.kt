package io.github.taetae98coding.diary.feature.calendar.ui.home

import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.feature.calendar.ui.home.birthday.toDateRange
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.toDateRange
import kotlinx.datetime.LocalDateRange
import kotlin.uuid.Uuid

internal sealed interface CalendarHomeScaffoldEvent {
    data object Refresh : CalendarHomeScaffoldEvent

    data object ClickFilter : CalendarHomeScaffoldEvent

    data class ClickMemo(
        val id: Uuid,
    ) : CalendarHomeScaffoldEvent

    data class ClickContactBirthday(
        val contactId: Uuid,
    ) : CalendarHomeScaffoldEvent

    data class MoveMemo(
        val id: Uuid,
        val fromDateTime: MemoDateTime,
        val toDateRange: LocalDateRange,
    ) : CalendarHomeScaffoldEvent

    data class SelectDate(
        val dateRange: LocalDateRange,
    ) : CalendarHomeScaffoldEvent
}
