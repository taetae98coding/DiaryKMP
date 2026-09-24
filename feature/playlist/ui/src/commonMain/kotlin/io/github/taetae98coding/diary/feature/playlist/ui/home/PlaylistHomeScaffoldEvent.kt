package io.github.taetae98coding.diary.feature.playlist.ui.home

import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlin.uuid.Uuid

internal sealed interface PlaylistHomeScaffoldEvent {
    data object ClickNavigateUp : PlaylistHomeScaffoldEvent

    data object ClickAdd : PlaylistHomeScaffoldEvent

    data object ClickDownload : PlaylistHomeScaffoldEvent

    data object ClickSort : PlaylistHomeScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : PlaylistHomeScaffoldEvent

    data object Refresh : PlaylistHomeScaffoldEvent

    data class ClickMusic(
        val id: Uuid,
    ) : PlaylistHomeScaffoldEvent
}
