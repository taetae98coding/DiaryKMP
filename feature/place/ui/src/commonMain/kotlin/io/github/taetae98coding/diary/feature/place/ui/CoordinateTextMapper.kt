package io.github.taetae98coding.diary.feature.place.ui

import androidx.compose.foundation.text.input.TextFieldState
import io.github.taetae98coding.diary.core.model.place.toPlaceCoordinateOrNaN
import io.github.taetae98coding.diary.core.model.place.toPlaceCoordinateText

internal fun Double.toCoordinateText(): String = toPlaceCoordinateText()

internal fun TextFieldState.decimalOrNaN(): Double = text.toString().toPlaceCoordinateOrNaN()
