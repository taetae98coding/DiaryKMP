package io.github.taetae98coding.diary.work.appaction.dday

import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatInputList
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument
import kotlinx.datetime.DateTimeUnit

private const val REPEATS = "repeats"

private val MONTHS_PER_YEAR: Int = DateTimeUnit.YEAR.months

// 숫자 자리는 기기 로캘의 숫자가 아닌 ASCII 숫자만 받도록 [0-9]로 적는다.
private val repeatPattern: Regex = Regex("P(?:([0-9]+)Y)?(?:([0-9]+)M)?(?:([0-9]+)D)?")

// 데스크톱 MCP 연결도 이 규칙으로 반복 간격을 읽고 적어 두 경로의 결과가 같게 한다.
public fun List<String>.toDDayRepeatInputListOrNull(): List<DDayRepeatInput>? = map { value -> value.toDDayRepeatInputOrNull() ?: return null }

public fun Set<DateTimeUnit.DateBased>.toDDayRepeatValueList(): List<String> = toRepeatInputList().map { input -> input.toDDayRepeatValue() }

internal fun List<String>.toDDayRepeatInputListArgument(): List<DDayRepeatInput> =
    toDDayRepeatInputListOrNull()
        ?: invalidArgument(message = "Each '$REPEATS' item must be an ISO 8601 period of days only or of years and months only, 1 or greater, like P100D, P3M or P1Y.")

private fun String.toDDayRepeatInputOrNull(): DDayRepeatInput? {
    val match = repeatPattern.matchEntire(this) ?: return null
    val (yearText, monthText, dayText) = match.destructured

    return when {
        dayText.isNotEmpty() -> if (yearText.isEmpty() && monthText.isEmpty()) dayText.toDayRepeatInputOrNull() else null
        yearText.isNotEmpty() || monthText.isNotEmpty() -> toMonthRepeatInputOrNull(yearText = yearText, monthText = monthText)
        else -> null
    }
}

private fun String.toDayRepeatInputOrNull(): DDayRepeatInput? {
    val days = toIntOrNull()?.takeIf { days -> days >= 1 } ?: return null

    return DDayRepeatInput(number = days, unit = DDayRepeatUnit.DAY)
}

private fun toMonthRepeatInputOrNull(
    yearText: String,
    monthText: String,
): DDayRepeatInput? {
    val years = yearText.ifEmpty { "0" }.toLongOrNull()
    val months = monthText.ifEmpty { "0" }.toLongOrNull()
    val totalMonths = if (years != null && months != null && years <= Int.MAX_VALUE) years * MONTHS_PER_YEAR + months else null

    return totalMonths
        ?.takeIf { value -> value in 1..Int.MAX_VALUE }
        ?.let { value -> DDayRepeatInput(number = value.toInt(), unit = DDayRepeatUnit.MONTH) }
}

private fun DDayRepeatInput.toDDayRepeatValue(): String {
    val value = checkNotNull(number)

    return when (unit) {
        DDayRepeatUnit.DAY -> "P${value}D"
        DDayRepeatUnit.MONTH -> "P${value}M"
        DDayRepeatUnit.YEAR -> "P${value}Y"
    }
}
