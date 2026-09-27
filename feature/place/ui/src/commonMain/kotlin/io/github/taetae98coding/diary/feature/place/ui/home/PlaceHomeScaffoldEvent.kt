package io.github.taetae98coding.diary.feature.place.ui.home

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import kotlin.uuid.Uuid

internal sealed interface PlaceHomeScaffoldEvent {
    data object ClickNavigateUp : PlaceHomeScaffoldEvent

    data object ClickSearch : PlaceHomeScaffoldEvent

    data class ClickAdd(
        val coordinate: Coordinate?,
    ) : PlaceHomeScaffoldEvent

    data class ClickPlace(
        val id: Uuid,
    ) : PlaceHomeScaffoldEvent

    data class DeletePlace(
        val id: Uuid,
    ) : PlaceHomeScaffoldEvent

    data class MoveMap(
        val bounds: CoordinateBounds?,
    ) : PlaceHomeScaffoldEvent

    data object Refresh : PlaceHomeScaffoldEvent

    data object ClickSort : PlaceHomeScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : PlaceHomeScaffoldEvent
}
