package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.library.kotlinx.datetime.overlaps

internal fun List<GoldenHolidayCandidate>.groupByOverlap(): List<List<GoldenHolidayCandidate>> {
    val groupList = mutableListOf<MutableList<GoldenHolidayCandidate>>()

    // 시작일 순으로 훑으면 겹치는 후보는 언제나 마지막 묶음에만 닿는다.
    sortedBy { candidate -> candidate.dateRange.start }
        .forEach { candidate ->
            val lastGroup = groupList.lastOrNull()

            if (lastGroup != null && lastGroup.any { selected -> selected.dateRange overlaps candidate.dateRange }) {
                lastGroup.add(candidate)
            } else {
                groupList.add(mutableListOf(candidate))
            }
        }

    return groupList
}

internal fun List<GoldenHolidayCandidate>.toGoldenHolidayGroupList(holidayList: List<Holiday>): List<GoldenHolidayGroup> =
    map { candidate -> candidate.toGoldenHoliday(holidayList = holidayList) }
        .withoutDominated()
        .groupBy { option -> option.holidayList }
        .map { (_, optionList) ->
            GoldenHolidayGroup(
                optionList = optionList,
            )
        }

private fun List<GoldenHoliday>.withoutDominated(): List<GoldenHoliday> = filterNot { option -> any { other -> other.dominates(option) } }

private fun GoldenHoliday.dominates(other: GoldenHoliday): Boolean {
    val isNotWorse =
        this !== other &&
            dateRange.size >= other.dateRange.size &&
            holidayList.containsAll(other.holidayList)
    val isBetter =
        dateRange.size > other.dateRange.size ||
            holidayList.size > other.holidayList.size

    return isNotWorse && isBetter
}

private fun GoldenHolidayCandidate.toGoldenHoliday(holidayList: List<Holiday>): GoldenHoliday =
    GoldenHoliday(
        dateRange = dateRange,
        holidayList = holidayList.filter { holiday -> holiday.isHoliday && holiday.dateRange overlaps dateRange },
        annualLeaveDateRangeList = annualLeaveDateRangeList,
    )
