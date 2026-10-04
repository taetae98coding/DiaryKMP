package io.github.taetae98coding.diary.feature.playlist.ui.home

import io.github.taetae98coding.diary.core.model.list.ListSort

internal data class PlaylistHomeUiState(
    val sort: ListSort = ListSort.TITLE,
)
