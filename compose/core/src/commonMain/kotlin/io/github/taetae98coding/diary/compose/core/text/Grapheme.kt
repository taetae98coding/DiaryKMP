package io.github.taetae98coding.diary.compose.core.text

private const val ZERO_WIDTH_JOINER = 0x200D
private const val VARIATION_SELECTOR_15 = 0xFE0E
private const val VARIATION_SELECTOR_16 = 0xFE0F
private const val SUPPLEMENTARY_PLANE_START = 0x10000
private const val HIGH_SURROGATE_START = 0xD800
private const val LOW_SURROGATE_START = 0xDC00
private const val SURROGATE_SHIFT = 10

private val RegionalIndicatorRange = 0x1F1E6..0x1F1FF
private val SkinToneModifierRange = 0x1F3FB..0x1F3FF
private val TagRange = 0xE0020..0xE007F

private val CombiningCategorySet =
    setOf(
        CharCategory.NON_SPACING_MARK,
        CharCategory.ENCLOSING_MARK,
        CharCategory.COMBINING_SPACING_MARK,
    )

/**
 * 이모지는 이어붙인 코드가 모여 하나로 보이므로 코드 단위나 코드 포인트로 자르면 조각난 문자가 남는다.
 */
public fun CharSequence.takeLastGrapheme(): CharSequence {
    if (isEmpty()) return ""

    var startIndex = 0
    var endIndex = graphemeEndIndex(startIndex = startIndex)

    while (endIndex < length) {
        startIndex = endIndex
        endIndex = graphemeEndIndex(startIndex = startIndex)
    }

    return subSequence(startIndex, endIndex)
}

private fun CharSequence.graphemeEndIndex(startIndex: Int): Int {
    if (codePointAt(index = startIndex) in RegionalIndicatorRange) return regionalIndicatorEndIndex(startIndex = startIndex)

    var index = nextCodePointIndex(index = startIndex)
    var extendedIndex = extendedEndIndexOrNull(index = index)

    while (extendedIndex != null) {
        index = extendedIndex
        extendedIndex = extendedEndIndexOrNull(index = index)
    }

    return index
}

/** 국기 이모지는 지역 표시 문자 두 개가 모여 하나로 보인다. */
private fun CharSequence.regionalIndicatorEndIndex(startIndex: Int): Int {
    val index = nextCodePointIndex(index = startIndex)
    val hasSecondIndicator = index < length && codePointAt(index = index) in RegionalIndicatorRange

    return if (hasSecondIndicator) nextCodePointIndex(index = index) else index
}

private fun CharSequence.extendedEndIndexOrNull(index: Int): Int? {
    if (index >= length) return null

    val codePoint = codePointAt(index = index)

    return when {
        codePoint == ZERO_WIDTH_JOINER -> {
            val joinedIndex = nextCodePointIndex(index = index)

            if (joinedIndex < length) nextCodePointIndex(index = joinedIndex) else null
        }

        codePoint.isGraphemeExtend() -> nextCodePointIndex(index = index)

        else -> null
    }
}

private fun Int.isGraphemeExtend(): Boolean =
    this == VARIATION_SELECTOR_15 ||
        this == VARIATION_SELECTOR_16 ||
        this in SkinToneModifierRange ||
        this in TagRange ||
        isCombiningMark()

private fun Int.isCombiningMark(): Boolean = this <= Char.MAX_VALUE.code && toChar().category in CombiningCategorySet

private fun CharSequence.codePointAt(index: Int): Int {
    val high = this[index]
    val low = if (high.isHighSurrogate() && index + 1 < length) this[index + 1] else null

    return if (low != null && low.isLowSurrogate()) {
        val highOffset = high.code - HIGH_SURROGATE_START
        val lowOffset = low.code - LOW_SURROGATE_START

        SUPPLEMENTARY_PLANE_START + (highOffset shl SURROGATE_SHIFT) + lowOffset
    } else {
        high.code
    }
}

private fun CharSequence.nextCodePointIndex(index: Int): Int = index + if (codePointAt(index = index) >= SUPPLEMENTARY_PLANE_START) 2 else 1
