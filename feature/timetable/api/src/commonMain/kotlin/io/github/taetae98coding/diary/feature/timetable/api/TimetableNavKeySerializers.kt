package io.github.taetae98coding.diary.feature.timetable.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

public fun PolymorphicModuleBuilder<ScreenNavKey>.timetableNavKeys() {
    subclass(CalendarTimetableNavKey::class, CalendarTimetableNavKey.serializer())
}
