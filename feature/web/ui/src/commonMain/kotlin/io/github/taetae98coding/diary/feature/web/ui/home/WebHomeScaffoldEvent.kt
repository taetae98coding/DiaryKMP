package io.github.taetae98coding.diary.feature.web.ui.home

import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlin.uuid.Uuid

internal sealed interface WebHomeScaffoldEvent {
    data object ClickNavigateUp : WebHomeScaffoldEvent

    data object ClickSearch : WebHomeScaffoldEvent

    data object ClickAdd : WebHomeScaffoldEvent

    data object ClickSort : WebHomeScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : WebHomeScaffoldEvent

    data object Refresh : WebHomeScaffoldEvent

    data class ClickWeb(
        val id: Uuid,
    ) : WebHomeScaffoldEvent

    data class DeleteWeb(
        val id: Uuid,
    ) : WebHomeScaffoldEvent
}
