package io.github.taetae98coding.diary.feature.routine.api

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.navigateToDetail
import kotlin.uuid.Uuid

public fun NavBackStack<ScreenNavKey>.navigateToRoutineDetail(id: Uuid) {
    navigateToDetail(
        detailKey = RoutineDetailNavKey(id = id),
        isDetailPaneKey = { key -> key is RoutineDetailNavKey || key is RoutineAddNavKey },
    )
}
