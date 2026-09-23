package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds
import kotlin.uuid.Uuid

internal sealed interface TagDetailPlaceContentEvent {
    data class ClickPlace(
        val id: Uuid,
    ) : TagDetailPlaceContentEvent

    data class MoveMap(
        val bounds: CoordinateBounds?,
        val coordinate: Coordinate?,
    ) : TagDetailPlaceContentEvent

    data object Refresh : TagDetailPlaceContentEvent

    data object ClickSort : TagDetailPlaceContentEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagDetailPlaceContentEvent
}
