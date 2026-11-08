package io.github.taetae98coding.diary.domain.dday.count

import io.github.taetae98coding.diary.core.model.dday.DDayCount
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayPeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.number

private val MONTHS_PER_YEAR: Int = DateTimeUnit.YEAR.months

public fun DDayDetail.count(today: LocalDate): DDayCount =
    when {
        today < baseDate ->
            DDayCount.Upcoming(
                remainingDays = today.daysUntil(baseDate),
                period = periodBetween(from = today, to = baseDate),
            )

        today == baseDate -> DDayCount.Today

        else ->
            DDayCount.Passed(
                passedDays = baseDate.daysUntil(today) + if (countsBaseDay) 1 else 0,
                period = periodBetween(from = baseDate, to = today),
            )
    }

private fun periodBetween(
    from: LocalDate,
    to: LocalDate,
): DDayPeriod {
    val monthDifference = (to.year - from.year) * MONTHS_PER_YEAR + (to.month.number - from.month.number)
    val months = if (to.day < from.day) monthDifference - 1 else monthDifference

    return DDayPeriod(
        years = months / MONTHS_PER_YEAR,
        months = months % MONTHS_PER_YEAR,
    )
}
