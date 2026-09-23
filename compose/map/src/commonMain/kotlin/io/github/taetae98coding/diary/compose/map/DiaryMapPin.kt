package io.github.taetae98coding.diary.compose.map

import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.map.provider.label
import kotlin.uuid.Uuid

public data class DiaryMapPin(
    val id: Uuid,
    val coordinate: DiaryMapCoordinate,
    val color: Color,
    val label: String,
)

internal val DiaryMapPin.isFinite: Boolean
    get() = coordinate.isFinite
