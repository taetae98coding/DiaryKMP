package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.END_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.END_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.dateTimeValue
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.hasRole
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryDateTimeInputExecutionBoundaryTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-036 다른 앱에 다녀와도 사용 여부와 종일 여부와 선택한 기간이 유지된다`() {
        val registry = SaveableStateRegistry(restoredValues = null) { true }
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        composeRule.setContent {
            CompositionLocalProvider(
                LocalSaveableStateRegistry provides registry,
                LocalLifecycleOwner provides lifecycleOwner,
            ) {
                DiaryTheme {
                    DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = dateTimeValue()))
                }
            }
        }
        composeRule.onNode(hasRole(Role.Checkbox)).performClick()
        composeRule.onNodeWithText(START_TIME_TEXT).assertDoesNotExist()

        // 백그라운드로 이동하면 화면 상태가 저장되고 수명 주기가 멈춘 상태가 된 뒤, 돌아오면 다시 재개된다.
        composeRule.runOnIdle {
            registry.performSave()
            lifecycleOwner.currentState = Lifecycle.State.CREATED
        }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        assertAllDayChangedValue()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-037 시스템이 앱을 종료했다가 다시 열리면 떠나기 직전의 값이 복원된다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = dateTimeValue()))
            }
        }
        composeRule.onNode(hasRole(Role.Checkbox)).performClick()
        composeRule.onNodeWithText(START_TIME_TEXT).assertDoesNotExist()

        restorationTester.emulateSavedInstanceStateRestore()

        assertAllDayChangedValue()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-038 앱을 새로 실행하면 이전에 고른 값 없이 초기 기간으로 시작한다`() {
        var isShown by mutableStateOf(true)
        composeRule.setContent {
            DiaryTheme {
                if (isShown) {
                    DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = dateTimeValue()))
                }
            }
        }
        composeRule.onNode(hasRole(Role.Checkbox)).performClick()
        composeRule.onNodeWithText(START_TIME_TEXT).assertDoesNotExist()

        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()
        composeRule.runOnIdle { isShown = true }
        composeRule.waitForIdle()

        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNode(hasRole(Role.Checkbox)).assertIsOff()
        composeRule.onNodeWithText(START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(START_TIME_TEXT).assertExists()
        composeRule.onNodeWithText(END_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(END_TIME_TEXT).assertExists()
    }

    private fun assertAllDayChangedValue() {
        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNode(hasRole(Role.Checkbox)).assertIsOn()
        composeRule.onNodeWithText(START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(END_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(START_TIME_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(END_TIME_TEXT).assertDoesNotExist()
    }
}
