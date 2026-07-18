package io.github.taetae98coding.diary.library.kotlinx.datetime

import kotlinx.datetime.LocalDateRange

public infix fun LocalDateRange.overlaps(other: LocalDateRange): Boolean = !isEmpty() && !other.isEmpty() && start <= other.endInclusive && other.start <= endInclusive
