package io.github.taetae98coding.diary.compose.calendar.move

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlinx.datetime.LocalDateRange

@Stable
public class CalendarItemMoveState {
    private val targetList = mutableListOf<CalendarMoveTarget>()

    public var moving: CalendarMovingItem? by mutableStateOf(null)
        internal set

    public var pointerPosition: Offset by mutableStateOf(Offset.Zero)
        internal set

    internal fun register(target: CalendarMoveTarget) {
        targetList += target
    }

    internal fun unregister(target: CalendarMoveTarget) {
        targetList -= target
    }

    internal fun findTarget(windowPosition: Offset): CalendarMoveTarget? = targetList.lastOrNull { it.bounds.contains(windowPosition) }

    internal fun findPieceBoundsList(
        key: Any,
        bounds: Rect,
    ): List<Rect> =
        targetList
            .filter { it.key == key && it.bounds.overlaps(bounds) }
            .map { it.bounds }
}

@Stable
public class CalendarMovingItem internal constructor(
    public val key: Any,
    public val fromDateRange: LocalDateRange,
    public val pieceBoundsList: List<Rect>,
    public val startPointerPosition: Offset,
    internal val onMoved: (LocalDateRange) -> Unit,
) {
    public var toDateRange: LocalDateRange by mutableStateOf(fromDateRange)
        internal set
}

internal class CalendarMoveTarget(
    val key: Any,
    val dateRange: LocalDateRange,
    val onMoved: (LocalDateRange) -> Unit,
) {
    // 배치마다 갱신되는 레이아웃 값이라 값 교체 대신 슬롯으로 둔다.
    var bounds: Rect = Rect.Zero
}
