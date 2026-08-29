package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.checklist.ui.home.ChecklistHomeScreen

public fun EntryProviderScope<NavKey>.checklistEntry(backStack: NavBackStack<NavKey>) {
    checklistHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.checklistHomeEntry(backStack: NavBackStack<NavKey>) {
    entry<ChecklistHomeNavKey> {
        ChecklistHomeScreen(navigateUp = backStack::removeLastOrNull)
    }
}
