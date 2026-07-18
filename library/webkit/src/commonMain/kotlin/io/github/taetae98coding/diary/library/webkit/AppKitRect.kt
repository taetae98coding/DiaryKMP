package io.github.taetae98coding.diary.library.webkit

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

internal data class AppKitRect(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
) {
    companion object {
        val ZERO: AppKitRect = AppKitRect(x = 0.0, y = 0.0, width = 0.0, height = 0.0)
    }
}

// AWT는 좌상단 원점, AppKit은 좌하단 원점이므로 컨텐츠 높이를 기준으로 y를 뒤집는다.
internal fun AppKitRect.toSegment(arena: Arena): MemorySegment {
    val segment = arena.allocate(ObjCRuntime.cgRectLayout)

    doubleArrayOf(x, y, width, height).forEachIndexed { index, value ->
        segment.setAtIndex(ValueLayout.JAVA_DOUBLE, index.toLong(), value)
    }

    return segment
}

internal fun appKitFrame(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    contentHeight: Int,
): AppKitRect =
    AppKitRect(
        x = x.toDouble(),
        y = (contentHeight - y - height).toDouble(),
        width = width.toDouble(),
        height = height.toDouble(),
    )
