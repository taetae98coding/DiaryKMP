package io.github.taetae98coding.diary.feature.setting.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.settingNavKeys() {
    subclass(SettingHomeNavKey::class, SettingHomeNavKey.serializer())
    subclass(SettingHolidayNavKey::class, SettingHolidayNavKey.serializer())
    subclass(SettingMapNavKey::class, SettingMapNavKey.serializer())
    subclass(SettingGeminiNavKey::class, SettingGeminiNavKey.serializer())
}
