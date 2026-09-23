package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.plus
import org.koin.core.annotation.Factory

@Factory
public class GetGoldenHolidayUseCase internal constructor(
    private val getHolidayUseCase: GetHolidayUseCase,
) : FlowUseCase<GetGoldenHolidayUseCase.Parameter, List<GoldenHolidayGroup>>() {
    override fun execute(parameter: Parameter): Flow<Result<List<GoldenHolidayGroup>>> =
        holidayListFlow(year = parameter.year)
            .map { holidayList ->
                Result.success(
                    goldenHolidayGroupList(
                        year = parameter.year,
                        annualLeaveCount = parameter.annualLeaveCount,
                        holidayList = holidayList,
                    ),
                )
            }

    private fun holidayListFlow(year: Int): Flow<List<Holiday>> =
        combine(
            year.goldenHolidaySourceYearList().map { targetYear ->
                getHolidayUseCase(parameter = targetYear)
                    .map { result -> result.getOrDefault(emptyList()) }
            },
        ) { holidayListArray -> holidayListArray.flatMap { holidayList -> holidayList } }

    public data class Parameter(
        val year: Int,
        val annualLeaveCount: Int,
    )
}

private fun goldenHolidayGroupList(
    year: Int,
    annualLeaveCount: Int,
    holidayList: List<Holiday>,
): List<GoldenHolidayGroup> {
    val holidayDateSet = holidayDateSet(holidayList = holidayList)
    val restRangeList = restRangeList(searchRange = searchRange(year = year), holidayDateSet = holidayDateSet)

    return candidateList(
        restRangeList = restRangeList,
        holidayDateSet = holidayDateSet,
        annualLeaveCount = annualLeaveCount,
    ).groupByOverlap()
        .flatMap { candidateGroup -> candidateGroup.toGoldenHolidayGroupList(holidayList = holidayList) }
        .filter { group -> group.overlaps(year = year) }
        .sortedBy { group ->
            group.optionList
                .first()
                .dateRange.start
        }
}

private fun GoldenHolidayGroup.overlaps(year: Int): Boolean = optionList.any { option -> year in option.dateRange.start.year..option.dateRange.endInclusive.year }

private fun searchRange(year: Int): LocalDateRange {
    val sourceYearList = year.goldenHolidaySourceYearList()

    return LocalDateRange(
        start = LocalDate(year = sourceYearList.first(), month = 1, day = 1),
        endInclusive = LocalDate(year = sourceYearList.last(), month = 12, day = 31),
    )
}

private fun holidayDateSet(holidayList: List<Holiday>): Set<LocalDate> =
    holidayList
        .filter { holiday -> holiday.isHoliday }
        .flatMapTo(mutableSetOf()) { holiday -> holiday.dateRange }

private fun restRangeList(
    searchRange: LocalDateRange,
    holidayDateSet: Set<LocalDate>,
): List<LocalDateRange> =
    searchRange
        .filter { date -> isRestDay(date = date, holidayDateSet = holidayDateSet) }
        .groupConsecutive()

private fun isRestDay(
    date: LocalDate,
    holidayDateSet: Set<LocalDate>,
): Boolean = date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY || date in holidayDateSet

private fun List<LocalDate>.groupConsecutive(): List<LocalDateRange> {
    val rangeList = mutableListOf<LocalDateRange>()
    var start = firstOrNull() ?: return rangeList
    var endInclusive = start

    drop(1).forEach { date ->
        if (date == endInclusive.plus(1, DateTimeUnit.DAY)) {
            endInclusive = date
        } else {
            rangeList.add(LocalDateRange(start = start, endInclusive = endInclusive))
            start = date
            endInclusive = date
        }
    }
    rangeList.add(LocalDateRange(start = start, endInclusive = endInclusive))

    return rangeList
}
