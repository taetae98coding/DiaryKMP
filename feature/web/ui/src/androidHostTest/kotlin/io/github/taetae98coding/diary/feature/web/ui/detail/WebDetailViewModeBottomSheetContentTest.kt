package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WebDetailViewModeBottomSheetContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `표시 방식 줄을 누르는 대상은 Bottom Sheet의 좌우 끝까지 채운다`() {
        setContent()

        val contentBounds = composeRule.onNodeWithTag(CONTENT_TEST_TAG).getBoundsInRoot()
        val rowBounds = composeRule.onNodeWithText(DEFAULT_RESPONSE_LABEL).getBoundsInRoot()

        rowBounds.left shouldBe contentBounds.left
        rowBounds.right shouldBe contentBounds.right
    }

    @Test
    fun `표시 방식 줄의 내용은 누르는 대상보다 안쪽에 놓인다`() {
        setContent()

        val rowBounds = composeRule.onNodeWithText(DEFAULT_RESPONSE_LABEL).getBoundsInRoot()
        val labelBounds = composeRule.onNodeWithText(DEFAULT_RESPONSE_LABEL, useUnmergedTree = true).getBoundsInRoot()

        labelBounds.left shouldBeGreaterThan rowBounds.left
        labelBounds.right shouldBeLessThan rowBounds.right
    }

    private fun setContent() {
        composeRule.setContent {
            DiaryTheme {
                WebDetailViewModeBottomSheetContent(
                    onEvent = {},
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(CONTENT_TEST_TAG),
                )
            }
        }
    }

    private companion object {
        private const val CONTENT_TEST_TAG = "WebDetailViewModeBottomSheetContent"
        private const val DEFAULT_RESPONSE_LABEL = "Response body mode"
    }
}
