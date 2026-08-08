package io.github.taetae98coding.diary.domain.holiday.usecase

public fun Int.goldenHolidaySourceYearList(): List<Int> = listOf(this - 1, this, this + 1)
