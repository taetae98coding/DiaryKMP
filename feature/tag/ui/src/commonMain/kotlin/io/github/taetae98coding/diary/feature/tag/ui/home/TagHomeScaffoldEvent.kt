package io.github.taetae98coding.diary.feature.tag.ui.home

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface TagHomeScaffoldEvent {
    data object ClickAdd : TagHomeScaffoldEvent

    data object ClickFilter : TagHomeScaffoldEvent

    data object ClickFinishedList : TagHomeScaffoldEvent

    data object ClickSearch : TagHomeScaffoldEvent

    data object ClickSort : TagHomeScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagHomeScaffoldEvent

    data object Refresh : TagHomeScaffoldEvent
}
