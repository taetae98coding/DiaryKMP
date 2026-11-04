package io.github.taetae98coding.diary.feature.timetable.api

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import kotlin.uuid.Uuid

public fun NavBackStack<ScreenNavKey>.openTimetable(navKey: CalendarTimetableNavKey) {
    val openedKey = navKey.copy(id = Uuid.random())

    if (lastOrNull() is CalendarTimetableNavKey) {
        this[lastIndex] = openedKey
    } else {
        add(openedKey)
    }
}
