package io.github.taetae98coding.diary.feature.routine.api

import androidx.navigation3.runtime.NavKey

public fun List<NavKey>.isRoutineListDetailPane(key: NavKey): Boolean {
    val index = lastIndexOf(key)
    if (index < 0 || key != RoutineAddNavKey) return false

    return subList(0, index).lastOrNull { belowKey -> belowKey != RoutineAddNavKey } == RoutineHomeNavKey
}
