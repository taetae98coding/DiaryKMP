package io.github.taetae98coding.diary.compose.place

import io.github.taetae98coding.diary.compose.map.DiaryMapBounds
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.location.CoordinateBounds

public fun Coordinate.toDiaryMapCoordinate(): DiaryMapCoordinate = DiaryMapCoordinate(latitude = latitude, longitude = longitude)

public fun DiaryMapCoordinate.toCoordinate(): Coordinate = Coordinate(latitude = latitude, longitude = longitude)

public fun DiaryMapBounds.toCoordinateBounds(): CoordinateBounds = CoordinateBounds(south = south, north = north, west = west, east = east)
