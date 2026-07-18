package io.github.taetae98coding.diary.library.kotlinx.datetime

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

public const val DAYS_PER_WEEK: Int = 7

public val DayOfWeek.sundayBasedNumber: Int
    get() = sundayBasedDayOfWeekList.indexOf(this)

public fun sundayBasedDayOfWeek(number: Int): DayOfWeek = sundayBasedDayOfWeekList[number % DAYS_PER_WEEK]

public fun LocalDate.sundayOfWeek(): LocalDate = minus(dayOfWeek.sundayBasedNumber, DateTimeUnit.DAY)

private val sundayBasedDayOfWeekList =
    listOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
    )
