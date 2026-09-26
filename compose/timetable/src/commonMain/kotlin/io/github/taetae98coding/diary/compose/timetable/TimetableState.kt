package io.github.taetae98coding.diary.compose.timetable

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

@Stable
public class TimetableState internal constructor(
    public val type: TimetableType,
    initialPage: Int,
    initialVerticalScrollOffset: Int?,
) {
    internal val pagerState: PagerState = PagerState(currentPage = initialPage) { Int.MAX_VALUE }

    internal var verticalScrollOffset: Int? = initialVerticalScrollOffset

    public val currentDateRange: LocalDateRange
        get() = type.dateRangeAt(pagerState.currentPage)

    public constructor(
        type: TimetableType,
        initialDate: LocalDate,
    ) : this(
        type = type,
        initialPage = type.pageOf(initialDate),
        initialVerticalScrollOffset = null,
    )

    internal fun dateRangeAt(page: Int): LocalDateRange = type.dateRangeAt(page)

    public suspend fun animateScrollToPrevious() {
        if (pagerState.currentPage <= 0) return

        pagerState.animateScrollToPage(pagerState.currentPage - 1)
    }

    public suspend fun animateScrollToNext() {
        pagerState.animateScrollToPage(pagerState.currentPage + 1)
    }

    internal companion object {
        private const val NO_SCROLL_OFFSET = -1

        val Saver =
            listSaver<TimetableState, Any>(
                save = { state ->
                    listOf(
                        state.type.name,
                        state.pagerState.currentPage,
                        state.verticalScrollOffset ?: NO_SCROLL_OFFSET,
                    )
                },
                restore = { list ->
                    TimetableState(
                        type = TimetableType.valueOf(list[0] as String),
                        initialPage = list[1] as Int,
                        initialVerticalScrollOffset = (list[2] as Int).takeIf { it != NO_SCROLL_OFFSET },
                    )
                },
            )
    }
}

@Composable
public fun rememberTimetableState(
    type: TimetableType = TimetableType.DAY,
    initialDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
): TimetableState =
    rememberSaveable(saver = TimetableState.Saver) {
        TimetableState(type = type, initialDate = initialDate)
    }
