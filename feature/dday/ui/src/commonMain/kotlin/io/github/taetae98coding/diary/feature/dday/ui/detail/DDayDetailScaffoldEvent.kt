package io.github.taetae98coding.diary.feature.dday.ui.detail

internal sealed interface DDayDetailScaffoldEvent {
    data object ClickNavigateUp : DDayDetailScaffoldEvent

    data object ClickDelete : DDayDetailScaffoldEvent

    data object ClickUpdate : DDayDetailScaffoldEvent
}
