package io.github.taetae98coding.diary.feature.tag.ui.finished

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface TagFinishedListScaffoldEvent {
    data object ClickNavigateUp : TagFinishedListScaffoldEvent

    data object ClickSort : TagFinishedListScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagFinishedListScaffoldEvent

    data object Refresh : TagFinishedListScaffoldEvent
}
