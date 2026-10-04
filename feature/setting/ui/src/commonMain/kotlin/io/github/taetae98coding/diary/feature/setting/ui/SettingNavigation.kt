package io.github.taetae98coding.diary.feature.setting.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingBrowserNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingDownloadNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingGeminiNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHolidayNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingMapNavKey

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
