package io.github.taetae98coding.diary.feature.tag.ui.detail.music

import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlin.uuid.Uuid

internal sealed interface TagDetailMusicContentEvent {
    data class ClickMusic(
        val id: Uuid,
    ) : TagDetailMusicContentEvent

    data object Refresh : TagDetailMusicContentEvent

    data object ClickSort : TagDetailMusicContentEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagDetailMusicContentEvent

    data class DeleteMusic(
        val id: Uuid,
    ) : TagDetailMusicContentEvent
}
