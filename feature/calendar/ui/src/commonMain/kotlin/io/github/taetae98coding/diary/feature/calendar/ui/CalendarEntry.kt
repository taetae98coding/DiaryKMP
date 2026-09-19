@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.calendar.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeFilterNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeScreen
import io.github.taetae98coding.diary.feature.calendar.ui.home.filter.CalendarHomeFilterContent
import io.github.taetae98coding.diary.feature.calendar.ui.home.rememberCalendarHomeScaffoldState
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<NavKey>.calendarEntry(backStack: NavBackStack<NavKey>) {
    calendarHomeEntry(backStack = backStack)
    calendarHomeFilterEntry()
}

private fun EntryProviderScope<NavKey>.calendarHomeEntry(backStack: NavBackStack<NavKey>) {
    entry<CalendarHomeNavKey> {
        CalendarHomeScreen(
            navigateToMemoDetail = { id -> backStack.add(MemoDetailNavKey(id)) },
            navigateToMemoAdd = { dateRange ->
                backStack.add(
                    MemoAddNavKey(
                        initialDateRange =
                            MemoAddNavKey.InitialDateRange(
                                start = dateRange.start,
                                endInclusive = dateRange.endInclusive,
                            ),
                    ),
                )
            },
            navigateToContactDetail = { contactId -> backStack.add(ContactDetailNavKey(contactId)) },
            navigateToFilter = { backStack.add(CalendarHomeFilterNavKey) },
            state = rememberCalendarHomeScaffoldState(),
            permissionManager = rememberPermissionManager(),
            holidayViewModel = koinViewModel(),
            memoViewModel = koinViewModel(),
            birthdayViewModel = koinViewModel(),
            weatherViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<NavKey>.calendarHomeFilterEntry() {
    entry<CalendarHomeFilterNavKey>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
    ) {
        CalendarHomeFilterContent()
    }
}
