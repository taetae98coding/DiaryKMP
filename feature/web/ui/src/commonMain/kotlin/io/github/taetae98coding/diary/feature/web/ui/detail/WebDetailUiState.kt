package io.github.taetae98coding.diary.feature.web.ui.detail

import io.github.taetae98coding.diary.core.model.web.WebDetail
import kotlin.uuid.Uuid

internal sealed interface WebDetailUiState {
    data object Loading : WebDetailUiState

    data class Content(
        val id: Uuid,
        val detail: WebDetail,
        val isUpdateInProgress: Boolean = false,
        val isDeleteInProgress: Boolean = false,
    ) : WebDetailUiState
}

internal fun WebDetailUiState.urlOrEmpty(): String = (this as? WebDetailUiState.Content)?.detail?.url.orEmpty()
