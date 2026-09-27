package io.github.taetae98coding.diary.feature.place.ui

import androidx.compose.foundation.text.input.TextFieldState
import io.github.taetae98coding.diary.domain.place.toPlaceCoordinateOrNaN
import io.github.taetae98coding.diary.domain.place.toPlaceCoordinateText

internal fun Double.toCoordinateText(): String = toPlaceCoordinateText()

internal fun TextFieldState.decimalOrNaN(): Double = text.toString().toPlaceCoordinateOrNaN()
