package io.github.taetae98coding.diary.feature.routine.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.navigateUpTo
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineFinishedListNavKey

internal fun NavBackStack<ScreenNavKey>.navigateToRoutineAddFromHome() {
    add(RoutineAddNavKey())
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromRoutineFinishedList() {
    navigateUpTo(homeKey = RoutineFinishedListNavKey)
}
