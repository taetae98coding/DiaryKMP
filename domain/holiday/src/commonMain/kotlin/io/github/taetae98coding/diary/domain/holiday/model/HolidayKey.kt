package io.github.taetae98coding.diary.domain.holiday.model

public fun String.toHolidayKey(): String = filterNot { character -> character.isWhitespace() }
