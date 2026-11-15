package io.github.taetae98coding.diary.feature.routine.ui.home.viewmode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
internal class RoutineHomeViewModeState(
    initialViewMode: RoutineHomeViewMode = RoutineHomeViewMode.LIST,
) {
    var viewMode: RoutineHomeViewMode by mutableStateOf(initialViewMode)
        private set

    fun toggle() {
        viewMode = viewMode.toggled()
    }

    companion object {
        val Saver: Saver<RoutineHomeViewModeState, String> =
            Saver(
                save = { state -> state.viewMode.name },
                restore = { saved -> RoutineHomeViewModeState(initialViewMode = RoutineHomeViewMode.valueOf(saved)) },
            )
    }
}

@Composable
internal fun rememberRoutineHomeViewModeState(initialViewMode: RoutineHomeViewMode = RoutineHomeViewMode.LIST): RoutineHomeViewModeState =
    rememberSaveable(saver = RoutineHomeViewModeState.Saver) {
        RoutineHomeViewModeState(initialViewMode = initialViewMode)
    }
