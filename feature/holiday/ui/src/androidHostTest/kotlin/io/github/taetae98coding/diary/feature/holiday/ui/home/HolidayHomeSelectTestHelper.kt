package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput

private const val LONG_PRESS_MARGIN_MILLIS = 100L
private const val EDGE_MARGIN = 1F

internal fun ComposeContentTestRule.dayCenter(day: Int): Offset =
    onNodeWithText(day.toString())
        .fetchSemanticsNode()
        .boundsInRoot
        .center

internal fun ComposeContentTestRule.rootCenter(): Offset =
    onRoot()
        .fetchSemanticsNode()
        .boundsInRoot
        .center

internal fun ComposeContentTestRule.rootBottom(): Float =
    onRoot()
        .fetchSemanticsNode()
        .boundsInRoot.bottom - EDGE_MARGIN

internal fun ComposeContentTestRule.rootRight(): Float =
    onRoot()
        .fetchSemanticsNode()
        .boundsInRoot.right - EDGE_MARGIN

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

internal fun ComposeContentTestRule.performUp() {
    onRoot().performTouchInput { up() }
    waitForIdle()
}
