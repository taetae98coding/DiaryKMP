package io.github.taetae98coding.diary.compose.tag.primary

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrimaryTagInputEmojiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-37caa7be-6e26-4826-b112-c458b7015cf4 태그 칩에 이모지와 제목을 함께 표시한다`() {
        val tagList =
            listOf(
                Arb.tag(title = WORK_TAG_TITLE, isFinished = false).next().let { tag -> tag.copy(detail = tag.detail.copy(emoji = TAG_EMOJI)) },
                Arb.tag(title = EXERCISE_TAG_TITLE, isFinished = false).next(),
            )

        composeRule.setPrimaryTagInput(uiState = PrimaryTagInputUiState(selectedTagList = tagList))

        composeRule.onNodeWithText("$TAG_EMOJI $WORK_TAG_TITLE").assertExists()
        composeRule.onNodeWithText(EXERCISE_TAG_TITLE).assertExists()
        composeRule.onNodeWithText(" $EXERCISE_TAG_TITLE").assertDoesNotExist()
    }

    @Test
    fun `TC-37caa7be-6e26-4826-b112-c458b7015cf4 태그 선택 목록에 이모지와 제목을 함께 표시한다`() {
        val tagList =
            listOf(
                Arb.tag(title = WORK_TAG_TITLE, isFinished = false).next().let { tag -> tag.copy(detail = tag.detail.copy(emoji = TAG_EMOJI)) },
                Arb.tag(title = EXERCISE_TAG_TITLE, isFinished = false).next(),
            )

        composeRule.setPrimaryTagPickerDialog(tagList = tagList)

        composeRule.dialogNodeWithText("$TAG_EMOJI $WORK_TAG_TITLE").assertExists()
        composeRule.dialogNodeWithText(EXERCISE_TAG_TITLE).assertExists()
    }

    public companion object {
        private const val TAG_EMOJI = "🏃"
    }
}
