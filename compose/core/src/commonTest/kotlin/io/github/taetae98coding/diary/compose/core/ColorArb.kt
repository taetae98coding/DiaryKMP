package io.github.taetae98coding.diary.compose.core

import androidx.compose.ui.graphics.Color
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map

private const val OPAQUE_ALPHA = 0xFF000000.toInt()
private const val RGB_MASK = 0xFFFFFF

// 색 입력은 색을 #RRGGBB 문자열로 옮겨 적어 투명도를 버리므로, 투명한 색은 옮겨 적은 뒤 다른 값으로 읽힌다.
internal fun Arb.Companion.opaqueColor(): Arb<Color> = Arb.int().map { argb -> Color(color = OPAQUE_ALPHA or (argb and RGB_MASK)) }
