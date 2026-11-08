package io.github.taetae98coding.diary.feature.dday.ui.detail

import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import kotlin.uuid.Uuid

internal sealed interface DDayDetailUiState {
    data object Loading : DDayDetailUiState

    data class Content(
        val id: Uuid,
        val detail: DDayDetail,
        val isUpdateInProgress: Boolean = false,
        val isDeleteInProgress: Boolean = false,
    ) : DDayDetailUiState
}
