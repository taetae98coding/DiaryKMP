package io.github.taetae98coding.diary.feature.calendar.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<NavKey>.calendarNavKeys() {
    subclass(CalendarHomeNavKey::class, CalendarHomeNavKey.serializer())
    subclass(CalendarHomeFilterNavKey::class, CalendarHomeFilterNavKey.serializer())
}
