package io.github.taetae98coding.diary.compose.core.chip

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints

/**
 * 칩이 놓인 자리에서 쓸 수 있는 폭까지 줄어들 수 있게 한다.
 *
 * Material 3 칩은 내용을 `Modifier.width(IntrinsicSize.Max)`로 감싸 최소 intrinsic 폭을 최대 intrinsic 폭과 같게 보고한다.
 * 그대로 두면 칩을 늘어놓는 배치가 칩을 줄일 수 없다고 판단해, 이름이 긴 칩이 영역 밖으로 넘어가 양 끝이 잘린다.
 * 최소 intrinsic 폭만 0으로 낮춰 남은 폭에 맞춰 줄어들게 하고, 실제 측정은 그대로 둔다.
 */
internal fun Modifier.shrinkToAvailableWidth(): Modifier = this then ShrinkToAvailableWidthElement

private data object ShrinkToAvailableWidthElement : ModifierNodeElement<ShrinkToAvailableWidthNode>() {
    override fun create(): ShrinkToAvailableWidthNode = ShrinkToAvailableWidthNode()

    override fun update(node: ShrinkToAvailableWidthNode) = Unit
}

private class ShrinkToAvailableWidthNode :
    Modifier.Node(),
    LayoutModifierNode {
    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)

        return layout(placeable.width, placeable.height) { placeable.place(x = 0, y = 0) }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int,
    ): Int = 0
}
