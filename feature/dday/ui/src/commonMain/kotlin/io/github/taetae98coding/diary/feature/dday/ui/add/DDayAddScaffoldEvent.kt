package io.github.taetae98coding.diary.feature.dday.ui.add

internal sealed interface DDayAddScaffoldEvent {
    data object ClickNavigateUp : DDayAddScaffoldEvent

    data object ClickAdd : DDayAddScaffoldEvent
}
