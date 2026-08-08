package io.github.taetae98coding.diary.compose.calendar

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import io.github.taetae98coding.diary.compose.calendar.move.CalendarItemMoveState
import io.github.taetae98coding.diary.compose.calendar.scroll.CalendarItemScrollState
import io.github.taetae98coding.diary.compose.calendar.select.CalendarSelectState
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import kotlin.time.Clock

@Stable
public class CalendarState(
    initialYearMonth: YearMonth,
) {
    internal val pagerState: PagerState = PagerState(currentPage = initialYearMonth.toPage()) { Int.MAX_VALUE }

    public val selectState: CalendarSelectState = CalendarSelectState()

    public val moveState: CalendarItemMoveState = CalendarItemMoveState()

    public val itemScrollState: CalendarItemScrollState = CalendarItemScrollState()

    public val currentYearMonth: YearMonth
        get() = pagerState.currentPage.toYearMonth()

    public suspend fun animateScrollTo(yearMonth: YearMonth) {
        pagerState.animateScrollToPage(yearMonth.toPage())
    }

    public suspend fun animateScrollToPreviousMonth() {
        pagerState.animateScrollToPage(pagerState.currentPage - 1)
    }

    public suspend fun animateScrollToNextMonth() {
        pagerState.animateScrollToPage(pagerState.currentPage + 1)
    }

    internal companion object {
        val Saver: Saver<CalendarState, Int> =
            Saver(
                save = { it.pagerState.currentPage },
                restore = { CalendarState(initialYearMonth = it.toYearMonth()) },
            )
    }
}

@Composable
public fun rememberCalendarState(initialYearMonth: YearMonth = Clock.System.todayIn(TimeZone.currentSystemDefault()).yearMonth): CalendarState =
    rememberSaveable(saver = CalendarState.Saver) {
        CalendarState(initialYearMonth = initialYearMonth)
    }

internal fun YearMonth.toPage(): Int = (year - 1) * MONTHS_PER_YEAR + (month.number - 1)

internal fun Int.toYearMonth(): YearMonth =
    YearMonth(
        year = this / MONTHS_PER_YEAR + 1,
        month = Month(this % MONTHS_PER_YEAR + 1),
    )

private const val MONTHS_PER_YEAR = 12
