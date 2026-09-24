package io.github.taetae98coding.diary.feature.contact.ui.detail.memo

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface ContactDetailMemoContentEvent {
    data object ClickSort : ContactDetailMemoContentEvent

    data class SelectSort(
        val sort: ListSort,
    ) : ContactDetailMemoContentEvent
}
