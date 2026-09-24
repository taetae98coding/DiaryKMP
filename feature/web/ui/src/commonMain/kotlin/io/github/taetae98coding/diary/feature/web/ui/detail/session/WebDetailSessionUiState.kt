package io.github.taetae98coding.diary.feature.web.ui.detail.session

internal sealed interface WebDetailSessionUiState {
    data object Preparing : WebDetailSessionUiState

    data class Prepared(
        val url: String,
    ) : WebDetailSessionUiState
}

internal fun WebDetailSessionUiState.isPrepared(url: String): Boolean = this is WebDetailSessionUiState.Prepared && this.url == url
