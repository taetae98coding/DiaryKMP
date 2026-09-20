package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
class DiaryNavigateUpTopBarTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `제목을 표시하고 뒤로가기를 누르면 콜백을 한 번 호출한다`() {
        var navigateUpCount = 0
        composeRule.setContent {
            DiaryTheme {
                DiaryNavigateUpTopBar(
                    title = TITLE,
                    onNavigateUp = { navigateUpCount += 1 },
                    navigateUpContentDescription = NAVIGATE_UP,
                )
            }
        }

        composeRule.onNodeWithText(TITLE).assertExists()
        composeRule.onNodeWithContentDescription(NAVIGATE_UP).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `뒤로가기 표시 여부가 바뀌면 버튼이 나타나고 사라진다`() {
        var isVisible by mutableStateOf(false)
        composeRule.setContent {
            DiaryTheme {
                DiaryNavigateUpTopBar(
                    title = TITLE,
                    onNavigateUp = {},
                    navigateUpContentDescription = NAVIGATE_UP,
                    isNavigateUpVisibleProvider = { isVisible },
                )
            }
        }

        composeRule.onNodeWithContentDescription(NAVIGATE_UP).assertDoesNotExist()

        composeRule.runOnIdle { isVisible = true }

        composeRule.onNodeWithContentDescription(NAVIGATE_UP).assertExists()
    }

    @Test
    fun `동작 슬롯에 넘긴 내용을 끝 쪽에 표시한다`() {
        composeRule.setContent {
            DiaryTheme {
                DiaryNavigateUpTopBar(
                    title = TITLE,
                    onNavigateUp = {},
                    navigateUpContentDescription = NAVIGATE_UP,
                    actions = { Text(text = ACTION) },
                )
            }
        }

        composeRule.onNodeWithText(ACTION).assertExists()
        composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size shouldBe 1
    }

    private companion object {
        const val TITLE = "제목"
        const val NAVIGATE_UP = "뒤로가기"
        const val ACTION = "동작"
    }
}
