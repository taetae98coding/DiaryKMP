package io.github.taetae98coding.diary.feature.file.ui.home

import kotlin.uuid.Uuid

internal sealed interface FileHomeScaffoldEvent {
    data object ClickNavigateUp : FileHomeScaffoldEvent

    data object ClickAdd : FileHomeScaffoldEvent

    data object ClickRetry : FileHomeScaffoldEvent

    data object Refresh : FileHomeScaffoldEvent

    data class ClickFile(
        val id: Uuid,
    ) : FileHomeScaffoldEvent
}
