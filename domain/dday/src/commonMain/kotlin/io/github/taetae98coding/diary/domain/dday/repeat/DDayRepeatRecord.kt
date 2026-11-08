package io.github.taetae98coding.diary.domain.dday.repeat

import kotlinx.datetime.DateTimeUnit

private val MONTHS_PER_YEAR: Int = DateTimeUnit.YEAR.months

private val repeatInputUnitOrder: List<DDayRepeatUnit> = listOf(DDayRepeatUnit.DAY, DDayRepeatUnit.MONTH, DDayRepeatUnit.YEAR)

public fun DDayRepeatInput.isRecordable(): Boolean = number != null && number >= 1

public fun List<DDayRepeatInput>.toRepeatSetOrNull(): Set<DateTimeUnit.DateBased>? {
    if (!all { input -> input.isRecordable() }) return null

    return mapTo(mutableSetOf()) { input -> input.toRepeat(number = requireNotNull(input.number)) }
}

public fun Set<DateTimeUnit.DateBased>.toRepeatInputList(): List<DDayRepeatInput> =
    map { repeat -> repeat.toRepeatInput() }
        .sortedWith(
            compareBy<DDayRepeatInput> { input -> repeatInputUnitOrder.indexOf(input.unit) }
                .thenBy { input -> input.number },
        )

private fun DDayRepeatInput.toRepeat(number: Int): DateTimeUnit.DateBased =
    when (unit) {
        DDayRepeatUnit.DAY -> DateTimeUnit.DayBased(days = number)
        DDayRepeatUnit.MONTH -> DateTimeUnit.MonthBased(months = number)
        DDayRepeatUnit.YEAR -> DateTimeUnit.MonthBased(months = number * MONTHS_PER_YEAR)
    }

private fun DateTimeUnit.DateBased.toRepeatInput(): DDayRepeatInput =
    when (this) {
        is DateTimeUnit.DayBased -> DDayRepeatInput(number = days, unit = DDayRepeatUnit.DAY)

        is DateTimeUnit.MonthBased ->
            if (months % MONTHS_PER_YEAR == 0) {
                DDayRepeatInput(number = months / MONTHS_PER_YEAR, unit = DDayRepeatUnit.YEAR)
            } else {
                DDayRepeatInput(number = months, unit = DDayRepeatUnit.MONTH)
            }
    }
