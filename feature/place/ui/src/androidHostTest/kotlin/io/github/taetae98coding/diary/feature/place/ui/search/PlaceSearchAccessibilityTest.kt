package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceSearchAccessibilityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `기본 환경에서 검색어 입력과 지우기 버튼의 접근성 이름을 제공한다`() {
        composeRule.setPlaceSearchContent()

        composeRule.onNodeWithContentDescription(DEFAULT_QUERY_INPUT_DESCRIPTION).performTextInput(TYPED_QUERY)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 검색어 입력과 지우기 버튼의 접근성 이름을 제공한다`() {
        composeRule.setPlaceSearchContent()

        composeRule.onNodeWithContentDescription(KOREAN_QUERY_INPUT_DESCRIPTION).performTextInput(TYPED_QUERY)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(KOREAN_CLEAR_BUTTON_DESCRIPTION).assertExists()
    }

    @Test
    fun `검색어 입력의 접근성 이름은 자리 표시 문구와 별개로 제공된다`() {
        composeRule.setPlaceSearchContent()

        composeRule.onNodeWithText(DEFAULT_QUERY_PLACEHOLDER).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_QUERY_INPUT_DESCRIPTION).assertExists()
    }

    @Test
    fun `검색어가 비어 있으면 지우기 버튼을 표시하지 않는다`() {
        composeRule.setPlaceSearchContent()

        composeRule.onNodeWithContentDescription(DEFAULT_CLEAR_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    private companion object {
        private const val TYPED_QUERY = "PlaceSearchQuery"
    }
}
