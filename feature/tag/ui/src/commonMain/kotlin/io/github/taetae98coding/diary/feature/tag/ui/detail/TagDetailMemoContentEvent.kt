package io.github.taetae98coding.diary.feature.tag.ui.detail

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface TagDetailMemoContentEvent {
    data object ClickFinishedList : TagDetailMemoContentEvent

    data object ClickSort : TagDetailMemoContentEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagDetailMemoContentEvent
}
