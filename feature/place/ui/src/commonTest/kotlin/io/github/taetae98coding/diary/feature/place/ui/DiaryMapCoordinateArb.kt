package io.github.taetae98coding.diary.feature.place.ui

import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.testing.place.coordinateInFormPrecision
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map

internal fun Arb.Companion.diaryMapCoordinateInFormPrecision(): Arb<DiaryMapCoordinate> = Arb.coordinateInFormPrecision().map(Coordinate::toDiaryMapCoordinate)
