package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistAddNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.checklist.ui.add.ChecklistAddScreen
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailScreen
import io.github.taetae98coding.diary.feature.checklist.ui.home.ChecklistHomeScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

public fun EntryProviderScope<ScreenNavKey>.checklistEntry(backStack: NavBackStack<ScreenNavKey>) {
    checklistHomeEntry(backStack = backStack)
    checklistAddEntry(backStack = backStack)
    checklistDetailEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.checklistHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ChecklistHomeNavKey> {
        ChecklistHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToAdd = { backStack.add(ChecklistAddNavKey) },
            navigateToDetail = { id -> backStack.add(ChecklistDetailNavKey(id = id)) },
            checklistViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.checklistAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ChecklistAddNavKey> {
        ChecklistAddScreen(
            navigateUp = backStack::removeLastOrNull,
            viewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.checklistDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ChecklistDetailNavKey> { key ->
        ChecklistDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            checklistViewModel = koinViewModel { parametersOf(key.id) },
            itemViewModel = koinViewModel { parametersOf(key.id) },
        )
    }
}
