package io.github.taetae98coding.diary.feature.place.ui.add

internal sealed interface PlaceAddScaffoldEvent {
    data object ClickNavigateUp : PlaceAddScaffoldEvent

    data object ClickAdd : PlaceAddScaffoldEvent

    data object ClickSearch : PlaceAddScaffoldEvent
}
