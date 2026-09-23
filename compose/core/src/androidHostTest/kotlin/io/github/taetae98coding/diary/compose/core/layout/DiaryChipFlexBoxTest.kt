package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryChipFlexBoxTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `칩이 한 줄이어도 칩 영역 높이를 유지한다`() {
        setContent(itemCount = 1)

        height(tag = CHIP_FLEX_BOX_TAG) shouldBe CHIP_AREA_HEIGHT
    }

    @Test
    fun `칩이 영역보다 많으면 칩 전체 높이와 위아래 여백만큼 높아진다`() {
        setContent(itemCount = MANY_ITEM_COUNT)

        val chipHeight = bottom(tag = itemTag(index = MANY_ITEM_COUNT - 1)) - top(tag = itemTag(index = 0))

        height(tag = CHIP_FLEX_BOX_TAG) shouldBe chipHeight + SCREEN_VERTICAL_PADDING * 2
        height(tag = CHIP_FLEX_BOX_TAG) shouldBeGreaterThan CHIP_AREA_HEIGHT
    }

    @Test
    fun `칩이 영역보다 많아도 첫 칩 위에 화면 세로 여백이 남는다`() {
        setContent(itemCount = MANY_ITEM_COUNT)

        top(tag = itemTag(index = 0)) - top(tag = CHIP_FLEX_BOX_TAG) shouldBe SCREEN_VERTICAL_PADDING
    }

    @Test
    fun `칩이 영역보다 많아도 마지막 칩 아래에 화면 세로 여백이 남는다`() {
        setContent(itemCount = MANY_ITEM_COUNT)

        bottom(tag = CHIP_FLEX_BOX_TAG) - bottom(tag = itemTag(index = MANY_ITEM_COUNT - 1)) shouldBe SCREEN_VERTICAL_PADDING
    }

    @Test
    fun `칩이 영역보다 많아도 칩 영역은 세로 스크롤을 가져가지 않는다`() {
        setContent(itemCount = MANY_ITEM_COUNT)

        composeRule.onNode(hasScrollAction()).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun `세로로 스크롤하는 부모 안에서는 칩 영역을 밀어도 다른 자리와 같은 만큼 스크롤한다`() {
        val scrollState = setScrollableParentContent()

        val plainScroll = dragUp(state = scrollState, tag = PLAIN_TAG)
        val chipScroll = dragUp(state = scrollState, tag = CHIP_FLEX_BOX_TAG)

        plainScroll shouldBeGreaterThan 0
        chipScroll shouldBe plainScroll
    }

    @Test
    fun `스크롤하는 칩 영역은 칩이 영역보다 많아도 칩 영역 높이를 유지한다`() {
        setScrollableContent(itemCount = MANY_ITEM_COUNT)

        height(tag = CHIP_FLEX_BOX_TAG) shouldBe CHIP_AREA_HEIGHT
    }

    @Test
    fun `스크롤하는 칩 영역은 스크롤하지 않은 상태에서 첫 칩 위에 화면 세로 여백이 남는다`() {
        setScrollableContent(itemCount = MANY_ITEM_COUNT)

        top(tag = itemTag(index = 0)) - top(tag = CHIP_FLEX_BOX_TAG) shouldBe SCREEN_VERTICAL_PADDING
    }

    @Test
    fun `스크롤하는 칩 영역은 끝까지 스크롤하면 마지막 칩 아래에 화면 세로 여백이 남는다`() {
        setScrollableContent(itemCount = MANY_ITEM_COUNT)

        composeRule.onNodeWithTag(CHIP_FLEX_BOX_TAG).performSemanticsAction(SemanticsActions.ScrollBy) { it(0F, SCROLL_TO_END) }

        bottom(tag = CHIP_FLEX_BOX_TAG) - bottom(tag = itemTag(index = MANY_ITEM_COUNT - 1)) shouldBe SCREEN_VERTICAL_PADDING
    }

    private fun setContent(itemCount: Int) {
        composeRule.setContent {
            DiaryTheme {
                Box(modifier = Modifier.width(BOX_WIDTH)) {
                    Items(itemCount = itemCount, modifier = Modifier.testTag(CHIP_FLEX_BOX_TAG))
                }
            }
        }
    }

    private fun setScrollableContent(itemCount: Int) {
        composeRule.setContent {
            DiaryTheme {
                Box(modifier = Modifier.width(BOX_WIDTH)) {
                    ScrollableItems(itemCount = itemCount, modifier = Modifier.testTag(CHIP_FLEX_BOX_TAG))
                }
            }
        }
    }

    private fun setScrollableParentContent(): ScrollState {
        lateinit var scrollState: ScrollState

        composeRule.setContent {
            DiaryTheme {
                scrollState = rememberScrollState()

                Column(
                    modifier =
                        Modifier
                            .width(BOX_WIDTH)
                            .height(PARENT_HEIGHT)
                            .verticalScroll(scrollState),
                ) {
                    Items(itemCount = MANY_ITEM_COUNT, modifier = Modifier.testTag(CHIP_FLEX_BOX_TAG))
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(CHIP_AREA_HEIGHT)
                                .testTag(PLAIN_TAG),
                    )
                    Box(modifier = Modifier.height(PARENT_HEIGHT))
                }
            }
        }

        return scrollState
    }

    /**
     * [tag]가 가리키는 자리를 위로 끌어 부모가 스크롤한 값을 돌려준다. 끌기마다 같은 조건에서 비교하도록 스크롤을 처음으로 되돌린 뒤 끈다.
     */
    private fun dragUp(
        state: ScrollState,
        tag: String,
    ): Int {
        composeRule.runOnIdle { state.dispatchRawDelta(-state.value.toFloat()) }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(tag).performTouchInput {
            down(Offset(x = centerX, y = height - 1F))
            moveBy(Offset(x = 0F, y = -DRAG_DISTANCE))
            up()
        }
        composeRule.waitForIdle()

        return state.value
    }

    @Composable
    private fun Items(
        itemCount: Int,
        modifier: Modifier = Modifier,
    ) {
        DiaryChipFlexBox(modifier = modifier) {
            Chips(itemCount = itemCount)
        }
    }

    @Composable
    private fun ScrollableItems(
        itemCount: Int,
        modifier: Modifier = Modifier,
    ) {
        DiaryScrollableChipFlexBox(modifier = modifier) {
            Chips(itemCount = itemCount)
        }
    }

    @Composable
    private fun Chips(itemCount: Int) {
        repeat(itemCount) { index ->
            Box(modifier = Modifier.size(width = ITEM_WIDTH, height = ITEM_HEIGHT).testTag(itemTag(index = index)))
        }
    }

    private fun height(tag: String): Dp = bottom(tag = tag) - top(tag = tag)

    private fun top(tag: String): Dp = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot().top

    private fun bottom(tag: String): Dp = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot().bottom

    private companion object {
        // 여러 줄로 넘어가 칩 전체 높이가 칩 영역 높이보다 커지는 개수다.
        const val MANY_ITEM_COUNT = 12

        // 스크롤 끝까지 이동하기 위해 칩 영역보다 충분히 큰 값이다.
        const val SCROLL_TO_END = 10_000F

        // 터치 슬롭을 넘겨 부모가 실제로 스크롤하는 거리다.
        const val DRAG_DISTANCE = 100F

        val CHIP_AREA_HEIGHT = 150.dp
        val SCREEN_VERTICAL_PADDING = 16.dp

        val BOX_WIDTH = 200.dp
        val ITEM_WIDTH = 60.dp
        val ITEM_HEIGHT = 40.dp

        // 칩 영역과 비교할 자리를 함께 보여 주면서 스크롤이 남는 높이다.
        val PARENT_HEIGHT = 600.dp

        const val CHIP_FLEX_BOX_TAG = "chipFlexBox"
        const val PLAIN_TAG = "plain"

        fun itemTag(index: Int): String = "item$index"
    }
}
