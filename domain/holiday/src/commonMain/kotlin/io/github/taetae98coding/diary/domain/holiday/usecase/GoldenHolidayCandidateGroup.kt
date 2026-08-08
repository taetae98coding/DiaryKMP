package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.GoldenHoliday
import io.github.taetae98coding.diary.core.model.holiday.GoldenHolidayGroup
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.library.kotlinx.datetime.overlaps

// 서로 겹치는 후보는 같은 연휴를 보는 다른 방법이므로 한 묶음으로 모은다.
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

// 담은 공휴일이 다르면 서로 다른 연휴이므로 항목을 나눈다. 그래서 대안을 넘겨도 항목의 공휴일 이름은 바뀌지 않는다.
internal fun List<GoldenHolidayCandidate>.toGoldenHolidayGroupList(holidayList: List<Holiday>): List<GoldenHolidayGroup> =
    map { candidate -> candidate.toGoldenHoliday(holidayList = holidayList) }
        .withoutDominated()
        .groupBy { option -> option.holidayList }
        .map { (_, optionList) ->
            GoldenHolidayGroup(
                optionList = optionList,
            )
        }

// 어느 면에서도 나을 게 없는 안은 보여 주지 않는다.
private fun List<GoldenHoliday>.withoutDominated(): List<GoldenHoliday> = filterNot { option -> any { other -> other.dominates(option) } }

// 일수가 짧지 않으면서 상대의 공휴일을 모두 담고, 그중 한 가지라도 더 나으면 상대를 밀어낸다.
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
