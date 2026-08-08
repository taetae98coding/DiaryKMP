package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import io.github.taetae98coding.diary.compose.calendar.grid.CalendarWeekOfMonthGridGroupScope
import io.github.taetae98coding.diary.compose.calendar.move.CalendarItemMoveState
import io.github.taetae98coding.diary.compose.calendar.move.calendarMoveItem
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo

private const val MOVING_MEMO_ALPHA = 0.38F

internal fun CalendarWeekOfMonthGridGroupScope.memoItem(
    moveState: CalendarItemMoveState,
    memoProvider: () -> List<CalendarMemo>,
    onEvent: (CalendarHomeScaffoldEvent) -> Unit,
) {
    memoProvider().forEach { memo ->
        item(
            dateRange = memo.dateTime.toDateRange(),
            key = memo.id,
        ) {
            CalendarHomeMemoText(
                memo = memo,
                modifier =
                    Modifier
                        .animateItem()
                        .graphicsLayer { alpha = if (moveState.moving?.key == memo.id) MOVING_MEMO_ALPHA else 1F }
                        .calendarMoveItem(
                            state = moveState,
                            key = memo.id,
                            dateRange = memo.dateTime.toDateRange(),
                            onMoved = { toDateRange ->
                                onEvent(
                                    CalendarHomeScaffoldEvent.MoveMemo(
                                        id = memo.id,
                                        fromDateTime = memo.dateTime,
                                        toDateRange = toDateRange,
                                    ),
                                )
                            },
                        ).clickable(role = Role.Button) { onEvent(CalendarHomeScaffoldEvent.ClickMemo(id = memo.id)) },
            )
        }
    }
}
