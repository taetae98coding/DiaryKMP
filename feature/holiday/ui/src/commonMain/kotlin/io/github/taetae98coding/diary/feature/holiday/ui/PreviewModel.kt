package io.github.taetae98coding.diary.feature.holiday.ui

import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import kotlinx.datetime.LocalDate

internal fun previewGoldenHoliday(): GoldenHoliday =
    GoldenHoliday(
        dateRange = LocalDate(year = 2026, month = 9, day = 24)..LocalDate(year = 2026, month = 9, day = 27),
        holidayList =
            listOf(
                Holiday(
                    name = "추석",
                    isHoliday = true,
                    dateRange = LocalDate(year = 2026, month = 9, day = 25)..LocalDate(year = 2026, month = 9, day = 26),
                ),
            ),
        annualLeaveDateRangeList =
            listOf(
                LocalDate(year = 2026, month = 9, day = 24)..LocalDate(year = 2026, month = 9, day = 24),
            ),
    )

internal fun previewGoldenHolidayGroup(): GoldenHolidayGroup = GoldenHolidayGroup(optionList = listOf(previewGoldenHoliday()))
