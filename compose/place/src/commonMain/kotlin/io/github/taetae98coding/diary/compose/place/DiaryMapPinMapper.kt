package io.github.taetae98coding.diary.compose.place

import io.github.taetae98coding.diary.compose.map.DiaryMapPin
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.library.compose.ui.color.toColor

public fun Place.toDiaryMapPin(): DiaryMapPin =
    DiaryMapPin(
        id = id,
        coordinate = detail.coordinate.toDiaryMapCoordinate(),
        color = detail.color.toColor(),
        label = detail.title,
    )
