package io.github.taetae98coding.diary.feature.tag.ui.detail.music

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface TagDetailMusicContentEvent {
    data object Refresh : TagDetailMusicContentEvent

    data object ClickSort : TagDetailMusicContentEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagDetailMusicContentEvent
}
