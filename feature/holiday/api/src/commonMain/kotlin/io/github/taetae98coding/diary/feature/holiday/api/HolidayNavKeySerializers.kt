package io.github.taetae98coding.diary.feature.holiday.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.holidayNavKeys() {
    subclass(HolidayHomeNavKey::class, HolidayHomeNavKey.serializer())
}
