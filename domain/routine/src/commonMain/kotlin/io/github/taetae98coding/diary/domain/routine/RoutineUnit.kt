package io.github.taetae98coding.diary.domain.routine

import io.github.taetae98coding.diary.library.kotlin.text.codePointLength

public const val ROUTINE_UNIT_MAX_LENGTH: Int = 10

private val LineBreakCharSet: Set<Char> = setOf('\n', '\r', '\u000B', '\u000C', '\u0085', ' ', ' ')

// 서버의 char_length와 같이 UTF-16 단위가 아니라 유니코드 문자 단위로 센다.
public fun String.routineUnitLength(): Int = codePointLength()

public fun Char.isRoutineUnitLineBreak(): Boolean = this in LineBreakCharSet

public fun String.isValidRoutineUnit(): Boolean = this == trim() && none { char -> char.isRoutineUnitLineBreak() } && routineUnitLength() <= ROUTINE_UNIT_MAX_LENGTH
