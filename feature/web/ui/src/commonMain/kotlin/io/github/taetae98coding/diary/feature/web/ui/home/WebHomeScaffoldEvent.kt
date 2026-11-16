package io.github.taetae98coding.diary.feature.web.ui.home

import io.github.taetae98coding.diary.core.model.list.ListSort

internal sealed interface WebHomeScaffoldEvent {
    data object ClickNavigateUp : WebHomeScaffoldEvent

    data object ClickSearch : WebHomeScaffoldEvent

    data object ClickAdd : WebHomeScaffoldEvent

    data object ClickSort : WebHomeScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : WebHomeScaffoldEvent

    data object Refresh : WebHomeScaffoldEvent
}
