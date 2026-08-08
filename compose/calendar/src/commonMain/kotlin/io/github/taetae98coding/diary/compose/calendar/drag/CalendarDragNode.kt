package io.github.taetae98coding.diary.compose.calendar.drag

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.SuspendingPointerInputModifierNode
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.calendar.move.CalendarItemMoveState
import io.github.taetae98coding.diary.compose.calendar.move.startDragSession
import io.github.taetae98coding.diary.compose.calendar.select.CalendarSelectDragSession
import io.github.taetae98coding.diary.compose.calendar.select.CalendarSelectState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlin.math.abs

internal class CalendarDragNode(
    private var selectState: CalendarSelectState,
    private var moveState: CalendarItemMoveState?,
    private var dateAt: (position: Offset, size: IntSize) -> LocalDate,
    private var animateScrollMonth: (suspend (direction: Int) -> Boolean)?,
    private var onSelect: (LocalDateRange) -> Unit,
) : DelegatingNode(),
    CompositionLocalConsumerModifierNode,
    GlobalPositionAwareModifierNode {
    private val pointerInputNode = delegate(SuspendingPointerInputModifierNode { detectDrag() })

    private var layoutCoordinates: LayoutCoordinates? = null
    private var session: CalendarDragSession? = null
    private var isMoveSession = false
    private var pointerPosition = Offset.Zero
    private var pointerBoundsSize = IntSize.Zero
    private var edgeDirection = 0
    private var monthScrollJob: Job? = null

    fun update(
        selectState: CalendarSelectState,
        moveState: CalendarItemMoveState?,
        dateAt: (position: Offset, size: IntSize) -> LocalDate,
        animateScrollMonth: (suspend (direction: Int) -> Boolean)?,
        onSelect: (LocalDateRange) -> Unit,
    ) {
        this.dateAt = dateAt
        this.animateScrollMonth = animateScrollMonth
        this.onSelect = onSelect

        if (this.selectState !== selectState || this.moveState !== moveState) {
            edgeDirection = 0
            session?.cancel()
            session = null
            isMoveSession = false
            this.selectState = selectState
            this.moveState = moveState
            pointerInputNode.resetPointerInputHandler()
        }
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        layoutCoordinates = coordinates
    }

    private suspend fun PointerInputScope.detectDrag() {
        val edgeWidth = EdgeScrollWidth.toPx()
        val monthSwipeDistance = MonthSwipeDistance.toPx()

        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val longPress = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture

            session = startSession(position = longPress.position)
            drag(position = longPress.position, size = size)
            performHapticFeedback(HapticFeedbackType.LongPress)
            updateEdgeScroll(position = longPress.position, size = size, edgeWidth = edgeWidth)

            val isFinished =
                awaitDrag(
                    longPress = longPress,
                    edgeWidth = edgeWidth,
                    monthSwipeDistance = monthSwipeDistance,
                )

            edgeDirection = 0
            if (isFinished) session?.finish() else session?.cancel()
            session = null
        }
    }

    private suspend fun AwaitPointerEventScope.awaitDrag(
        longPress: PointerInputChange,
        edgeWidth: Float,
        monthSwipeDistance: Float,
    ): Boolean {
        val monthSwipe = CalendarMonthSwipe(distance = monthSwipeDistance)
        var change = longPress

        while (!change.changedToUpIgnoreConsumed()) {
            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
            val next = event.changes.firstOrNull { it.id == longPress.id } ?: return false

            next.consume()
            if (!next.changedToUpIgnoreConsumed()) {
                if (drag(position = next.position, size = size)) {
                    performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                }
                updateEdgeScroll(position = next.position, size = size, edgeWidth = edgeWidth)
            }
            trackMonthSwipe(event = event, primaryId = longPress.id, monthSwipe = monthSwipe)

            change = next
        }

        return true
    }

    private fun startSession(position: Offset): CalendarDragSession {
        val moveSession =
            moveState?.let { moveState ->
                layoutCoordinates?.let { coordinates ->
                    moveState.startDragSession(coordinates = coordinates, position = position)
                }
            }

        isMoveSession = moveSession != null

        return moveSession ?: CalendarSelectDragSession(state = selectState, onSelect = onSelect)
    }

    private fun drag(
        position: Offset,
        size: IntSize,
    ): Boolean {
        pointerPosition = position
        pointerBoundsSize = size

        val session = session ?: return false

        return session.drag(
            date = dateAt(position, size),
            windowPosition = layoutCoordinates?.localToWindow(position),
        )
    }

    private fun trackMonthSwipe(
        event: PointerEvent,
        primaryId: PointerId,
        monthSwipe: CalendarMonthSwipe,
    ) {
        event.changes.forEach { change ->
            if (change.id == primaryId) return@forEach

            // 이동을 유지한 채 달을 넘기는 손가락이므로, 페이저 스와이프와 아이템 스크롤을 일으키지 않게 소비한다.
            change.consume()
            if (!isMoveSession) return@forEach

            monthSwipe.direction(change = change)?.let { direction -> startMonthScroll(direction = direction) }
        }
    }

    private fun updateEdgeScroll(
        position: Offset,
        size: IntSize,
        edgeWidth: Float,
    ) {
        if (animateScrollMonth == null) return

        edgeDirection =
            when {
                position.x < edgeWidth -> PREVIOUS_MONTH_DIRECTION
                position.x > size.width - edgeWidth -> NEXT_MONTH_DIRECTION
                else -> 0
            }

        if (edgeDirection == 0 || monthScrollJob?.isActive == true) return

        monthScrollJob =
            coroutineScope.launch {
                var hasMoved = true

                while (hasMoved && edgeDirection != 0) {
                    hasMoved = scrollMonth(direction = edgeDirection)
                }
            }
    }

    private fun startMonthScroll(direction: Int) {
        if (animateScrollMonth == null || monthScrollJob?.isActive == true) return

        monthScrollJob = coroutineScope.launch { scrollMonth(direction = direction) }
    }

    private suspend fun scrollMonth(direction: Int): Boolean {
        val hasMoved = animateScrollMonth?.invoke(direction) ?: false

        if (hasMoved && drag(position = pointerPosition, size = pointerBoundsSize)) {
            performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }

        return hasMoved
    }

    private fun performHapticFeedback(type: HapticFeedbackType) {
        currentValueOf(LocalHapticFeedback).performHapticFeedback(type)
    }
}

private class CalendarMonthSwipe(
    private val distance: Float,
) {
    private val startPositionMap = mutableMapOf<PointerId, Offset>()
    private val swipedIdSet = mutableSetOf<PointerId>()

    fun direction(change: PointerInputChange): Int? {
        if (change.changedToUpIgnoreConsumed()) {
            startPositionMap -= change.id
            swipedIdSet -= change.id

            return null
        }

        val offset = change.position - startPositionMap.getOrPut(change.id) { change.position }
        val isSwiped = change.id !in swipedIdSet && abs(offset.x) > distance && abs(offset.x) > abs(offset.y)

        if (isSwiped) {
            swipedIdSet += change.id
        }

        return if (isSwiped) {
            if (offset.x < 0) NEXT_MONTH_DIRECTION else PREVIOUS_MONTH_DIRECTION
        } else {
            null
        }
    }
}

private const val PREVIOUS_MONTH_DIRECTION = -1
private const val NEXT_MONTH_DIRECTION = 1
private val EdgeScrollWidth = 24.dp
private val MonthSwipeDistance = 48.dp
