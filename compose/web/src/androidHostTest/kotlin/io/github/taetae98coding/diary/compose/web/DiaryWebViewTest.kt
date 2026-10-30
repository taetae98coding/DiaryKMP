package io.github.taetae98coding.diary.compose.web

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val URL = "https://example.com"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryWebViewTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `진행 표시 없이 웹 표시 수단이 주소를 연다`() {
        composeRule.setContent {
            DiaryTheme {
                DiaryWebView(url = URL)
            }
        }

        composeRule.onNodeWithTag(DIARY_WEB_VIEW_TEST_TAG).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }
}
