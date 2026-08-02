package io.github.taetae98coding.diary.feature.tag.ui.add

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.form.rememberTagAddFormState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagAddScaffoldEmojiInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-ADD-FEATURE-014 이모지 입력에는 문자 하나만 남는다`() {
        setTagAddScaffold()

        listOf(
            RUNNER to RUNNER,
            MAN_RUNNING to MAN_RUNNING,
            RUNNER + SWIMMER to SWIMMER,
            KOREA_FLAG + JAPAN_FLAG to JAPAN_FLAG,
            "abc" to "c",
        ).forEach { (input, expected) ->
            composeRule.emojiInput().performClick()
            composeRule.waitForIdle()
            composeRule.emojiDialogInput().performTextReplacement(input)
            composeRule.waitForIdle()

            composeRule
                .emojiDialogInput()
                .fetchSemanticsNode()
                .config[SemanticsProperties.EditableText]
                .text shouldBe expected

            composeRule.onNodeWithText(DEFAULT_EMOJI_CONFIRM).performClick()
            composeRule.waitForIdle()

            composeRule.emojiInput().assert(hasText(expected))
        }
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-024 이모지 입력에 문자가 있으면 새로 입력한 문자로 대체된다`() {
        setTagAddScaffold()

        composeRule.emojiInput().performClick()
        composeRule.waitForIdle()
        composeRule.emojiDialogInput().performTextReplacement(RUNNER)
        composeRule.waitForIdle()
        composeRule.emojiDialogInput().performTextInput(SWIMMER)
        composeRule.waitForIdle()

        composeRule
            .emojiDialogInput()
            .fetchSemanticsNode()
            .config[SemanticsProperties.EditableText]
            .text shouldBe SWIMMER

        composeRule.onNodeWithText(DEFAULT_EMOJI_CONFIRM).performClick()
        composeRule.waitForIdle()

        composeRule.emojiInput().assert(hasText(SWIMMER))
    }

    @Test
    fun `TC-TAG-ADD-FEATURE-014 기본 환경에서 이모지 입력 라벨은 Emoji이다`() {
        setTagAddScaffold()

        composeRule.onNodeWithText(DEFAULT_EMOJI_LABEL).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-ADD-FEATURE-014 한국어 환경에서 이모지 입력 라벨은 이모지이다`() {
        setTagAddScaffold()

        composeRule.onNodeWithText(KOREAN_EMOJI_LABEL).assertExists()
    }

    private fun setTagAddScaffold() {
        composeRule.setContent {
            DiaryTheme {
                TagAddScaffold(
                    state = rememberTagAddFormState(initialColor = Color.Red),
                    uiStateProvider = { TagAddUiState() },
                    onEvent = {},
                    onLinkPickerEvent = {},
                )
            }
        }
    }

    public companion object {
        private const val KOREAN_EMOJI_LABEL = "이모지"
        private const val RUNNER = "🏃"
        private const val SWIMMER = "🏊"
        private const val MAN_RUNNING = "🏃‍♂️"
        private const val KOREA_FLAG = "🇰🇷"
        private const val JAPAN_FLAG = "🇯🇵"
    }
}
