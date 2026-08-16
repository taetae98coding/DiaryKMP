package io.github.taetae98coding.diary.feature.search.ui.home

internal sealed interface SearchHomeScaffoldEvent {
    data object ClickNavigateUp : SearchHomeScaffoldEvent
}
