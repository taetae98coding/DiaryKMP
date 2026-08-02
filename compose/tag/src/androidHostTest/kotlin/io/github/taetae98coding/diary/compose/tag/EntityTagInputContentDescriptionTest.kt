package io.github.taetae98coding.diary.compose.tag

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko")
class EntityTagInputKoreanTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `한국어 환경에서 태그 추가 칩과 태그 상세 이동 동작 이름을 한국어로 표시한다`() {
        val tag = entityTestTag(title = WORK_TAG_TITLE)

        composeRule.setEntityTagInput(uiState = EntityTagInputUiState(tagList = listOf(tag)))

        composeRule.onNodeWithText(KOREAN_ENTITY_TAG_LABEL).assertExists()
        composeRule.onNode(hasClickLabel(KOREAN_ENTITY_TAG_LABEL)).assertExists()
        composeRule.onNode(hasClickLabel(KOREAN_TAG_DETAIL_ACTION)).assertExists()
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EntityTagInputDefaultLocaleTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `그 외 기본 환경에서 태그 추가 칩과 태그 상세 이동 동작 이름을 기본 문구로 표시한다`() {
        val tag = entityTestTag(title = WORK_TAG_TITLE)

        composeRule.setEntityTagInput(uiState = EntityTagInputUiState(tagList = listOf(tag)))

        composeRule.onNodeWithText(DEFAULT_ENTITY_TAG_LABEL).assertExists()
        composeRule.onNode(hasClickLabel(DEFAULT_ENTITY_TAG_LABEL)).assertExists()
        composeRule.onNode(hasClickLabel(DEFAULT_TAG_DETAIL_ACTION)).assertExists()
    }
}
