package io.github.taetae98coding.diary.feature.checklist.ui.card

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.feature.checklist.ui.testChecklistSummary
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ChecklistCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `카드 폭보다 긴 진행 글은 전체 글을 접근성에 제공하고 시간이 지나면 그려지는 위치가 바뀐다`() {
        val summary = testChecklistSummary(checkedCount = Int.MAX_VALUE, totalCount = Int.MAX_VALUE)
        val progressText = "${summary.checkedCount}/${summary.totalCount}"
        composeRule.mainClock.autoAdvance = false
        setCard(summary)
        composeRule.mainClock.advanceTimeByFrame()

        val first = captureProgressText(progressText)
        val sameTime = captureProgressText(progressText)
        composeRule.mainClock.advanceTimeBy(MARQUEE_MOVE_MILLIS)
        val later = captureProgressText(progressText)

        sameTime.contentEquals(first) shouldBe true
        later.contentEquals(first) shouldBe false
    }

    private fun captureProgressText(progressText: String): IntArray {
        val pixelMap =
            composeRule
                .onNodeWithText(text = progressText, useUnmergedTree = true)
                .captureToImage()
                .toPixelMap()
        return IntArray(pixelMap.width * pixelMap.height) { index -> pixelMap[index % pixelMap.width, index / pixelMap.width].toArgb() }
    }

    private fun setCard(summary: ChecklistSummary) {
        composeRule.setContent {
            DiaryTheme {
                ChecklistCard(
                    onClick = {},
                    modifier = Modifier.width(NARROW_CARD_WIDTH),
                    summary = summary,
                )
            }
        }
    }

    private companion object {
        private val NARROW_CARD_WIDTH = 72.dp

        // 반복 이동은 처음 1.2초를 기다린 뒤 시작하므로 그보다 충분히 뒤의 화면을 비교한다.
        private const val MARQUEE_MOVE_MILLIS = 3_000L
    }
}
