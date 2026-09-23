package io.github.taetae98coding.diary.feature.playlist.ui.detail

import io.github.taetae98coding.diary.core.model.playlist.MusicDetail
import kotlin.uuid.Uuid

internal sealed interface MusicDetailUiState {
    data object Loading : MusicDetailUiState

    data class Content(
        val id: Uuid,
        val detail: MusicDetail,
        val isUpdateInProgress: Boolean = false,
        val isLinkFetchInProgress: Boolean = false,
        val isDeleteInProgress: Boolean = false,
    ) : MusicDetailUiState
}
