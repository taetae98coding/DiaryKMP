package io.github.taetae98coding.diary.feature.tag.ui.detail.contact

import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlin.uuid.Uuid

internal sealed interface TagDetailContactContentEvent {
    data class ClickContact(
        val id: Uuid,
    ) : TagDetailContactContentEvent

    data object Refresh : TagDetailContactContentEvent

    data object ClickSort : TagDetailContactContentEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagDetailContactContentEvent

    data class DeleteContact(
        val id: Uuid,
    ) : TagDetailContactContentEvent
}
