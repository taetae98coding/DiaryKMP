@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.setting.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingBrowserNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingDownloadNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingGeminiNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHolidayNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingMapNavKey
import io.github.taetae98coding.diary.feature.setting.ui.browser.SettingBrowserScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.setting.ui.browser.SettingBrowserScreen
import io.github.taetae98coding.diary.feature.setting.ui.download.SettingDownloadScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.setting.ui.download.SettingDownloadScreen
import io.github.taetae98coding.diary.feature.setting.ui.gemini.SettingGeminiScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.setting.ui.gemini.SettingGeminiScreen
import io.github.taetae98coding.diary.feature.setting.ui.holiday.SettingHolidayScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.setting.ui.holiday.SettingHolidayScreen
import io.github.taetae98coding.diary.feature.setting.ui.home.SettingHomeScreen
import io.github.taetae98coding.diary.feature.setting.ui.map.SettingMapScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.setting.ui.map.SettingMapScreen
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<ScreenNavKey>.settingEntry(backStack: NavBackStack<ScreenNavKey>) {
    settingHomeEntry(backStack = backStack)
    settingHolidayEntry(backStack = backStack)
    settingMapEntry(backStack = backStack)
    settingGeminiEntry(backStack = backStack)
    settingBrowserEntry(backStack = backStack)
    settingDownloadEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.settingHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<SettingHomeNavKey>(
        metadata =
            ListDetailSceneStrategy.listPane(
                sceneKey = SettingHomeNavKey,
                detailPlaceholder = { SettingDetailPlaceholder() },
            ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION),
    ) {
        SettingHomeScreen(
            navigateUp = backStack::navigateUpFromSettingHome,
            navigateToHoliday = { backStack.navigateToSettingDetail(SettingHolidayNavKey) },
            navigateToMap = { backStack.navigateToSettingDetail(SettingMapNavKey) },
            navigateToGemini = { backStack.navigateToSettingDetail(SettingGeminiNavKey) },
            navigateToBrowser = { backStack.navigateToSettingDetail(SettingBrowserNavKey) },
            navigateToDownload = { backStack.navigateToSettingDetail(SettingDownloadNavKey) },
            viewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.settingHolidayEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<SettingHolidayNavKey>(
        metadata = ListDetailSceneStrategy.detailPane(sceneKey = SettingHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION),
    ) {
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        SettingHolidayScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = {
                SettingHolidayScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible)
            },
            viewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.settingMapEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<SettingMapNavKey>(
        metadata = ListDetailSceneStrategy.detailPane(sceneKey = SettingHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION),
    ) {
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        SettingMapScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = {
                SettingMapScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible)
            },
            viewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.settingGeminiEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<SettingGeminiNavKey>(
        metadata = ListDetailSceneStrategy.detailPane(sceneKey = SettingHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION),
    ) {
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        SettingGeminiScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = {
                SettingGeminiScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible)
            },
            settingViewModel = koinViewModel(),
            modelViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.settingBrowserEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<SettingBrowserNavKey>(
        metadata = ListDetailSceneStrategy.detailPane(sceneKey = SettingHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION),
    ) {
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        SettingBrowserScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = {
                SettingBrowserScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible)
            },
            viewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.settingDownloadEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<SettingDownloadNavKey>(
        metadata = ListDetailSceneStrategy.detailPane(sceneKey = SettingHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION),
    ) {
        val isListPaneVisible = isPaneVisible(role = ListDetailPaneScaffoldRole.List)

        SettingDownloadScreen(
            navigateUp = backStack::removeLastOrNull,
            componentVisibleProvider = {
                SettingDownloadScaffoldComponentVisible(isNavigateUpButtonVisible = !isListPaneVisible)
            },
            viewModel = koinViewModel(),
        )
    }
}

private val settingDetailNavKeySet: Set<ScreenNavKey> =
    setOf(SettingHolidayNavKey, SettingMapNavKey, SettingGeminiNavKey, SettingBrowserNavKey, SettingDownloadNavKey)

internal fun NavBackStack<ScreenNavKey>.navigateToSettingDetail(destination: ScreenNavKey) {
    require(destination in settingDetailNavKeySet)
    if (lastOrNull() == destination) return

    if (lastOrNull() in settingDetailNavKeySet) {
        removeLastOrNull()
    }

    add(destination)
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromSettingHome() {
    val settingHomeIndex = indexOfLast { key -> key == SettingHomeNavKey }
    if (settingHomeIndex < 0) return

    repeat(size - settingHomeIndex) {
        removeLastOrNull()
    }
}
