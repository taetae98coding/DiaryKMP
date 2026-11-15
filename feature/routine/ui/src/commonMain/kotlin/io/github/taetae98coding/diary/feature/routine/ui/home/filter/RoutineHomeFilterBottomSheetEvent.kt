package io.github.taetae98coding.diary.feature.routine.ui.home.filter

import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope

internal sealed interface RoutineHomeFilterBottomSheetEvent {
    data class SetViewScope(
        val viewScope: RoutineViewScope,
    ) : RoutineHomeFilterBottomSheetEvent
}
