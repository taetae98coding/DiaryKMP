package io.github.taetae98coding.diary.feature.playlist.ui.home

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface PlaylistHomeScaffoldEvent {
    data object ClickNavigateUp : PlaylistHomeScaffoldEvent

    data object ClickAdd : PlaylistHomeScaffoldEvent

    data object ClickSort : PlaylistHomeScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : PlaylistHomeScaffoldEvent

    data object Refresh : PlaylistHomeScaffoldEvent
}
