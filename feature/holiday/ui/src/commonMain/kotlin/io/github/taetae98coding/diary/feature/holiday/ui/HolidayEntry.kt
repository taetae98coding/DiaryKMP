package io.github.taetae98coding.diary.feature.holiday.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.feature.holiday.api.HolidayHomeNavKey
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeScreen
import io.github.taetae98coding.diary.feature.holiday.ui.home.rememberHolidayHomeScaffoldState
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey

public fun EntryProviderScope<ScreenNavKey>.holidayEntry(backStack: NavBackStack<ScreenNavKey>) {
    holidayHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.holidayHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
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
