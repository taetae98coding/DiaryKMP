package io.github.taetae98coding.diary.feature.holiday.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.feature.holiday.api.HolidayHomeNavKey
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeScreen
import io.github.taetae98coding.diary.feature.holiday.ui.home.rememberHolidayHomeScaffoldState
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey

public fun EntryProviderScope<NavKey>.holidayEntry(backStack: NavBackStack<NavKey>) {
    holidayHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.holidayHomeEntry(backStack: NavBackStack<NavKey>) {
    entry<HolidayHomeNavKey> {
        HolidayHomeScreen(
            navigateUp = backStack::removeLastOrNull,
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
            state = rememberHolidayHomeScaffoldState(),
        )
    }
}
