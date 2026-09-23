package io.github.taetae98coding.diary.feature.routine.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey

public fun List<ScreenNavKey>.isRoutineListDetailPane(key: ScreenNavKey): Boolean {
    val index = lastIndexOf(key)
    if (index < 0 || key != RoutineAddNavKey) return false

    return subList(0, index).lastOrNull { belowKey -> belowKey != RoutineAddNavKey } == RoutineHomeNavKey
}
