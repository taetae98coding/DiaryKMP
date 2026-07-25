package io.github.taetae98coding.diary.compose.core.effect

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RequestFocusEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `전달한 FocusRequester가 가리키는 입력에 초점을 맞춘다`() {
        setRequestFocusEffect()

        composeRule.onNodeWithTag(TARGET_TEST_TAG).assertIsFocused()
    }

    @Test
    fun `초점을 옮긴 뒤에는 다시 초점을 맞추지 않는다`() {
        setRequestFocusEffect()

        composeRule.onNodeWithTag(OTHER_TEST_TAG).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(OTHER_TEST_TAG).assertIsFocused()
        composeRule.onNodeWithTag(TARGET_TEST_TAG).assertIsNotFocused()
    }

    private fun setRequestFocusEffect() {
        composeRule.setContent {
            DiaryTheme {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        val focusRequester = remember { FocusRequester() }

        RequestFocusEffect(focusRequester = focusRequester)

        TextField(
            state = rememberTextFieldState(),
            modifier =
                Modifier
                    .focusRequester(focusRequester)
                    .testTag(TARGET_TEST_TAG),
        )
        TextField(
            state = rememberTextFieldState(),
            modifier = Modifier.testTag(OTHER_TEST_TAG),
        )
    }

    public companion object {
        private const val TARGET_TEST_TAG = "RequestFocusEffectTarget"
        private const val OTHER_TEST_TAG = "RequestFocusEffectOther"
    }
}
