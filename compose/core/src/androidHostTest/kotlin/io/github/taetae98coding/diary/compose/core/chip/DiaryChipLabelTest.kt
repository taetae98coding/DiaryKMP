package io.github.taetae98coding.diary.compose.core.chip

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.color.DiaryColorIndicator
import io.github.taetae98coding.diary.compose.core.layout.DiaryChipFlexBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric의 글자 측정은 폭이 좁아도 줄바꿈을 만들지 않으므로, 이름이 한 줄을 유지하는지는 여기서 검증할 수 없고 칩이 차지하는 폭만 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryChipLabelTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `이름이 길어도 칩은 칩 영역에서 쓸 수 있는 폭을 넘지 않는다`() {
        setContent()

        width(tag = LONG_ASSIST_TAG) shouldBeLessThanOrEqualTo CHIP_AREA_WIDTH
        width(tag = LONG_FILTER_TAG) shouldBeLessThanOrEqualTo CHIP_AREA_WIDTH
    }

    @Test
    fun `이름이 길어도 칩은 칩 영역 안에서 시작하고 끝난다`() {
        setContent()

        listOf(LONG_ASSIST_TAG, LONG_FILTER_TAG).forEach { tag ->
            val bounds = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot()
            val areaBounds = composeRule.onNodeWithTag(CHIP_FLEX_BOX_TAG).getUnclippedBoundsInRoot()

            bounds.left - areaBounds.left shouldBeGreaterThanOrEqualTo SCREEN_HORIZONTAL_PADDING
            areaBounds.right - bounds.right shouldBeGreaterThanOrEqualTo SCREEN_HORIZONTAL_PADDING
        }
    }

    @Test
    fun `이름 앞에 표시를 두면 칩이 그 표시 폭만큼 넓어진다`() {
        setContent()

        // 칩이 자식 노드를 하나로 합쳐 보여 주므로 칩 안의 표시는 합치지 않은 트리에서 찾는다.
        val indicatorBounds = composeRule.onNodeWithTag(COLOR_INDICATOR_TAG, useUnmergedTree = true).getUnclippedBoundsInRoot()

        width(tag = LEADING_ASSIST_TAG) - width(tag = PLAIN_ASSIST_TAG) shouldBe indicatorBounds.right - indicatorBounds.left
    }

    private fun setContent() {
        composeRule.setContent {
            DiaryTheme {
                Box(modifier = Modifier.width(BOX_WIDTH)) {
                    Chips(modifier = Modifier.testTag(CHIP_FLEX_BOX_TAG))
                }
            }
        }
    }

    @Composable
    private fun Chips(modifier: Modifier = Modifier) {
        DiaryChipFlexBox(modifier = modifier) {
            DiaryAssistChip(label = SHORT_LABEL, onClick = {}, modifier = Modifier.testTag(PLAIN_ASSIST_TAG))
            DiaryAssistChip(
                label = SHORT_LABEL,
                onClick = {},
                modifier = Modifier.testTag(LEADING_ASSIST_TAG),
                leadingIcon = { DiaryColorIndicator(color = Color.Red, modifier = Modifier.testTag(COLOR_INDICATOR_TAG)) },
            )
            DiaryAssistChip(label = LONG_LABEL, onClick = {}, modifier = Modifier.testTag(LONG_ASSIST_TAG))
            DiaryFilterChip(label = SHORT_LABEL, onClick = {})
            DiaryFilterChip(label = LONG_LABEL, onClick = {}, modifier = Modifier.testTag(LONG_FILTER_TAG))
        }
    }

    private fun width(tag: String): Dp = composeRule.onNodeWithTag(tag).getUnclippedBoundsInRoot().let { it.right - it.left }

    private companion object {
        const val SHORT_LABEL = "업무"

        // 칩 영역에서 쓸 수 있는 폭을 확실히 넘는 길이다.
        val LONG_LABEL = "아주 긴 제목을 가진 태그 이름이 여기에 들어간다 ".repeat(20)

        val BOX_WIDTH = 360.dp

        // 공통 여백과 간격 디자인(docs/design/dimens.md)이 정한 값이다.
        val SCREEN_HORIZONTAL_PADDING = 16.dp

        val CHIP_AREA_WIDTH = BOX_WIDTH - SCREEN_HORIZONTAL_PADDING * 2

        const val CHIP_FLEX_BOX_TAG = "chipFlexBox"
        const val PLAIN_ASSIST_TAG = "plainAssist"
        const val LEADING_ASSIST_TAG = "leadingAssist"
        const val COLOR_INDICATOR_TAG = "colorIndicator"
        const val LONG_ASSIST_TAG = "longAssist"
        const val LONG_FILTER_TAG = "longFilter"
    }
}
