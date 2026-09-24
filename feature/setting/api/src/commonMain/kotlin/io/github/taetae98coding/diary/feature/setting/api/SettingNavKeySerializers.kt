package io.github.taetae98coding.diary.feature.setting.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.settingNavKeys() {
    subclass(SettingHomeNavKey::class, SettingHomeNavKey.serializer())
    subclass(SettingHolidayNavKey::class, SettingHolidayNavKey.serializer())
    subclass(SettingMapNavKey::class, SettingMapNavKey.serializer())
    subclass(SettingGeminiNavKey::class, SettingGeminiNavKey.serializer())
    subclass(SettingBrowserNavKey::class, SettingBrowserNavKey.serializer())
}
