package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.LocalRetainedValuesStoreProvider
import androidx.compose.runtime.retain.ManagedRetainedValuesStore
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
    fun `TC-TITLE-INPUT-DOMAIN-003 화면 재생성 후에도 입력한 텍스트를 복원한다`() {
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

    @Test
    fun `TC-TITLE-INPUT-DOMAIN-001 진입할 때 초점을 두도록 정한 화면이 보이면 제목 입력에 초점이 놓인다`() {
        setFocusEffectContent(store = ManagedRetainedValuesStore(), isShown = { true })

        composeRule.onNode(hasSetTextAction() and hasText(TITLE_LABEL)).assertIsFocused()
    }

    @Test
    fun `TC-TITLE-INPUT-DOMAIN-002 화면이 회전해도 제목 입력으로 초점을 다시 옮기지 않는다`() {
        val store = ManagedRetainedValuesStore()
        var isShown by mutableStateOf(true)
        setFocusEffectContent(store = store, isShown = { isShown })
        composeRule.onNode(hasSetTextAction() and hasText(TITLE_LABEL)).assertIsFocused()
        composeRule.onNodeWithTag(OTHER_TEST_TAG).performClick()
        composeRule.onNodeWithTag(OTHER_TEST_TAG).assertIsFocused()

        isShown = false
        composeRule.waitForIdle()
        isShown = true
        composeRule.waitForIdle()

        composeRule.onNode(hasSetTextAction() and hasText(TITLE_LABEL)).assertIsNotFocused()
    }

    private fun setFocusEffectContent(
        store: ManagedRetainedValuesStore,
        isShown: () -> Boolean,
    ) {
        composeRule.setContent {
            DiaryTheme {
                if (isShown()) {
                    LocalRetainedValuesStoreProvider(store = store) {
                        val state = rememberDiaryTitleInputState()
                        DiaryTitleInputFocusEffect(state = state)
                        Column {
                            DiaryTitleInput(state = state)
                            TextField(
                                state = rememberTextFieldState(),
                                modifier = Modifier.testTag(OTHER_TEST_TAG),
                            )
                        }
                    }
                }
            }
        }
    }

    public companion object {
        private const val INITIAL_TEXT = "DiaryTitleInputInitial"
        private const val RESTORED_TEXT = "DiaryTitleInputRestored"
        private const val TITLE_LABEL = "Title"
        private const val OTHER_TEST_TAG = "DiaryTitleInputOther"
    }
}
