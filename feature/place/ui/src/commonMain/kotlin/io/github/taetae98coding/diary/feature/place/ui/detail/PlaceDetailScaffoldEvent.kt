package io.github.taetae98coding.diary.feature.place.ui.detail

internal sealed interface PlaceDetailScaffoldEvent {
    data object ClickNavigateUp : PlaceDetailScaffoldEvent

    data object ClickUpdate : PlaceDetailScaffoldEvent

    data object ClickDelete : PlaceDetailScaffoldEvent

    data object ClickSearch : PlaceDetailScaffoldEvent

    data object ClickOpenExternalMap : PlaceDetailScaffoldEvent
}
