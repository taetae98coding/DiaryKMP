package io.github.taetae98coding.diary.feature.tag.ui.detail.contact

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface TagDetailContactContentEvent {
    data object Refresh : TagDetailContactContentEvent

    data object ClickSort : TagDetailContactContentEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagDetailContactContentEvent
}
