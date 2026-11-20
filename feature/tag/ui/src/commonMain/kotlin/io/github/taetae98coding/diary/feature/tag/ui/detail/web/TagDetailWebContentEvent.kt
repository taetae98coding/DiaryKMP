package io.github.taetae98coding.diary.feature.tag.ui.detail.web

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface TagDetailWebContentEvent {
    data object Refresh : TagDetailWebContentEvent

    data object ClickSort : TagDetailWebContentEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagDetailWebContentEvent
}
