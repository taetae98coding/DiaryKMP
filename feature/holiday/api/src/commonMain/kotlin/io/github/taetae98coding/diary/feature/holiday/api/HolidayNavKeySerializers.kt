package io.github.taetae98coding.diary.feature.holiday.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.holidayNavKeys() {
    subclass(HolidayHomeNavKey::class, HolidayHomeNavKey.serializer())
}
