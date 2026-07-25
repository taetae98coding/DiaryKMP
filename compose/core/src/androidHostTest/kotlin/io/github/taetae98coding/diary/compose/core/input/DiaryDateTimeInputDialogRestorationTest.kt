package io.github.taetae98coding.diary.compose.core.input

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.ALL_DAY_END_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.ALL_DAY_END_DAY_CELL_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_CONFIRM
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.END_HOUR_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.END_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_DAY_CELL_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_HOUR_12_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.allDayValue
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.dateTimeValue
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryDateTimeInputDialogRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUpTimeFormat() {
        Settings.System.putString(
            RuntimeEnvironment.getApplication().contentResolver,
            Settings.System.TIME_12_24,
            "12",
        )
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-022 시작 날짜를 고르는 중에 재생성되어도 시작 날짜 고르기가 유지된다`() {
        val restorationTester = setDiaryDateTimeInput(initialValue = allDayValue())

        composeRule.onNodeWithText(START_DATE_TEXT).performClick()
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNode(hasText(text = START_DAY_CELL_TEXT, substring = true)).assert(isSelected())
        assertPeriodUnchanged(startText = START_DATE_TEXT, endText = ALL_DAY_END_DATE_TEXT)
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-022 종료 날짜를 고르는 중에 재생성되어도 종료 날짜 고르기가 유지된다`() {
        val restorationTester = setDiaryDateTimeInput(initialValue = allDayValue())

        composeRule.onNodeWithText(ALL_DAY_END_DATE_TEXT).performClick()
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNode(hasText(text = ALL_DAY_END_DAY_CELL_TEXT, substring = true)).assert(isSelected())
        assertPeriodUnchanged(startText = START_DATE_TEXT, endText = ALL_DAY_END_DATE_TEXT)
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-022 시작 시간을 고르는 중에 재생성되어도 시작 시간 고르기가 유지된다`() {
        val restorationTester = setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNodeWithText(START_TIME_TEXT).performClick()
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onAllNodesWithText(START_HOUR_12_TEXT).assertAny(isSelected())
        assertPeriodUnchanged(startText = START_TIME_TEXT, endText = END_TIME_TEXT)
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-022 종료 시간을 고르는 중에 재생성되어도 종료 시간 고르기가 유지된다`() {
        val restorationTester = setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNodeWithText(END_TIME_TEXT).performClick()
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onAllNodesWithText(END_HOUR_TEXT).assertAny(isSelected())
        assertPeriodUnchanged(startText = START_TIME_TEXT, endText = END_TIME_TEXT)
    }

    // 열려 있는 다이얼로그 헤더가 기간 칸과 같은 문구를 표시하므로, 누를 수 있는 기간 칸으로 좁혀 찾는다.
    private fun assertPeriodUnchanged(
        startText: String,
        endText: String,
    ) {
        composeRule.onNode(hasText(startText) and hasClickAction()).assertExists()
        composeRule.onNode(hasText(endText) and hasClickAction()).assertExists()
    }

    private fun setDiaryDateTimeInput(initialValue: DiaryDateTimeInputValue): StateRestorationTester {
        val restorationTester = StateRestorationTester(composeRule)
        val content: @Composable () -> Unit = {
            DiaryTheme {
                DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = initialValue))
            }
        }

        restorationTester.setContent(content)

        return restorationTester
    }
}
