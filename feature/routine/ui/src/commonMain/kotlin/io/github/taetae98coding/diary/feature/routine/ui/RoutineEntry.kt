@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.routine.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.result.rememberResultRequestKey
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.ListDetailPlaceholderStateProvider
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.compose.routine.FinishedRoutineListDetailPlaceholder
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineDetailNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeFilterNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.api.findRoutineDetailPaneListKey
import io.github.taetae98coding.diary.feature.routine.api.isRoutineAddOnDetailPane
import io.github.taetae98coding.diary.feature.routine.api.navigateToRoutineDetail
import io.github.taetae98coding.diary.feature.routine.ui.add.RoutineAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.routine.ui.add.RoutineAddScreen
import io.github.taetae98coding.diary.feature.routine.ui.detail.RoutineDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.routine.ui.detail.RoutineDetailScreen
import io.github.taetae98coding.diary.feature.routine.ui.finished.RoutineFinishedListScreen
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.routine.ui.home.RoutineHomeScreen
import io.github.taetae98coding.diary.feature.routine.ui.home.filter.RoutineHomeFilterContent
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.navigateToTagAddFromFilter
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun EntryProviderScope<ScreenNavKey>.routineEntry(
    backStack: NavBackStack<ScreenNavKey>,
    homeReselectEvent: Flow<Unit>,
) {
    routineHomeEntry(
        backStack = backStack,
        homeReselectEvent = homeReselectEvent,
    )
    routineHomeFilterEntry(backStack = backStack)
    routineFinishedListEntry(backStack = backStack)
    routineAddEntry(backStack = backStack)
    routineDetailEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.routineHomeEntry(
    backStack: NavBackStack<ScreenNavKey>,
    homeReselectEvent: Flow<Unit>,
) {
    entry<RoutineHomeNavKey>(
        clazzContentKey = { ROUTINE_HOME_CONTENT_KEY },
        metadata = routineHomeListPaneMetadata(backStack = backStack),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)

        RoutineHomeScreen(
            navigateToAdd = backStack::navigateToRoutineAddFromHome,
            navigateToDetail = { id -> backStack.navigateToRoutineDetail(id = id) },
            navigateToFilter = { backStack.add(RoutineHomeFilterNavKey) },
            navigateToFinishedList = { backStack.add(RoutineFinishedListNavKey) },
            componentVisibleProvider = { RoutineHomeScaffoldComponentVisible(isAddButtonVisible = !isDetailPaneVisible || !backStack.isRoutineAddOnDetailPane()) },
            homeReselectEvent = homeReselectEvent,
            routineViewModel = koinViewModel(),
            calendarViewModel = koinViewModel(),
            holidayViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

internal const val ROUTINE_HOME_CONTENT_KEY: String = "RoutineHomeNavKey"

internal fun routineHomeListPaneMetadata(backStack: NavBackStack<ScreenNavKey>): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = RoutineHomeNavKey,
        detailPlaceholder = {
            ListDetailPlaceholderStateProvider(listContentKey = ROUTINE_HOME_CONTENT_KEY) {
                val tagAddRequestKey = rememberResultRequestKey()

                RoutineAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
                    navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { RoutineAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                    addViewModel = koinViewModel(),
                    tagViewModel = koinViewModel { parametersOf(null) },
                )
            }
        },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

private fun EntryProviderScope<ScreenNavKey>.routineHomeFilterEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<RoutineHomeFilterNavKey>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
    ) {
        RoutineHomeFilterContent(
            navigateToTagAdd = backStack::navigateToTagAddFromFilter,
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.routineFinishedListEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<RoutineFinishedListNavKey>(
        metadata = routineFinishedListPaneMetadata(),
    ) {
        RoutineFinishedListScreen(
            navigateUp = backStack::navigateUpFromRoutineFinishedList,
            navigateToDetail = { id -> backStack.navigateToRoutineDetail(id = id) },
            routineViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

internal fun routineFinishedListPaneMetadata(): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = RoutineFinishedListNavKey,
        detailPlaceholder = { FinishedRoutineListDetailPlaceholder() },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

private fun EntryProviderScope<ScreenNavKey>.routineAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<RoutineAddNavKey>(
        metadata = { key -> backStack.routineListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        RoutineAddScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { RoutineAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            addViewModel = koinViewModel(),
            tagViewModel = koinViewModel { parametersOf(key.initialTagId) },
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.routineDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<RoutineDetailNavKey>(
        metadata = { key -> backStack.routineListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        RoutineDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { RoutineDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            routineViewModel = koinViewModel { parametersOf(key.id) },
            recordViewModel = koinViewModel { parametersOf(key.id) },
            tagViewModel = koinViewModel { parametersOf(EntityTagKind.ROUTINE, key.id) },
        )
    }
}

private fun NavBackStack<ScreenNavKey>.routineListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> {
    val sceneKey = routineDetailPaneSceneKey(key) ?: return emptyMap()

    return ListDetailSceneStrategy.detailPane(sceneKey = sceneKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
}

internal fun List<ScreenNavKey>.routineDetailPaneSceneKey(key: ScreenNavKey): ScreenNavKey? =
    findRoutineDetailPaneListKey(key) { belowKey ->
        belowKey == RoutineHomeNavKey ||
            belowKey == RoutineFinishedListNavKey ||
            belowKey is TagRoutineFinishedListNavKey
    }
