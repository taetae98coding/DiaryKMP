package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoGeminiButtonTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-003 설정을 확인하지 못하면 시작 동작을 표시하지 않는다`() {
        setButton(isVisible = false)

        composeRule.onNodeWithContentDescription(DEFAULT_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-001 설정을 확인하면 시작 동작을 표시한다`() {
        setButton(isVisible = true)

        composeRule.onNodeWithContentDescription(DEFAULT_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-MEMO-GEMINI-FEATURE-002 설정이 갖춰지지 않아도 누를 수 있다`() {
        var clickCount = 0
        setButton(isVisible = true, onClick = { clickCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        clickCount shouldBe 1
    }

    private fun setButton(
        isVisible: Boolean,
        onClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MemoGeminiButton(
                    onClick = onClick,
                    isVisibleProvider = { isVisible },
                )
            }
        }
    }

    private companion object {
        private const val DEFAULT_BUTTON_DESCRIPTION = "Writing assistant"
    }
}
