package io.github.taetae98coding.diary.feature.playlist.ui.add

internal sealed interface MusicAddScaffoldEvent {
    data object ClickNavigateUp : MusicAddScaffoldEvent

    data object ClickAdd : MusicAddScaffoldEvent

    data object ClickFetchLink : MusicAddScaffoldEvent
}
