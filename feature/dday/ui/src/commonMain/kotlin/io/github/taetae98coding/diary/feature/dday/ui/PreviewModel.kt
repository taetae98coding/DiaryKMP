package io.github.taetae98coding.diary.feature.dday.ui

import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate

internal fun previewDDayDetail(title: String): DDayDetail =
    DDayDetail(
        title = title,
        description = "",
        color = 0xFF3A7BD5,
        baseDate = LocalDate(year = 2025, month = 1, day = 15),
        countsBaseDay = true,
        repeats = setOf(DateTimeUnit.DayBased(days = 100), DateTimeUnit.MonthBased(months = 12)),
        endDate = null,
        overflowPolicy = DDayOverflowPolicy.CLAMP_TO_MONTH_END,
    )
