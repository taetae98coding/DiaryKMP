@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.calendar.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.compose.core.lifecycle.dropUnlessResumedWithArg
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeFilterNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeScreen
import io.github.taetae98coding.diary.feature.calendar.ui.home.ScrollToTodayOnReselectEffect
import io.github.taetae98coding.diary.feature.calendar.ui.home.filter.CalendarHomeFilterContent
import io.github.taetae98coding.diary.feature.calendar.ui.home.rememberCalendarHomeScaffoldState
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.navigateToTagAddFromFilter
import kotlinx.coroutines.flow.Flow

internal fun EntryProviderScope<ScreenNavKey>.calendarEntry(
    backStack: NavBackStack<ScreenNavKey>,
    homeReselectEvent: Flow<Unit>,
) {
    calendarHomeEntry(
        backStack = backStack,
        homeReselectEvent = homeReselectEvent,
    )
    calendarHomeFilterEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.calendarHomeEntry(
    backStack: NavBackStack<ScreenNavKey>,
    homeReselectEvent: Flow<Unit>,
) {
    entry<CalendarHomeNavKey> {
        val state = rememberCalendarHomeScaffoldState()

        ScrollToTodayOnReselectEffect(
            reselectEvent = homeReselectEvent,
            state = state,
        )
        CalendarHomeScreen(
            navigateToMemoDetail = dropUnlessResumedWithArg { id -> backStack.add(MemoDetailNavKey(id)) },
            navigateToDDayDetail = dropUnlessResumedWithArg { id -> backStack.add(DDayDetailNavKey(id = id)) },
            navigateToMemoAdd =
                dropUnlessResumedWithArg { dateRange ->
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
            navigateToContactDetail = dropUnlessResumedWithArg { contactId -> backStack.add(ContactDetailNavKey(contactId)) },
            navigateToFilter = dropUnlessResumed { backStack.add(CalendarHomeFilterNavKey) },
            navigateToTimetable = dropUnlessResumedWithArg { key -> backStack.add(key) },
            state = state,
            permissionManager = rememberPermissionManager(),
            holidayViewModel = metroViewModel(),
            memoViewModel = metroViewModel(),
            ddayViewModel = metroViewModel(),
            birthdayViewModel = metroViewModel(),
            filterViewModel = metroViewModel(),
            weatherViewModel = metroViewModel(),
            syncViewModel = metroViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.calendarHomeFilterEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<CalendarHomeFilterNavKey>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
    ) {
        CalendarHomeFilterContent(
            navigateToTagAdd = backStack::navigateToTagAddFromFilter,
        )
    }
}
