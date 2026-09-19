package io.github.taetae98coding.diary.feature.dday.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.dday.ui.home.DDayHomeScreen
import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey

public fun EntryProviderScope<ScreenNavKey>.dDayEntry(backStack: NavBackStack<ScreenNavKey>) {
    dDayHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.dDayHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<DDayHomeNavKey> {
        DDayHomeScreen(navigateUp = backStack::removeLastOrNull)
    }
}
