package io.github.taetae98coding.diary.feature.timetable.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<ScreenNavKey>.timetableEntry(backStack: NavBackStack<ScreenNavKey>) {
    calendarTimetableEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.calendarTimetableEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<CalendarTimetableNavKey> { key ->
        CalendarTimetableScreen(
            navigateUp = { backStack.removeLastOrNull() },
            navigateToMemoDetail = { id -> backStack.add(MemoDetailNavKey(id)) },
            navigateToMemoAdd = { dateTime -> backStack.add(dateTime.toMemoAddNavKey()) },
            navigateToContactDetail = { contactId -> backStack.add(ContactDetailNavKey(contactId)) },
            state = rememberCalendarTimetableScaffoldState(type = key.type, initialDate = key.date),
            memoViewModel = koinViewModel(),
            holidayViewModel = koinViewModel(),
            weatherViewModel = koinViewModel(),
            birthdayViewModel = koinViewModel(),
        )
    }
}

private fun MemoDateTime.toMemoAddNavKey(): MemoAddNavKey =
    when (this) {
        is MemoDateTime.AllDay ->
            MemoAddNavKey(
                initialDateRange = MemoAddNavKey.InitialDateRange(start = dateRange.start, endInclusive = dateRange.endInclusive),
            )

        is MemoDateTime.DateTime ->
            MemoAddNavKey(
                initialDateTimeRange = MemoAddNavKey.InitialDateTimeRange(start = start, endInclusive = endInclusive),
            )
    }
