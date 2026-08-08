package io.github.taetae98coding.diary.feature.holiday.ui.home

import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month

// 2026년 2월 1일은 일요일이므로 2월 7일과 14일은 토요일, 2월 8일과 15일은 일요일이다.
internal object HolidayHomeTestFixture {
    const val YEAR = 2026

    const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
    const val DEFAULT_ANNUAL_LEAVE_LABEL = "Annual leave"
    const val DEFAULT_DECREASE_DESCRIPTION = "Decrease annual leave"
    const val DEFAULT_INCREASE_DESCRIPTION = "Increase annual leave"
    const val DEFAULT_ANNUAL_LEAVE_ITEM_LABEL = "Leave"
    const val DEFAULT_LOADING_DESCRIPTION = "Loading holidays"
    const val DEFAULT_ERROR_DESCRIPTION = "Holidays could not be loaded"
    const val DEFAULT_NOT_PROVIDED_DESCRIPTION = "Holidays are not provided for this year"
    const val DEFAULT_RETRY_LABEL = "Retry"

    const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
    const val KOREAN_ANNUAL_LEAVE_LABEL = "연차"
    const val KOREAN_DECREASE_DESCRIPTION = "연차 줄이기"
    const val KOREAN_INCREASE_DESCRIPTION = "연차 늘리기"
    const val DEFAULT_PREVIOUS_OPTION_DESCRIPTION = "Previous option"
    const val DEFAULT_NEXT_OPTION_DESCRIPTION = "Next option"

    const val KOREAN_ERROR_DESCRIPTION = "공휴일을 불러오지 못했어요"
    const val KOREAN_NOT_PROVIDED_DESCRIPTION = "이 년도의 공휴일은 제공하지 않아요"
    const val KOREAN_RETRY_LABEL = "다시 시도"
    const val KOREAN_PREVIOUS_OPTION_DESCRIPTION = "이전 연휴 안"
    const val KOREAN_NEXT_OPTION_DESCRIPTION = "다음 연휴 안"

    fun february(day: Int): LocalDate = LocalDate(year = YEAR, month = Month.FEBRUARY, day = day)

    fun holiday(
        name: String,
        start: LocalDate,
        endInclusive: LocalDate = start,
        isHoliday: Boolean = true,
    ): Holiday =
        Holiday(
            name = name,
            isHoliday = isHoliday,
            dateRange = LocalDateRange(start = start, endInclusive = endInclusive),
        )

    fun goldenHolidayGroup(optionList: List<GoldenHoliday>): GoldenHolidayGroup =
        GoldenHolidayGroup(
            optionList = optionList,
        )

    fun goldenHoliday(
        holidayList: List<Holiday>,
        start: LocalDate,
        endInclusive: LocalDate,
        annualLeaveDateRangeList: List<LocalDateRange> = emptyList(),
    ): GoldenHoliday =
        GoldenHoliday(
            dateRange = LocalDateRange(start = start, endInclusive = endInclusive),
            holidayList = holidayList,
            annualLeaveDateRangeList = annualLeaveDateRangeList,
        )
}
