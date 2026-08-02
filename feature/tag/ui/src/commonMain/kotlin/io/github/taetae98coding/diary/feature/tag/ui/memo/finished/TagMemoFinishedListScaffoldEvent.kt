package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface TagMemoFinishedListScaffoldEvent {
    data object ClickNavigateUp : TagMemoFinishedListScaffoldEvent

    data object ClickSort : TagMemoFinishedListScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagMemoFinishedListScaffoldEvent
}
