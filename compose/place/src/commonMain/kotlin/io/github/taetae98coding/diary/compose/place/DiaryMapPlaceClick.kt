package io.github.taetae98coding.diary.compose.place

import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.isCentered
import io.github.taetae98coding.diary.core.model.location.Coordinate

public fun DiaryMapState.moveToOrSelect(
    coordinate: Coordinate,
    onSelect: () -> Unit,
) {
    val target = coordinate.toDiaryMapCoordinate()

    if (isCentered(target)) {
        onSelect()
    } else {
        moveTo(target)
    }
}
