@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.routine.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.isRoutineListDetailPane
import io.github.taetae98coding.diary.feature.routine.ui.add.RoutineAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.routine.ui.add.RoutineAddScreen
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScreen
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<NavKey>.routineEntry(backStack: NavBackStack<NavKey>) {
    routineHomeEntry(backStack = backStack)
    routineAddEntry(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.routineHomeEntry(backStack: NavBackStack<NavKey>) {
    entry<RoutineHomeNavKey>(
        metadata =
            ListDetailSceneStrategy.listPane(
                sceneKey = RoutineHomeNavKey,
                detailPlaceholder = {
                    RoutineAddScreen(
                        navigateUp = {},
                        componentVisibleProvider = { RoutineAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                    )
                },
            ) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)

        RoutineHomeScreen(
            navigateToAdd = { backStack.add(RoutineAddNavKey) },
            componentVisibleProvider = { RoutineHomeScaffoldComponentVisible(isAddButtonVisible = !isDetailPaneVisible) },
            viewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<NavKey>.routineAddEntry(backStack: NavBackStack<NavKey>) {
    entry<RoutineAddNavKey>(
        metadata = { key -> backStack.routineListDetailPaneMetadata(key) },
    ) {
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        RoutineAddScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = { RoutineAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
        )
    }
}

private fun NavBackStack<NavKey>.routineListDetailPaneMetadata(key: NavKey): Map<String, Any> =
    if (isRoutineListDetailPane(key)) {
        ListDetailSceneStrategy.detailPane(sceneKey = RoutineHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f)
    } else {
        emptyMap()
    }
