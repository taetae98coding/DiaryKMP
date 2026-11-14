package io.github.taetae98coding.diary.compose.core.systemscroll

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

public actual fun Modifier.systemScroll(
    state: ScrollableState,
    onScrollToTop: suspend () -> Unit,
): Modifier = this then SystemScrollElement(state = state, onScrollToTop = onScrollToTop)

private data class SystemScrollElement(
    val state: ScrollableState,
    val onScrollToTop: suspend () -> Unit,
) : ModifierNodeElement<SystemScrollNode>() {
    override fun create(): SystemScrollNode = SystemScrollNode(state = state, onScrollToTop = onScrollToTop)

    override fun update(node: SystemScrollNode) {
        node.update(state = state, onScrollToTop = onScrollToTop)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "systemScroll"
        properties["state"] = state
    }
}

private class SystemScrollNode(
    private var state: ScrollableState,
    private var onScrollToTop: suspend () -> Unit,
) : Modifier.Node(),
    CompositionLocalConsumerModifierNode,
    GlobalPositionAwareModifierNode {
    private var target: SystemScrollTarget? = null
    private var coordinates: LayoutCoordinates? = null
    private var placedBounds: Rect? = null
    private var mirrorJob: Job? = null

    override fun onAttach() {
        val target =
            SystemScrollTarget(
                hostView = currentValueOf(LocalUIViewController).view,
                canScrollToTop = { isVisible() && state.canScrollBackward },
                onScrollToTop = { coroutineScope.launch { onScrollToTop() } },
            )

        this.target = target
        target.attach()
        mirror()
    }

    override fun onDetach() {
        target?.detach()
        target = null
        coordinates = null
        placedBounds = null
    }

    // 스크롤하는 동안에도 불리므로 자리가 바뀌었을 때만 iOS에 알린다.
    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val bounds = coordinates.boundsInRoot()

        this.coordinates = coordinates
        if (bounds != placedBounds) {
            placedBounds = bounds
            target?.place(bounds = bounds, density = requireDensity().density)
            target?.sync()
        }
    }

    fun update(
        state: ScrollableState,
        onScrollToTop: suspend () -> Unit,
    ) {
        this.onScrollToTop = onScrollToTop
        if (this.state !== state) {
            this.state = state
            if (isAttached) mirror()
        }
    }

    // Pager는 옆 페이지를 미리 구성해 두거나 화면 밖에 놓아 두기도 한다. 그런 노드는 한 번도 놓이지 않았거나, 조상에 모두 잘려 루트 기준 bounds가 비어 있다.
    private fun isVisible(): Boolean = coordinates?.takeIf { it.isAttached }?.boundsInRoot()?.isEmpty == false

    private fun mirror() {
        mirrorJob?.cancel()
        mirrorJob =
            coroutineScope.launch {
                snapshotFlow { state.canScrollBackward }.collect { target?.sync() }
            }
    }
}
