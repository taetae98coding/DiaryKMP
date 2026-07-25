package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.height
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryEmojiInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `이모지가 비어 있으면 칸 가운데에 라벨을 표시한다`() {
        setDiaryEmojiInput()

        composeRule.emojiInput().assert(hasText(DEFAULT_LABEL))
    }

    @Test
    fun `이모지가 있으면 칸에 라벨 대신 이모지를 표시한다`() {
        setDiaryEmojiInput(initialText = RUNNER)

        composeRule.emojiInput().assert(hasText(RUNNER))
        composeRule.emojiInput().assert(!hasText(DEFAULT_LABEL))
    }

    @Test
    fun `이모지 칸은 가로와 세로가 같고 제목 입력과 높이가 같다`() {
        setDiaryEmojiInput(initialText = RUNNER)

        val titleHeight = composeRule.titleInput().getUnclippedBoundsInRoot().height

        composeRule.emojiInput().assertHeightIsEqualTo(titleHeight)
        composeRule.emojiInput().assertWidthIsEqualTo(titleHeight)
    }

    @Test
    fun `칸을 누르면 현재 이모지가 채워진 다이얼로그가 열린다`() {
        setDiaryEmojiInput(initialText = RUNNER)

        composeRule.emojiInput().performClick()
        composeRule.waitForIdle()

        composeRule.dialogInput().assert(hasText(RUNNER))
        composeRule.onNodeWithText(DEFAULT_CANCEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
    }

    @Test
    fun `다이얼로그가 열리면 입력 칸에 초점이 맞춰진다`() {
        setDiaryEmojiInput()

        composeRule.emojiInput().performClick()
        composeRule.waitForIdle()

        composeRule.dialogInput().assertIsFocused()
    }

    @Test
    fun `확인을 누르면 다이얼로그가 닫히고 입력한 이모지를 칸에 반영한다`() {
        setDiaryEmojiInput(initialText = RUNNER)

        composeRule.emojiInput().performClick()
        composeRule.dialogInput().performTextReplacement(SWIMMER)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
        composeRule.emojiInput().assert(hasText(SWIMMER))
    }

    @Test
    fun `이모지가 있는 입력 칸에 이어서 입력하면 새 이모지로 대체한다`() {
        setDiaryEmojiInput(initialText = RUNNER)

        composeRule.emojiInput().performClick()
        composeRule.dialogInput().performTextInput(SWIMMER)
        composeRule.waitForIdle()

        composeRule.dialogInput().assert(hasText(SWIMMER))

        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        composeRule.emojiInput().assert(hasText(SWIMMER))
    }

    @Test
    fun `입력을 비운 채 확인하면 칸의 이모지를 비운다`() {
        setDiaryEmojiInput(initialText = RUNNER)

        composeRule.emojiInput().performClick()
        composeRule.dialogInput().performTextReplacement("")
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        composeRule.emojiInput().assert(hasText(DEFAULT_LABEL))
    }

    @Test
    fun `취소하면 칸의 이모지를 그대로 둔다`() {
        setDiaryEmojiInput(initialText = RUNNER)

        composeRule.emojiInput().performClick()
        composeRule.dialogInput().performTextReplacement(SWIMMER)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CANCEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CANCEL).assertDoesNotExist()
        composeRule.emojiInput().assert(hasText(RUNNER))
    }

    @Test
    fun `화면이 재생성되어도 반영된 이모지를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        setDiaryEmojiInput(restorationTester = restorationTester, initialText = RUNNER)

        composeRule.emojiInput().performClick()
        composeRule.dialogInput().performTextReplacement(SWIMMER)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.emojiInput().assert(hasText(SWIMMER))
    }

    @Test
    fun `화면이 재생성되어도 열린 다이얼로그와 편집 중인 문자를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        setDiaryEmojiInput(restorationTester = restorationTester, initialText = RUNNER)

        composeRule.emojiInput().performClick()
        composeRule.dialogInput().performTextReplacement(SWIMMER)
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.dialogInput().assert(hasText(SWIMMER))
    }

    private fun setDiaryEmojiInput(
        restorationTester: StateRestorationTester? = null,
        initialText: String = "",
    ) {
        val content: @Composable () -> Unit = {
            DiaryEmojiInputTestContent {
                DiaryEmojiInputTestRow(state = rememberDiaryEmojiInputState(initialText = initialText))
            }
        }

        if (restorationTester == null) {
            composeRule.setContent(content)
        } else {
            restorationTester.setContent(content)
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryEmojiInputTextTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `기본 환경에서는 다이얼로그 문구를 영어로 표시한다`() {
        assertDialogText(title = DEFAULT_LABEL, cancel = DEFAULT_CANCEL, confirm = DEFAULT_CONFIRM)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서는 다이얼로그 문구를 한국어로 표시한다`() {
        assertDialogText(title = KOREAN_LABEL, cancel = KOREAN_CANCEL, confirm = KOREAN_CONFIRM)
    }

    private fun assertDialogText(
        title: String,
        cancel: String,
        confirm: String,
    ) {
        composeRule.setContent {
            DiaryEmojiInputTestContent {
                DiaryEmojiInputTestRow(state = rememberDiaryEmojiInputState(initialText = RUNNER))
            }
        }

        composeRule.emojiInput().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(title).assertExists()
        composeRule.onNodeWithText(cancel).assertExists()
        composeRule.onNodeWithText(confirm).assertExists()
    }
}
