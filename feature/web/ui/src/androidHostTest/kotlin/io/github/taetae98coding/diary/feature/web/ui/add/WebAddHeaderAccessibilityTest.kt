package io.github.taetae98coding.diary.feature.web.ui.add

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.feature.web.ui.TEST_TAG_ADD_REQUEST_KEY
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class WebAddHeaderAccessibilityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `기본 환경에서 헤더 입력의 접근성 이름에 항목 순서를 담는다`() {
        setWebAddScreen()
        composeRule.addHeaderRow()
        composeRule.addHeaderRow()

        composeRule.onNodeWithContentDescription(FIRST_NAME_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(FIRST_VALUE_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(SECOND_NAME_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(SECOND_VALUE_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "+ko")
    fun `한국어 환경에서 헤더 입력의 접근성 이름에 항목 순서를 담는다`() {
        setWebAddScreen()
        addKoreanHeaderRow()
        addKoreanHeaderRow()

        composeRule.onNodeWithContentDescription(KOREAN_FIRST_NAME_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_FIRST_VALUE_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_SECOND_NAME_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_SECOND_VALUE_DESCRIPTION).assertExists()
    }

    @Test
    fun `헤더 항목마다 서로 다른 접근성 이름을 제공한다`() {
        setWebAddScreen()
        composeRule.addHeaderRow()
        composeRule.addHeaderRow()

        composeRule.onAllNodesWithContentDescription(FIRST_NAME_DESCRIPTION).assertCountEquals(1)
        composeRule.onAllNodesWithContentDescription(SECOND_NAME_DESCRIPTION).assertCountEquals(1)
    }

    @Test
    fun `헤더 항목을 삭제하면 남은 항목의 순서를 다시 센다`() {
        setWebAddScreen()
        composeRule.addHeaderRow()
        composeRule.addHeaderRow()
        composeRule.onNodeWithContentDescription(SECOND_NAME_DESCRIPTION).performTextInput(TYPED_SECOND_HEADER_NAME)
        composeRule.waitForIdle()

        composeRule.onAllNodesWithContentDescription(DEFAULT_HEADER_REMOVE_BUTTON_DESCRIPTION)[0].performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodesWithContentDescription(SECOND_NAME_DESCRIPTION).assertCountEquals(0)
        composeRule.onNodeWithContentDescription(FIRST_NAME_DESCRIPTION).assert(hasText(TYPED_SECOND_HEADER_NAME))
    }

    @Test
    fun `헤더 이름과 값 입력은 각각 별개의 접근성 대상으로 남는다`() {
        setWebAddScreen()
        composeRule.addHeaderRow()

        composeRule.onNodeWithContentDescription(FIRST_NAME_DESCRIPTION).performTextInput(TYPED_FIRST_HEADER_NAME)
        composeRule.onNodeWithContentDescription(FIRST_VALUE_DESCRIPTION).performTextInput(TYPED_FIRST_HEADER_VALUE)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(FIRST_NAME_DESCRIPTION).assert(hasText(TYPED_FIRST_HEADER_NAME))
        composeRule.onNodeWithContentDescription(FIRST_VALUE_DESCRIPTION).assert(hasText(TYPED_FIRST_HEADER_VALUE))
    }

    private fun addKoreanHeaderRow() {
        composeRule.onNodeWithContentDescription(KOREAN_HEADER_ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun setWebAddScreen() {
        composeRule.setContent {
            WebAddScreenTestTheme {
                WebAddScreen(
                    navigateToTagAdd = {},
                    tagAddRequestKey = TEST_TAG_ADD_REQUEST_KEY,
                    navigateUp = {},
                    componentVisibleProvider = { WebAddScaffoldComponentVisible() },
                    addViewModel = screenTestViewModel(),
                    navigateToTagDetail = {},
                    tagViewModel = addTagScreenTestViewModel(),
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        private const val FIRST_NAME_DESCRIPTION = "Header 1 name"
        private const val FIRST_VALUE_DESCRIPTION = "Header 1 value"
        private const val SECOND_NAME_DESCRIPTION = "Header 2 name"
        private const val SECOND_VALUE_DESCRIPTION = "Header 2 value"
        private const val KOREAN_HEADER_ADD_BUTTON_DESCRIPTION = "헤더 추가"
        private const val KOREAN_FIRST_NAME_DESCRIPTION = "1번째 헤더 이름"
        private const val KOREAN_FIRST_VALUE_DESCRIPTION = "1번째 헤더 값"
        private const val KOREAN_SECOND_NAME_DESCRIPTION = "2번째 헤더 이름"
        private const val KOREAN_SECOND_VALUE_DESCRIPTION = "2번째 헤더 값"
    }
}
