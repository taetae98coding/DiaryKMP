package io.github.taetae98coding.diary.feature.timetable.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.compose.core.lifecycle.dropUnlessResumedWithArg
import io.github.taetae98coding.diary.core.model.memo.MemoDateTime
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey

internal fun EntryProviderScope<ScreenNavKey>.timetableEntry(backStack: NavBackStack<ScreenNavKey>) {
    calendarTimetableEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.calendarTimetableEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<CalendarTimetableNavKey> { key ->
        CalendarTimetableScreen(
            navigateUp = { backStack.removeLastOrNull() },
            navigateToMemoDetail = dropUnlessResumedWithArg { id -> backStack.add(MemoDetailNavKey(id)) },
            navigateToDDayDetail = dropUnlessResumedWithArg { id -> backStack.add(DDayDetailNavKey(id = id)) },
            navigateToMemoAdd = dropUnlessResumedWithArg { dateTime -> backStack.add(dateTime.toMemoAddNavKey()) },
            navigateToContactDetail = dropUnlessResumedWithArg { contactId -> backStack.add(ContactDetailNavKey(contactId)) },
            state = rememberCalendarTimetableScaffoldState(type = key.type, initialDate = key.date),
            memoViewModel = metroViewModel(),
            holidayViewModel = metroViewModel(),
            weatherViewModel = metroViewModel(),
            ddayViewModel = metroViewModel(),
            birthdayViewModel = metroViewModel(),
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
