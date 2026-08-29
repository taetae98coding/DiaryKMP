package io.github.taetae98coding.diary.feature.dday.ui.home

internal sealed interface DDayHomeScaffoldEvent {
    data object ClickNavigateUp : DDayHomeScaffoldEvent
}
