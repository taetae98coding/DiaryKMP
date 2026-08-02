package io.github.taetae98coding.diary.feature.tag.ui.detail

import io.github.taetae98coding.diary.core.model.tag.TagDetail
import kotlin.uuid.Uuid

internal sealed interface TagDetailUiState {
    data object Loading : TagDetailUiState

    data class Content(
        val id: Uuid,
        val detail: TagDetail,
        val isFinished: Boolean,
        val isInProgress: Boolean = false,
        val isFinishInProgress: Boolean = false,
        val isDeleteInProgress: Boolean = false,
    ) : TagDetailUiState
}
