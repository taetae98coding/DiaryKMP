package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Stable
internal class HolidayHomeScaffoldState(
    initialYear: Int,
    initialAnnualLeaveCount: Int,
) {
    val pagerState: PagerState = PagerState(currentPage = initialYear.toPage()) { Int.MAX_VALUE }

    var annualLeaveCount: Int by mutableIntStateOf(initialAnnualLeaveCount)
        private set

    val year: Int
        get() = pagerState.currentPage.toYear()

    val canDecreaseAnnualLeave: Boolean
        get() = annualLeaveCount > MIN_ANNUAL_LEAVE_COUNT

    fun decreaseAnnualLeave() {
        if (canDecreaseAnnualLeave) annualLeaveCount--
    }

    fun increaseAnnualLeave() {
        annualLeaveCount++
    }

    suspend fun animateScrollTo(year: Int) {
        pagerState.animateScrollToPage(year.toPage())
    }

    companion object {
        val Saver: Saver<HolidayHomeScaffoldState, List<Int>> =
            Saver(
                save = { listOf(it.year, it.annualLeaveCount) },
                restore = {
                    HolidayHomeScaffoldState(
                        initialYear = it[YEAR_INDEX],
                        initialAnnualLeaveCount = it[ANNUAL_LEAVE_COUNT_INDEX],
                    )
                },
            )
    }
}

@Composable
internal fun rememberHolidayHomeScaffoldState(
    initialYear: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year,
    initialAnnualLeaveCount: Int = MIN_ANNUAL_LEAVE_COUNT,
): HolidayHomeScaffoldState =
    rememberSaveable(saver = HolidayHomeScaffoldState.Saver) {
        HolidayHomeScaffoldState(
            initialYear = initialYear,
            initialAnnualLeaveCount = initialAnnualLeaveCount,
        )
    }

internal fun Int.toYear(): Int = this + FIRST_PAGE_YEAR

// 페이지는 0보다 작을 수 없으므로 첫 페이지의 1년이 이동할 수 있는 가장 이전 년도가 된다.
private fun Int.toPage(): Int = (this - FIRST_PAGE_YEAR).coerceAtLeast(0)

internal const val MIN_ANNUAL_LEAVE_COUNT: Int = 0

private const val FIRST_PAGE_YEAR = 1
private const val YEAR_INDEX = 0
private const val ANNUAL_LEAVE_COUNT_INDEX = 1
