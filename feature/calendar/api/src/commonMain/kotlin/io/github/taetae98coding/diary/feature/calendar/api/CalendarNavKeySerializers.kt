package io.github.taetae98coding.diary.feature.calendar.api

import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.calendarNavKeys() {
    subclass(CalendarHomeNavKey::class, CalendarHomeNavKey.serializer())
    subclass(CalendarHomeFilterNavKey::class, CalendarHomeFilterNavKey.serializer())
}
