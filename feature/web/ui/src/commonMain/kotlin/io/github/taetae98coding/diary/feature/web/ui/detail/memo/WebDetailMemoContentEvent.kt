package io.github.taetae98coding.diary.feature.web.ui.detail.memo

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface WebDetailMemoContentEvent {
    data object ClickAdd : WebDetailMemoContentEvent

    data object ClickSort : WebDetailMemoContentEvent

    data class SelectSort(
        val sort: ListSort,
    ) : WebDetailMemoContentEvent
}
