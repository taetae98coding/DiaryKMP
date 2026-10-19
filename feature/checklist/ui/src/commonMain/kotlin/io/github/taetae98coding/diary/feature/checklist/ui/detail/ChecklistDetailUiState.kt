package io.github.taetae98coding.diary.feature.checklist.ui.detail

import io.github.taetae98coding.diary.core.model.checklist.ChecklistDetail
import kotlin.uuid.Uuid

internal sealed interface ChecklistDetailUiState {
    data object Loading : ChecklistDetailUiState

    data class Content(
        val id: Uuid,
        val detail: ChecklistDetail,
        val isUpdateInProgress: Boolean = false,
        val isDeleteInProgress: Boolean = false,
    ) : ChecklistDetailUiState
}
