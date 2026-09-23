package io.github.taetae98coding.diary.feature.tag.ui.detail.web

import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlin.uuid.Uuid

internal sealed interface TagDetailWebContentEvent {
    data class ClickWeb(
        val id: Uuid,
    ) : TagDetailWebContentEvent

    data object Refresh : TagDetailWebContentEvent

    data object ClickSort : TagDetailWebContentEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagDetailWebContentEvent
}
