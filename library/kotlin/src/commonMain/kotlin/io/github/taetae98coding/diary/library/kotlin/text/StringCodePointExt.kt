package io.github.taetae98coding.diary.library.kotlin.text

// 공용 Kotlin에는 codePointCount가 없어 서로게이트 쌍을 한 글자로 묶어 직접 센다. 짝이 없는 서로게이트는 한 글자로 센다.
public fun String.codePointLength(): Int {
    var length = 0
    var index = 0

    while (index < this.length) {
        val isSurrogatePair = this[index].isHighSurrogate() && index + 1 < this.length && this[index + 1].isLowSurrogate()

        index += if (isSurrogatePair) 2 else 1
        length++
    }

    return length
}
