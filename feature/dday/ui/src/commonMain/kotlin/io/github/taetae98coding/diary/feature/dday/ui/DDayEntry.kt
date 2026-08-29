package io.github.taetae98coding.diary.feature.dday.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.dday.ui.home.DDayHomeScreen

public fun EntryProviderScope<NavKey>.dDayEntry(backStack: NavBackStack<NavKey>) {
    dDayHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.dDayHomeEntry(backStack: NavBackStack<NavKey>) {
    entry<DDayHomeNavKey> {
        DDayHomeScreen(navigateUp = backStack::removeLastOrNull)
    }
}
