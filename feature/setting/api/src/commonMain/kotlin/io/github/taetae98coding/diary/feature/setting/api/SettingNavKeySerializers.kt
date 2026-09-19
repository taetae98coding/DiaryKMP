package io.github.taetae98coding.diary.feature.setting.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.settingNavKeys() {
    subclass(SettingHomeNavKey::class, SettingHomeNavKey.serializer())
    subclass(SettingHolidayNavKey::class, SettingHolidayNavKey.serializer())
    subclass(SettingMapNavKey::class, SettingMapNavKey.serializer())
    subclass(SettingGeminiNavKey::class, SettingGeminiNavKey.serializer())
}
