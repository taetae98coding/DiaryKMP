package io.github.taetae98coding.diary.feature.setting.ui.home

import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_home_browser_item_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_home_gemini_item_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_home_holiday_item_label
import io.github.taetae98coding.diary.feature.setting.ui.setting_home_map_item_label
import org.jetbrains.compose.resources.StringResource

internal enum class SettingHomeItem(
    val labelResource: StringResource,
) {
    HOLIDAY(Res.string.setting_home_holiday_item_label),
    MAP(Res.string.setting_home_map_item_label),
    GEMINI(Res.string.setting_home_gemini_item_label),
    BROWSER(Res.string.setting_home_browser_item_label),
}
