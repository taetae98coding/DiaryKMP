package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.approachLayout
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

@Composable
public fun Modifier.animatePlacement(
    lookaheadScope: LookaheadScope,
    animationSpec: FiniteAnimationSpec<IntOffset> = DefaultPlacementAnimationSpec,
): Modifier {
    val coroutineScope = rememberCoroutineScope()
    val animation = remember { PlacementAnimation() }

    return approachLayout(
        isMeasurementApproachInProgress = { false },
        isPlacementApproachInProgress = {
            coordinates?.let { layoutCoordinates ->
                with(lookaheadScope) {
                    animation.updateTarget(
                        // 스크롤 오프셋까지 애니메이션하면 칩이 스크롤을 따라다니므로 motion frame of reference를 제외한 좌표만 사용한다.
                        target = lookaheadScopeCoordinates.localLookaheadPositionOf(sourceCoordinates = layoutCoordinates, includeMotionFrameOfReference = false).round(),
                        coroutineScope = coroutineScope,
                        animationSpec = animationSpec,
                    )
                }
            }

            !animation.isIdle
        },
    ) { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        layout(placeable.width, placeable.height) {
            val position =
                coordinates?.let { layoutCoordinates ->
                    with(lookaheadScope) {
                        lookaheadScopeCoordinates.localPositionOf(sourceCoordinates = layoutCoordinates, relativeToSource = Offset.Zero, includeMotionFrameOfReference = false).round()
                    }
                }

            if (position == null) {
                placeable.place(IntOffset.Zero)
            } else {
                placeable.place((animation.value ?: position) - position)
            }
        }
    }
}

private class PlacementAnimation {
    private var animatable: Animatable<IntOffset, AnimationVector2D>? = null
    private var target: IntOffset? = null
    private var isPending = false
    private var animatedValue by mutableStateOf<IntOffset?>(null)

    val isIdle: Boolean
        get() = !isPending && animatable?.isRunning != true

    val value: IntOffset?
        get() = if (isIdle) null else animatedValue

    fun updateTarget(
        target: IntOffset,
        coroutineScope: CoroutineScope,
        animationSpec: FiniteAnimationSpec<IntOffset>,
    ) {
        if (this.target != null && this.target != target) {
            isPending = true
        }
        this.target = target

        val animatable = animatable ?: Animatable(target, IntOffset.VectorConverter)
        this.animatable = animatable

        if (isPending) {
            isPending = false
            // 시작을 다음 프레임으로 미루면 approach 진행 판정이 갱신되기 전에 배치가 끝나 첫 프레임이 목표 위치로 건너뛴다.
            coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                animatable.animateTo(targetValue = target, animationSpec = animationSpec)
            }
        }

        animatedValue = animatable.value
    }
}

private val DefaultPlacementAnimationSpec =
    spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    )
