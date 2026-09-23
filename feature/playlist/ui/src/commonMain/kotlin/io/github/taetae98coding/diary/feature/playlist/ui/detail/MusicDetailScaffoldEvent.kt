package io.github.taetae98coding.diary.feature.playlist.ui.detail

internal sealed interface MusicDetailScaffoldEvent {
    data object ClickNavigateUp : MusicDetailScaffoldEvent

    data object ClickUpdate : MusicDetailScaffoldEvent

    data object ClickFetchLink : MusicDetailScaffoldEvent

    data object ClickOpenInNew : MusicDetailScaffoldEvent

    data object ClickDelete : MusicDetailScaffoldEvent
}
