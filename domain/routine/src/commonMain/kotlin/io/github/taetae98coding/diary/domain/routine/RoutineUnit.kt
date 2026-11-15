package io.github.taetae98coding.diary.domain.routine

public const val ROUTINE_UNIT_MAX_LENGTH: Int = 10

private val LineBreakCharSet: Set<Char> = setOf('\n', '\r', '\u000B', '\u000C', '\u0085', ' ', ' ')

// 서버의 char_length와 같이 UTF-16 단위가 아니라 유니코드 문자 단위로 센다.
public fun String.routineUnitLength(): Int {
    var length = 0
    var index = 0

    while (index < this.length) {
        val isSurrogatePair = this[index].isHighSurrogate() && index + 1 < this.length && this[index + 1].isLowSurrogate()

        index += if (isSurrogatePair) 2 else 1
        length++
    }

    return length
}

public fun Char.isRoutineUnitLineBreak(): Boolean = this in LineBreakCharSet

public fun String.isValidRoutineUnit(): Boolean = this == trim() && none { char -> char.isRoutineUnitLineBreak() } && routineUnitLength() <= ROUTINE_UNIT_MAX_LENGTH
