package io.github.taetae98coding.diary.feature.tag.ui.detail

internal sealed interface TagDetailScaffoldEvent {
    data object ClickNavigateUp : TagDetailScaffoldEvent

    data object ClickFinish : TagDetailScaffoldEvent

    data object ClickRestart : TagDetailScaffoldEvent

    data object ClickDelete : TagDetailScaffoldEvent

    data object ClickScope : TagDetailScaffoldEvent
}
