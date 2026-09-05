package io.github.taetae98coding.diary.feature.playlist.ui.home

internal sealed interface PlaylistHomeScaffoldEvent {
    data object ClickNavigateUp : PlaylistHomeScaffoldEvent
}
