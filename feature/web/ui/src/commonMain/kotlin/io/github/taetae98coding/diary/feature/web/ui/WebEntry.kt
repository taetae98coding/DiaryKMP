@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.web.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.result.rememberResultRequestKey
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.isWebListDetailPane
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.web.ui.add.WebAddScreen
import io.github.taetae98coding.diary.feature.web.ui.detail.WebDetailScreen
import io.github.taetae98coding.diary.feature.web.ui.home.WebHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.web.ui.home.WebHomeScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

public fun EntryProviderScope<ScreenNavKey>.webEntry(backStack: NavBackStack<ScreenNavKey>) {
    webHomeEntry(backStack = backStack)
    webAddEntry(backStack = backStack)
    webDetailEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.webHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<WebHomeNavKey>(
        metadata =
            ListDetailSceneStrategy.listPane(
                sceneKey = WebHomeNavKey,
                detailPlaceholder = {
                    val tagAddRequestKey = rememberResultRequestKey()

                    WebAddScreen(
                        navigateUp = {},
                        navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
                        navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
                        tagAddRequestKey = tagAddRequestKey,
                        componentVisibleProvider = { WebAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                        addViewModel = koinViewModel(),
                        tagViewModel = koinViewModel { parametersOf(null) },
                    )
                },
            ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)

        WebHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToSearch = { backStack.add(SearchHomeNavKey(initialType = SearchHomeType.WEB)) },
            navigateToAdd = { backStack.add(WebAddNavKey()) },
            navigateToDetail = { id -> backStack.add(WebDetailNavKey(id = id)) },
            componentVisibleProvider = { WebHomeScaffoldComponentVisible(isAddButtonVisible = !isDetailPaneVisible) },
            webViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.webAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<WebAddNavKey>(
        metadata = { key -> backStack.webListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        WebAddScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { WebAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            addViewModel = koinViewModel(),
            tagViewModel = koinViewModel { parametersOf(key.initialTagId) },
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.webDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<WebDetailNavKey> { key ->
        val tagAddRequestKey = rememberResultRequestKey()

        WebDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            webViewModel = koinViewModel { parametersOf(key.id) },
            pageViewModel = koinViewModel { parametersOf(key.id) },
            tagViewModel = koinViewModel { parametersOf(key.id) },
        )
    }
}

private fun NavBackStack<ScreenNavKey>.webListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> =
    if (isWebListDetailPane(key)) {
        ListDetailSceneStrategy.detailPane(sceneKey = WebHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
    } else {
        emptyMap()
    }
