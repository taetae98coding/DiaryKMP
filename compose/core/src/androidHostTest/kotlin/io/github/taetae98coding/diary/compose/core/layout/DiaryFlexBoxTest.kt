package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryFlexBoxTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `줄이 여러 개여도 첫 줄을 최상단에 배치한다`() {
        composeRule.setContent {
            Box(modifier = Modifier.width(BOX_WIDTH)) {
                Items(modifier = Modifier.testTag(FLEX_BOX_TAG))
            }
        }

        top(tag = itemTag(index = 0)) shouldBe top(tag = FLEX_BOX_TAG)
    }

    @Test
    fun `줄 전체 높이가 스크롤 영역보다 크면 첫 줄과 마지막 줄을 모두 볼 수 있다`() {
        composeRule.setContent {
            Box(modifier = Modifier.width(BOX_WIDTH)) {
                Items(
                    modifier =
                        Modifier
                            .testTag(FLEX_BOX_TAG)
                            .height(SCROLL_AREA_HEIGHT)
                            .verticalScroll(rememberScrollState()),
                )
            }
        }

        top(tag = itemTag(index = 0)) shouldBe top(tag = FLEX_BOX_TAG)
        composeRule.onNodeWithTag(itemTag(index = 0)).assertIsDisplayed()

        composeRule.onNodeWithTag(itemTag(index = ITEM_COUNT - 1)).performScrollTo()

        composeRule.onNodeWithTag(itemTag(index = ITEM_COUNT - 1)).assertIsDisplayed()
    }

    @Composable
    private fun Items(modifier: Modifier = Modifier) {
        DiaryFlexBox(modifier = modifier) {
            repeat(ITEM_COUNT) { index ->
                Box(modifier = Modifier.size(width = ITEM_WIDTH, height = ITEM_HEIGHT).testTag(itemTag(index = index)))
            }
        }
    }

    private fun top(tag: String): Dp = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot().top

    private companion object {
        // 한 줄에 세 개만 들어가 세 줄로 넘어가고, 줄 전체 높이가 스크롤 영역보다 커지는 크기다.
        const val ITEM_COUNT = 9

        val BOX_WIDTH = 200.dp
        val ITEM_WIDTH = 60.dp
        val ITEM_HEIGHT = 20.dp
        val SCROLL_AREA_HEIGHT = 40.dp

        const val FLEX_BOX_TAG = "flexBox"

        fun itemTag(index: Int): String = "item$index"
    }
}
