package io.github.taetae98coding.diary.feature.file.ui.home

internal sealed interface FileHomeScaffoldEvent {
    data object ClickNavigateUp : FileHomeScaffoldEvent
}
