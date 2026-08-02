package io.github.taetae98coding.diary.feature.tag.ui.home

import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlin.uuid.Uuid

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

    data class ClickTag(
        val id: Uuid,
    ) : TagHomeScaffoldEvent
}
