@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.checklist.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.result.rememberResultRequestKey
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.ListDetailPlaceholderStateProvider
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistAddNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistDetailNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.checklist.api.isChecklistAddOnDetailPane
import io.github.taetae98coding.diary.feature.checklist.api.isChecklistListDetailPane
import io.github.taetae98coding.diary.feature.checklist.ui.add.ChecklistAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.checklist.ui.add.ChecklistAddScreen
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.checklist.ui.detail.ChecklistDetailScreen
import io.github.taetae98coding.diary.feature.checklist.ui.home.ChecklistHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.checklist.ui.home.ChecklistHomeScreen
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun EntryProviderScope<ScreenNavKey>.checklistEntry(backStack: NavBackStack<ScreenNavKey>) {
    checklistHomeEntry(backStack = backStack)
    checklistAddEntry(backStack = backStack)
    checklistDetailEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.checklistHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ChecklistHomeNavKey>(
        clazzContentKey = { CHECKLIST_HOME_CONTENT_KEY },
        metadata = checklistHomeListPaneMetadata(backStack = backStack),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)

        ChecklistHomeScreen(
            navigateUp = backStack::navigateUpFromChecklistHome,
            navigateToAdd = backStack::navigateToChecklistAddFromHome,
            navigateToDetail = { id -> backStack.navigateToChecklistDetail(id = id) },
            componentVisibleProvider = { ChecklistHomeScaffoldComponentVisible(isAddButtonVisible = !isDetailPaneVisible || !backStack.isChecklistAddOnDetailPane()) },
            checklistViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

internal const val CHECKLIST_HOME_CONTENT_KEY: String = "ChecklistHomeNavKey"

internal fun checklistHomeListPaneMetadata(backStack: NavBackStack<ScreenNavKey>): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = ChecklistHomeNavKey,
        detailPlaceholder = {
            ListDetailPlaceholderStateProvider(listContentKey = CHECKLIST_HOME_CONTENT_KEY) {
                val tagAddRequestKey = rememberResultRequestKey()

                ChecklistAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
                    navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { ChecklistAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                    addViewModel = koinViewModel(),
                    tagViewModel = koinViewModel { parametersOf(null) },
                )
            }
        },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

private fun EntryProviderScope<ScreenNavKey>.checklistAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ChecklistAddNavKey>(
        metadata = { key -> backStack.checklistListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        ChecklistAddScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { ChecklistAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            addViewModel = koinViewModel(),
            tagViewModel = koinViewModel { parametersOf(key.initialTagId) },
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.checklistDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ChecklistDetailNavKey>(
        metadata = { key -> backStack.checklistListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        ChecklistDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { ChecklistDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            checklistViewModel = koinViewModel { parametersOf(key.id) },
            itemViewModel = koinViewModel { parametersOf(key.id) },
            tagViewModel = koinViewModel { parametersOf(EntityTagKind.CHECKLIST, key.id) },
        )
    }
}

private fun NavBackStack<ScreenNavKey>.checklistListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> =
    if (isChecklistListDetailPane(key)) {
        ListDetailSceneStrategy.detailPane(sceneKey = ChecklistHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
    } else {
        emptyMap()
    }
