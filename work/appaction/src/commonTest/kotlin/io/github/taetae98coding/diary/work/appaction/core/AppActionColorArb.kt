package io.github.taetae98coding.diary.work.appaction.core

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map

private const val MAX_RGB = 0xFFFFFF
private const val HEX_RADIX = 16
private const val HEX_LENGTH = 6

// 공통 동작과 앱 동작은 색을 불투명도를 뺀 #RRGGBB 문자열로 주고받는다.
internal fun Arb.Companion.appActionColor(): Arb<String> = Arb.int(0..MAX_RGB).map { rgb -> "#" + rgb.toString(HEX_RADIX).padStart(HEX_LENGTH, '0').uppercase() }
