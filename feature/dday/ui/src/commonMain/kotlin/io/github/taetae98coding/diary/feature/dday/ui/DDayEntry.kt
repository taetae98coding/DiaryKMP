@file:OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.dday.ui

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
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.feature.dday.api.DDayAddNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeFilterNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.dday.api.isDDayAddOnDetailPane
import io.github.taetae98coding.diary.feature.dday.api.isDDayListDetailPane
import io.github.taetae98coding.diary.feature.dday.ui.add.DDayAddScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.dday.ui.add.DDayAddScreen
import io.github.taetae98coding.diary.feature.dday.ui.detail.DDayDetailScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.dday.ui.detail.DDayDetailScreen
import io.github.taetae98coding.diary.feature.dday.ui.home.DDayHomeScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.dday.ui.home.DDayHomeScreen
import io.github.taetae98coding.diary.feature.dday.ui.home.filter.DDayHomeFilterContent
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.navigateToTagAddFromFilter
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

internal fun EntryProviderScope<ScreenNavKey>.dDayEntry(backStack: NavBackStack<ScreenNavKey>) {
    dDayHomeEntry(backStack = backStack)
    dDayHomeFilterEntry(backStack = backStack)
    dDayAddEntry(backStack = backStack)
    dDayDetailEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.dDayHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<DDayHomeNavKey>(
        clazzContentKey = { DDAY_HOME_CONTENT_KEY },
        metadata = dDayHomeListPaneMetadata(backStack = backStack),
    ) {
        val isDetailPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.Detail)

        DDayHomeScreen(
            navigateUp = backStack::navigateUpFromDDayHome,
            navigateToAdd = backStack::navigateToDDayAddFromHome,
            navigateToDetail = { id -> backStack.navigateToDDayDetail(id = id) },
            navigateToFilter = { backStack.add(DDayHomeFilterNavKey) },
            componentVisibleProvider = { DDayHomeScaffoldComponentVisible(isAddButtonVisible = !isDetailPaneVisible || !backStack.isDDayAddOnDetailPane()) },
            ddayViewModel = koinViewModel(),
            syncViewModel = koinViewModel(),
        )
    }
}

internal const val DDAY_HOME_CONTENT_KEY: String = "DDayHomeNavKey"

internal fun dDayHomeListPaneMetadata(backStack: NavBackStack<ScreenNavKey>): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = DDayHomeNavKey,
        detailPlaceholder = {
            ListDetailPlaceholderStateProvider(listContentKey = DDAY_HOME_CONTENT_KEY) {
                val tagAddRequestKey = rememberResultRequestKey()

                DDayAddScreen(
                    navigateUp = {},
                    navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
                    navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
                    tagAddRequestKey = tagAddRequestKey,
                    componentVisibleProvider = { DDayAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
                    addViewModel = koinViewModel(),
                    tagViewModel = koinViewModel { parametersOf(null) },
                )
            }
        },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

private fun EntryProviderScope<ScreenNavKey>.dDayHomeFilterEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<DDayHomeFilterNavKey>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
    ) {
        DDayHomeFilterContent(
            navigateToTagAdd = backStack::navigateToTagAddFromFilter,
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.dDayAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<DDayAddNavKey>(
        metadata = { key -> backStack.dDayListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        DDayAddScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { DDayAddScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            addViewModel = koinViewModel(),
            tagViewModel = koinViewModel { parametersOf(key.initialTagId) },
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.dDayDetailEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<DDayDetailNavKey>(
        metadata = { key -> backStack.dDayListDetailPaneMetadata(key) },
    ) { key ->
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)
        val tagAddRequestKey = rememberResultRequestKey()

        DDayDetailScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToTagAdd = { backStack.add(TagAddNavKey(requestKey = tagAddRequestKey)) },
            navigateToTagDetail = { id -> backStack.add(TagDetailNavKey(id = id)) },
            tagAddRequestKey = tagAddRequestKey,
            componentVisibleProvider = { DDayDetailScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible) },
            ddayViewModel = koinViewModel { parametersOf(key.id) },
            tagViewModel = koinViewModel { parametersOf(EntityTagKind.DDAY, key.id) },
        )
    }
}

private fun NavBackStack<ScreenNavKey>.dDayListDetailPaneMetadata(key: ScreenNavKey): Map<String, Any> =
    if (isDDayListDetailPane(key)) {
        ListDetailSceneStrategy.detailPane(sceneKey = DDayHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
    } else {
        emptyMap()
    }
