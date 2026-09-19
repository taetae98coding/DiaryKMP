package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import io.github.taetae98coding.diary.compose.calendar.CalendarDefault
import io.github.taetae98coding.diary.compose.calendar.move.CalendarItemMoveState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.CalendarMemo
import io.github.taetae98coding.diary.feature.calendar.ui.previewCalendarMemo

private val MOVE_GHOST_ELEVATION = 8.dp

@Composable
internal fun CalendarHomeMoveGhost(
    moveState: CalendarItemMoveState,
    modifier: Modifier = Modifier,
    memoProvider: () -> List<CalendarMemo> = { emptyList() },
) {
    val moving = moveState.moving ?: return
    val memo = memoProvider().firstOrNull { it.id == moving.key } ?: return
    val density = LocalDensity.current
    val coordinatesState = remember { mutableStateOf<LayoutCoordinates?>(null) }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates -> coordinatesState.value = coordinates },
    ) {
        moving.pieceBoundsList.forEach { pieceBounds ->
            CalendarHomeMemoText(
                memo = memo,
                modifier =
                    Modifier
                        .offset {
                            val coordinates = coordinatesState.value ?: return@offset IntOffset.Zero
                            val dragDelta = moveState.pointerPosition - moving.startPointerPosition

                            coordinates.windowToLocal(pieceBounds.topLeft + dragDelta).round()
                        }.size(with(density) { pieceBounds.size.toDpSize() })
                        .shadow(elevation = MOVE_GHOST_ELEVATION, shape = CalendarDefault.itemShape),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun CalendarHomeMoveGhostPreview() {
    val memoList = remember { listOf(previewCalendarMemo()) }

    DiaryTheme {
        CalendarHomeMoveGhost(
            moveState = remember { CalendarItemMoveState() },
            memoProvider = { memoList },
        )
    }
}
