package io.github.taetae98coding.diary.feature.memo.ui.home

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface MemoHomeScaffoldEvent {
    data object ClickAdd : MemoHomeScaffoldEvent

    data object ClickFilter : MemoHomeScaffoldEvent

    data object ClickFinishedList : MemoHomeScaffoldEvent

    data object ClickSearch : MemoHomeScaffoldEvent

    data object ClickSort : MemoHomeScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : MemoHomeScaffoldEvent
}
