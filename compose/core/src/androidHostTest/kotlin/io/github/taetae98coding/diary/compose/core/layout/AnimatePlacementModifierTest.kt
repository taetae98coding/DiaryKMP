package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AnimatePlacementModifierTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `앞 요소의 크기가 커지면 뒤 요소는 새 위치로 건너뛰지 않고 이동한다`() {
        var isWide by mutableStateOf(false)

        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            LookaheadScope {
                Row {
                    Box(modifier = Modifier.width(if (isWide) WIDE_WIDTH else NARROW_WIDTH).height(SIZE))
                    Box(
                        modifier =
                            Modifier
                                .animatePlacement(lookaheadScope = this@LookaheadScope)
                                .size(SIZE)
                                .testTag(TAG),
                    )
                }
            }
        }
        composeRule.mainClock.advanceTimeByFrame()

        assertEquals(NARROW_WIDTH, left())

        isWide = true
        val movingTrace = List(MOVING_FRAME_COUNT) { composeRule.mainClock.advanceTimeByFrame().let { left() } }
        val moving = movingTrace.last()

        assertTrue(moving > NARROW_WIDTH, "이동을 시작하지 않았다. $movingTrace")
        assertTrue(moving < WIDE_WIDTH, "새 위치로 건너뛰었다. $movingTrace")

        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        assertEquals(WIDE_WIDTH, left())
    }

    private fun left(): Dp = composeRule.onNodeWithTag(TAG).getUnclippedBoundsInRoot().left

    private companion object {
        const val TAG = "target"
        const val MOVING_FRAME_COUNT = 5

        val SIZE = 50.dp
        val NARROW_WIDTH = 100.dp
        val WIDE_WIDTH = 200.dp
    }
}
