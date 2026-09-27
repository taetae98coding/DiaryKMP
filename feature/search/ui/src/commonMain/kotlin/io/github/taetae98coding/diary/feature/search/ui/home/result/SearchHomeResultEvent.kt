package io.github.taetae98coding.diary.feature.search.ui.home.result

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface SearchHomeResultEvent {
    data object ClickSort : SearchHomeResultEvent

    data class SelectSort(
        val sort: ListSort,
    ) : SearchHomeResultEvent
}
