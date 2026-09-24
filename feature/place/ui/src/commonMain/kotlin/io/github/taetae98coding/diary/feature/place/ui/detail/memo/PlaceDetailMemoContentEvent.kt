package io.github.taetae98coding.diary.feature.place.ui.detail.memo

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface PlaceDetailMemoContentEvent {
    data object ClickSort : PlaceDetailMemoContentEvent

    data class SelectSort(
        val sort: ListSort,
    ) : PlaceDetailMemoContentEvent
}
