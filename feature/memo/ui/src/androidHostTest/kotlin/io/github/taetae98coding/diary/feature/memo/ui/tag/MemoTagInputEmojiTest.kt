package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoTagInputEmojiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-023 태그 칩에 이모지와 제목을 함께 표시한다`() {
        val tagList =
            listOf(
                testTag(title = WORK_TAG_TITLE, emoji = TAG_EMOJI),
                testTag(title = EXERCISE_TAG_TITLE),
            )

        composeRule.setMemoTagInput(uiState = MemoTagInputUiState(selectedTagList = tagList))

        composeRule.onNodeWithText("$TAG_EMOJI $WORK_TAG_TITLE").assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(" $EXERCISE_TAG_TITLE").assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-TAG-INPUT-FEATURE-023 태그 선택 목록에 이모지와 제목을 함께 표시한다`() {
        val tagList =
            listOf(
                testTag(title = WORK_TAG_TITLE, emoji = TAG_EMOJI),
                testTag(title = EXERCISE_TAG_TITLE),
            )

        composeRule.setMemoTagPickerDialog(tagList = tagList)

        composeRule.dialogNodeWithText("$TAG_EMOJI $WORK_TAG_TITLE").assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    public companion object {
        private const val TAG_EMOJI = "🏃"
    }
}
