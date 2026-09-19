@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.setting.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.scene.isPaneVisible
import io.github.taetae98coding.diary.feature.setting.api.SettingGeminiNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHolidayNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingMapNavKey
import io.github.taetae98coding.diary.feature.setting.ui.gemini.SettingGeminiScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.setting.ui.gemini.SettingGeminiScreen
import io.github.taetae98coding.diary.feature.setting.ui.holiday.SettingHolidayScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.setting.ui.holiday.SettingHolidayScreen
import io.github.taetae98coding.diary.feature.setting.ui.home.SettingHomeScreen
import io.github.taetae98coding.diary.feature.setting.ui.map.SettingMapScaffoldComponentVisible
import io.github.taetae98coding.diary.feature.setting.ui.map.SettingMapScreen
import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<ScreenNavKey>.settingEntry(backStack: NavBackStack<ScreenNavKey>) {
    settingHomeEntry(backStack = backStack)
    settingHolidayEntry(backStack = backStack)
    settingMapEntry(backStack = backStack)
    settingGeminiEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.settingHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<SettingHomeNavKey>(
        metadata =
            ListDetailSceneStrategy.listPane(
                sceneKey = SettingHomeNavKey,
                detailPlaceholder = { SettingDetailPlaceholder() },
            ) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f),
    ) {
        SettingHomeScreen(
            navigateUp = backStack::navigateUpFromSettingHome,
            navigateToHoliday = { backStack.navigateToSettingDetail(SettingHolidayNavKey) },
            navigateToMap = { backStack.navigateToSettingDetail(SettingMapNavKey) },
            navigateToGemini = { backStack.navigateToSettingDetail(SettingGeminiNavKey) },
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.settingHolidayEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<SettingHolidayNavKey>(
        metadata = ListDetailSceneStrategy.detailPane(sceneKey = SettingHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f),
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
        metadata = ListDetailSceneStrategy.detailPane(sceneKey = SettingHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f),
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
        metadata = ListDetailSceneStrategy.detailPane(sceneKey = SettingHomeNavKey) + ListDetailSceneStrategy.preferredPaneSize(width = 0.5f),
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

internal fun NavBackStack<ScreenNavKey>.navigateToSettingDetail(destination: ScreenNavKey) {
    require(destination == SettingHolidayNavKey || destination == SettingMapNavKey || destination == SettingGeminiNavKey)
    if (lastOrNull() == destination) return

    when (lastOrNull()) {
        SettingHolidayNavKey, SettingMapNavKey, SettingGeminiNavKey -> removeLastOrNull()
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
