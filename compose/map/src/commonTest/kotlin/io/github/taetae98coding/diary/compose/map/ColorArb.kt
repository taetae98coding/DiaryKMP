package io.github.taetae98coding.diary.compose.map

import androidx.compose.ui.graphics.Color
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map

internal fun Arb.Companion.color(): Arb<Color> = Arb.int().map { argb -> Color(color = argb) }
