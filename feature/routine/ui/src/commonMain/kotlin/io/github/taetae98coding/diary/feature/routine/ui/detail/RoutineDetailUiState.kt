package io.github.taetae98coding.diary.feature.routine.ui.detail

import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import kotlin.uuid.Uuid

internal sealed interface RoutineDetailUiState {
    data object Loading : RoutineDetailUiState

    data class Content(
        val id: Uuid,
        val detail: RoutineDetail,
        val isFinished: Boolean,
        val isUpdateInProgress: Boolean = false,
        val isFinishInProgress: Boolean = false,
        val isDeleteInProgress: Boolean = false,
    ) : RoutineDetailUiState
}
