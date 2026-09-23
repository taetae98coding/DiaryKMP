package io.github.taetae98coding.diary.domain.holiday.usecase

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.minus
import kotlinx.datetime.plus

internal data class GoldenHolidayCandidate(
    val dateRange: LocalDateRange,
    val restDayCount: Int,
    val annualLeaveDateRangeList: List<LocalDateRange>,
)

private data class JoinedRestRange(
    val fromIndex: Int,
    val toIndex: Int,
    val restDayCount: Int,
    val innerAnnualLeaveDateRangeList: List<LocalDateRange>,
) {
    val usedAnnualLeaveCount: Int
        get() = innerAnnualLeaveDateRangeList.sumOf { dateRange -> dateRange.size }
}

internal fun candidateList(
    restRangeList: List<LocalDateRange>,
    holidayDateSet: Set<LocalDate>,
    annualLeaveCount: Int,
): List<GoldenHolidayCandidate> =
    restRangeList.indices.flatMap { fromIndex ->
        val joinedRestRange =
            joinedRestRange(
                restRangeList = restRangeList,
                holidayDateSet = holidayDateSet,
                fromIndex = fromIndex,
                annualLeaveCount = annualLeaveCount,
            )

        joinedRestRange
            ?.let { candidateList(restRangeList = restRangeList, joinedRestRange = it, annualLeaveCount = annualLeaveCount) }
            .orEmpty()
    }

// 같은 시작에서 덜 이어 붙인 묶음은 더 붙인 묶음에 밀리므로, 더 붙일 수 없을 때까지 붙인 묶음만 남긴다.
private fun joinedRestRange(
    restRangeList: List<LocalDateRange>,
    holidayDateSet: Set<LocalDate>,
    fromIndex: Int,
    annualLeaveCount: Int,
): JoinedRestRange? {
    val innerAnnualLeaveDateRangeList = mutableListOf<LocalDateRange>()
    var restDayCount = 0
    var usedAnnualLeaveCount = 0
    var hasHoliday = false
    var joinedRestRange: JoinedRestRange? = null

    for (toIndex in fromIndex..restRangeList.lastIndex) {
        if (toIndex > fromIndex) {
            val innerAnnualLeaveDateRange = innerAnnualLeaveDateRange(restRangeList = restRangeList, toIndex = toIndex)
            if (usedAnnualLeaveCount + innerAnnualLeaveDateRange.size > annualLeaveCount) break

            usedAnnualLeaveCount += innerAnnualLeaveDateRange.size
            innerAnnualLeaveDateRangeList.add(innerAnnualLeaveDateRange)
        }

        restDayCount += restRangeList[toIndex].size
        hasHoliday = hasHoliday || restRangeList[toIndex].any { date -> date in holidayDateSet }

        if (restDayCount >= MIN_REST_DAY_COUNT && hasHoliday) {
            joinedRestRange =
                JoinedRestRange(
                    fromIndex = fromIndex,
                    toIndex = toIndex,
                    restDayCount = restDayCount,
                    innerAnnualLeaveDateRangeList = innerAnnualLeaveDateRangeList.toList(),
                )
        }
    }

    return joinedRestRange
}

private fun candidateList(
    restRangeList: List<LocalDateRange>,
    joinedRestRange: JoinedRestRange,
    annualLeaveCount: Int,
): List<GoldenHolidayCandidate> {
    val remainingCount = annualLeaveCount - joinedRestRange.usedAnnualLeaveCount
    val frontRoom = restRangeList.frontRoom(fromIndex = joinedRestRange.fromIndex).coerceAtMost(remainingCount)
    val backRoom = restRangeList.backRoom(toIndex = joinedRestRange.toIndex).coerceAtMost(remainingCount)
    val start = restRangeList[joinedRestRange.fromIndex].start
    val endInclusive = restRangeList[joinedRestRange.toIndex].endInclusive

    return ((remainingCount - backRoom).coerceAtLeast(0)..frontRoom).map { frontCount ->
        val backCount = remainingCount - frontCount

        GoldenHolidayCandidate(
            dateRange =
                LocalDateRange(
                    start = start.minus(frontCount, DateTimeUnit.DAY),
                    endInclusive = endInclusive.plus(backCount, DateTimeUnit.DAY),
                ),
            restDayCount = joinedRestRange.restDayCount,
            annualLeaveDateRangeList =
                frontAnnualLeaveDateRangeList(endInclusive = start.minus(1, DateTimeUnit.DAY), count = frontCount) +
                    joinedRestRange.innerAnnualLeaveDateRangeList +
                    annualLeaveDateRangeList(start = endInclusive.plus(1, DateTimeUnit.DAY), count = backCount),
        )
    }
}

private fun innerAnnualLeaveDateRange(
    restRangeList: List<LocalDateRange>,
    toIndex: Int,
): LocalDateRange =
    LocalDateRange(
        start = restRangeList[toIndex - 1].endInclusive.plus(1, DateTimeUnit.DAY),
        endInclusive = restRangeList[toIndex].start.minus(1, DateTimeUnit.DAY),
    )

private fun List<LocalDateRange>.frontRoom(fromIndex: Int): Int =
    if (fromIndex == 0) {
        Int.MAX_VALUE
    } else {
        innerAnnualLeaveDateRange(restRangeList = this, toIndex = fromIndex).size - 1
    }

private fun List<LocalDateRange>.backRoom(toIndex: Int): Int =
    if (toIndex == lastIndex) {
        Int.MAX_VALUE
    } else {
        innerAnnualLeaveDateRange(restRangeList = this, toIndex = toIndex + 1).size - 1
    }

private fun annualLeaveDateRangeList(
    start: LocalDate,
    count: Int,
): List<LocalDateRange> =
    if (count <= 0) {
        emptyList()
    } else {
        listOf(LocalDateRange(start = start, endInclusive = start.plus(count - 1, DateTimeUnit.DAY)))
    }

private fun frontAnnualLeaveDateRangeList(
    endInclusive: LocalDate,
    count: Int,
): List<LocalDateRange> =
    annualLeaveDateRangeList(
        start = endInclusive.minus((count - 1).coerceAtLeast(0), DateTimeUnit.DAY),
        count = count,
    )

private const val MIN_REST_DAY_COUNT = 3
