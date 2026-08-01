package io.github.taetae98coding.diary.feature.memo.ui.finished

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface MemoFinishedListScaffoldEvent {
    data object ClickNavigateUp : MemoFinishedListScaffoldEvent

    data object ClickSort : MemoFinishedListScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : MemoFinishedListScaffoldEvent
}
