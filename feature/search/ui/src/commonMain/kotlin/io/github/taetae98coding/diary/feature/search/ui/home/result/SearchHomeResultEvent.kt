package io.github.taetae98coding.diary.feature.search.ui.home.result

import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlin.uuid.Uuid

internal sealed interface SearchHomeResultEvent {
    data class ClickResult(
        val id: Uuid,
    ) : SearchHomeResultEvent

    data object ClickSort : SearchHomeResultEvent

    data class SelectSort(
        val sort: ListSort,
    ) : SearchHomeResultEvent
}
