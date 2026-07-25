package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryTitleInputStateFactoryTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `rememberDiaryTitleInputState는 초기 텍스트로 상태를 만든다`() {
        lateinit var state: DiaryTitleInputState
        composeRule.setContent {
            state = rememberDiaryTitleInputState(initialText = INITIAL_TEXT)
        }

        composeRule.runOnIdle {
            state.text.toString() shouldBe INITIAL_TEXT
        }
    }

    @Test
    fun `requestFocus를 호출하면 제목 입력 칸에 초점이 맞춰진다`() {
        lateinit var state: DiaryTitleInputState
        composeRule.setContent {
            DiaryTheme {
                state = rememberDiaryTitleInputState()
                DiaryTitleInput(state = state)
            }
        }

        composeRule.runOnIdle { state.requestFocus() }

        composeRule.onNode(hasSetTextAction()).assertIsFocused()
    }

    @Test
    fun `화면 재생성 후에도 입력한 텍스트를 복원한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var state: DiaryTitleInputState
        restorationTester.setContent {
            state = rememberDiaryTitleInputState()
        }
        composeRule.runOnIdle { state.setText(RESTORED_TEXT) }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            state.text.toString() shouldBe RESTORED_TEXT
        }
    }

    public companion object {
        private const val INITIAL_TEXT = "DiaryTitleInputInitial"
        private const val RESTORED_TEXT = "DiaryTitleInputRestored"
    }
}
