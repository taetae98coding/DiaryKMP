package io.github.taetae98coding.diary.feature.file.ui.detail

internal sealed interface FileDetailScaffoldEvent {
    data object ClickNavigateUp : FileDetailScaffoldEvent

    data object ClickRetry : FileDetailScaffoldEvent

    data object ClickUpdate : FileDetailScaffoldEvent

    data object ClickDownload : FileDetailScaffoldEvent
}
