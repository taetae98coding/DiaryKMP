package io.github.taetae98coding.diary.compose.calendar.select

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import io.github.taetae98coding.diary.compose.calendar.Calendar
import io.github.taetae98coding.diary.compose.calendar.CalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDateRange

internal const val WAIT_TIMEOUT_MILLIS = 10_000L
internal const val IDLE_WAIT_MILLIS = 5_000L
private const val LONG_PRESS_MARGIN_MILLIS = 100L
private const val FRAME_MILLIS = 16L

internal fun ComposeContentTestRule.setCalendar(
    calendarState: CalendarState = CalendarState(initialYearMonth = JULY_2026),
    onSelect: ((LocalDateRange) -> Unit)? = {},
    hapticFeedback: HapticFeedback? = null,
) {
    setContent {
        DiaryTheme {
            CompositionLocalProvider(
                LocalHapticFeedback provides (hapticFeedback ?: LocalHapticFeedback.current),
            ) {
                Calendar(
                    state = calendarState,
                    onSelect = onSelect,
                ) {}
            }
        }
    }
}

internal fun ComposeContentTestRule.rootWidth(): Float =
    onRoot()
        .fetchSemanticsNode()
        .size.width
        .toFloat()

internal fun ComposeContentTestRule.dayCenter(
    day: Int,
    index: Int = 0,
): Offset =
    onAllNodesWithText(day.toString())[index]
        .fetchSemanticsNode()
        .boundsInRoot
        .center

internal fun ComposeContentTestRule.performLongPress(position: Offset) {
    onRoot().performTouchInput {
        down(position)
        advanceEventTime(viewConfiguration.longPressTimeoutMillis + LONG_PRESS_MARGIN_MILLIS)
        moveBy(Offset.Zero)
    }
    waitForIdle()
}

internal fun ComposeContentTestRule.performMoveTo(position: Offset) {
    onRoot().performTouchInput { moveTo(position) }
    waitForIdle()
}

// 가장자리 머무름처럼 달 이동이 반복되는 동안에는 idle 상태가 되지 않으므로 클록 자동 진행을 끈 뒤 이동만 주입한다.
internal fun ComposeContentTestRule.performMove(position: Offset) {
    onRoot().performTouchInput { moveTo(position) }
}

internal fun ComposeContentTestRule.performUp() {
    onRoot().performTouchInput { up() }
    waitForIdle()
}

// 손을 뗀 뒤 클록 자동 진행을 복구해 진행 중인 달 전환이 끝나기를 기다린다.
internal fun ComposeContentTestRule.performUpAndSettle() {
    onRoot().performTouchInput { up() }
    mainClock.autoAdvance = true
    waitForIdle()
}

internal fun ComposeContentTestRule.advanceTimeUntil(
    timeoutMillis: Long = WAIT_TIMEOUT_MILLIS,
    condition: () -> Boolean,
) {
    var elapsedMillis = 0L

    while (!condition()) {
        check(elapsedMillis < timeoutMillis) { "${timeoutMillis}ms 안에 조건이 충족되지 않았다." }

        mainClock.advanceTimeByFrame()
        elapsedMillis += FRAME_MILLIS
    }
}
