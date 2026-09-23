package io.github.taetae98coding.diary.feature.setting.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.Serializable

@Serializable
public data object SettingHolidayNavKey : ScreenNavKey {
    override val screenName: String get() = "SettingHoliday"
}
