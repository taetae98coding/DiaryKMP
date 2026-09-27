package io.github.taetae98coding.diary.feature.file.ui.add

internal sealed interface FileAddScaffoldEvent {
    data object ClickNavigateUp : FileAddScaffoldEvent

    data object ClickUpload : FileAddScaffoldEvent

    data object ClickChooseFile : FileAddScaffoldEvent
}
