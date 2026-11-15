package io.github.taetae98coding.diary.compose.core.share

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ShareActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `공유 버튼을 누르면 동작을 한 번 실행한다`() {
        var clickCount = 0
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(title = {}, actions = { ShareAction(onClick = { clickCount += 1 }) })
            }
        }

        composeRule.onNodeWithContentDescription(SHARE_CONTENT_DESCRIPTION).assertIsEnabled().performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 1
    }

    @Test
    fun `비활성 공유 버튼은 누를 수 없다`() {
        var clickCount = 0
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(title = {}, actions = { ShareAction(onClick = { clickCount += 1 }, isEnabled = false) })
            }
        }

        composeRule.onNodeWithContentDescription(SHARE_CONTENT_DESCRIPTION).assertIsNotEnabled().performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 0
    }

    @Config(qualifiers = "ko")
    @Test
    fun `한국어에서는 공유 버튼 이름이 공유다`() {
        composeRule.setContent {
            DiaryTheme {
                DiaryTopBar(title = {}, actions = { ShareAction(onClick = {}) })
            }
        }

        composeRule.onNodeWithContentDescription("공유").assertIsEnabled()
    }
}

private const val SHARE_CONTENT_DESCRIPTION = "Share"
