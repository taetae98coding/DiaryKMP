package io.github.taetae98coding.diary.feature.holiday.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.holidayNavKeys() {
    subclass(HolidayHomeNavKey::class, HolidayHomeNavKey.serializer())
}
