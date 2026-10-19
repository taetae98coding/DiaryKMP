package io.github.taetae98coding.diary.feature.core.memo.finished

import io.github.taetae98coding.diary.core.model.list.ListSort

public sealed interface EntityMemoFinishedListScaffoldEvent {
    public data object ClickNavigateUp : EntityMemoFinishedListScaffoldEvent

    public data object ClickSort : EntityMemoFinishedListScaffoldEvent

    public data class SelectSort(
        val sort: ListSort,
    ) : EntityMemoFinishedListScaffoldEvent
}
