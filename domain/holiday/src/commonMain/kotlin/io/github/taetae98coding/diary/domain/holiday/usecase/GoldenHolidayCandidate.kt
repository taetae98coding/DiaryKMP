package io.github.taetae98coding.diary.domain.holiday.usecase

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.minus
import kotlinx.datetime.plus

// 쉬는 날 덩어리들을 연차로 이어 붙이고, 남는 연차를 앞뒤에 붙여 연차를 모두 사용한 연휴 후보다.
internal data class GoldenHolidayCandidate(
    val dateRange: LocalDateRange,
    val restDayCount: Int,
    val annualLeaveDateRangeList: List<LocalDateRange>,
)

// 연차로 이어 붙일 수 있는 만큼 붙인 쉬는 날 덩어리 묶음이다.
private data class Core(
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
        val core =
            core(
                restRangeList = restRangeList,
                holidayDateSet = holidayDateSet,
                fromIndex = fromIndex,
                annualLeaveCount = annualLeaveCount,
            )

        core
            ?.let { candidateList(restRangeList = restRangeList, core = it, annualLeaveCount = annualLeaveCount) }
            .orEmpty()
    }

// 같은 시작에서 덜 이어 붙인 묶음은 더 붙인 묶음에 밀리므로, 더 붙일 수 없을 때까지 붙인 묶음만 남긴다.
private fun core(
    restRangeList: List<LocalDateRange>,
    holidayDateSet: Set<LocalDate>,
    fromIndex: Int,
    annualLeaveCount: Int,
): Core? {
    val innerAnnualLeaveDateRangeList = mutableListOf<LocalDateRange>()
    var restDayCount = 0
    var usedAnnualLeaveCount = 0
    var hasHoliday = false
    var core: Core? = null

    for (toIndex in fromIndex..restRangeList.lastIndex) {
        if (toIndex > fromIndex) {
            val innerAnnualLeaveDateRange = innerAnnualLeaveDateRange(restRangeList = restRangeList, toIndex = toIndex)
            if (usedAnnualLeaveCount + innerAnnualLeaveDateRange.size > annualLeaveCount) break

            usedAnnualLeaveCount += innerAnnualLeaveDateRange.size
            innerAnnualLeaveDateRangeList.add(innerAnnualLeaveDateRange)
        }

        restDayCount += restRangeList[toIndex].size
        hasHoliday = hasHoliday || restRangeList[toIndex].any { date -> date in holidayDateSet }

        // 황금연휴는 공휴일 덕분에 생기는 연휴이므로 공휴일이 없는 묶음은 후보로 보지 않는다.
        if (restDayCount >= MIN_REST_DAY_COUNT && hasHoliday) {
            core =
                Core(
                    fromIndex = fromIndex,
                    toIndex = toIndex,
                    restDayCount = restDayCount,
                    innerAnnualLeaveDateRangeList = innerAnnualLeaveDateRangeList.toList(),
                )
        }
    }

    return core
}

// 남는 연차를 앞에 몇 날 붙이느냐에 따라 후보가 갈린다.
private fun candidateList(
    restRangeList: List<LocalDateRange>,
    core: Core,
    annualLeaveCount: Int,
): List<GoldenHolidayCandidate> {
    val remainingCount = annualLeaveCount - core.usedAnnualLeaveCount
    val frontRoom = restRangeList.frontRoom(fromIndex = core.fromIndex).coerceAtMost(remainingCount)
    val backRoom = restRangeList.backRoom(toIndex = core.toIndex).coerceAtMost(remainingCount)
    val start = restRangeList[core.fromIndex].start
    val endInclusive = restRangeList[core.toIndex].endInclusive

    return ((remainingCount - backRoom).coerceAtLeast(0)..frontRoom).map { frontCount ->
        val backCount = remainingCount - frontCount

        GoldenHolidayCandidate(
            dateRange =
                LocalDateRange(
                    start = start.minus(frontCount, DateTimeUnit.DAY),
                    endInclusive = endInclusive.plus(backCount, DateTimeUnit.DAY),
                ),
            restDayCount = core.restDayCount,
            annualLeaveDateRangeList =
                frontAnnualLeaveDateRangeList(endInclusive = start.minus(1, DateTimeUnit.DAY), count = frontCount) +
                    core.innerAnnualLeaveDateRangeList +
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

// 앞쪽 이웃 덩어리에 닿으면 그 덩어리까지 이어 붙인 후보와 같아지므로, 닿기 전까지만 붙일 수 있다.
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
