package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.checklist.ui.home.ChecklistHomeScreen

public fun EntryProviderScope<ScreenNavKey>.checklistEntry(backStack: NavBackStack<ScreenNavKey>) {
    checklistHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.checklistHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ChecklistHomeNavKey> {
        ChecklistHomeScreen(navigateUp = backStack::removeLastOrNull)
    }
}
