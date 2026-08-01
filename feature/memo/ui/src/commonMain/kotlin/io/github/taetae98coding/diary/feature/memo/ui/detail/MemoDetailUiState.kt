package io.github.taetae98coding.diary.feature.memo.ui.detail

import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import kotlin.uuid.Uuid

internal sealed interface MemoDetailUiState {
    data object Loading : MemoDetailUiState

    data class Content(
        val id: Uuid,
        val detail: MemoDetail,
        val isFinished: Boolean,
        val isInProgress: Boolean = false,
        val isFinishInProgress: Boolean = false,
        val isCopyInProgress: Boolean = false,
        val isDeleteInProgress: Boolean = false,
    ) : MemoDetailUiState
}
