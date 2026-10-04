package io.github.taetae98coding.diary.feature.core.memo

import io.github.taetae98coding.diary.core.model.list.ListSort

public sealed interface EntityDetailMemoTabEvent {
    public data object ClickSort : EntityDetailMemoTabEvent

    public data class SelectSort(
        val sort: ListSort,
    ) : EntityDetailMemoTabEvent
}
