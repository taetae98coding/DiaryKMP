package io.github.taetae98coding.diary.feature.web.ui.detail

import io.github.taetae98coding.diary.core.model.web.WebPage

internal sealed interface WebDetailPageUiState {
    data object Loading : WebDetailPageUiState

    data class Content(
        val page: WebPage,
    ) : WebDetailPageUiState

    data object Failure : WebDetailPageUiState
}
