package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.ALL_DAY_END_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_END
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_START
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.END_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.END_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.allDayValue
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.dateTimeValue
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.defaultTimeText
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.hasRole
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.toDefaultDisplayText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryDateTimeInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-001 초기 기간이 없으면 스위치가 꺼진 상태로 표시되고 기간 선택 영역이 표시되지 않는다`() {
        setDiaryDateTimeInput()

        composeRule.onNode(hasRole(Role.Switch)).assertIsOff()
        composeRule.onNode(hasRole(Role.Checkbox)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_START).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_END).assertDoesNotExist()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-002 초기 종일 기간이 제공되면 스위치가 켜진 상태로 날짜가 표시된다`() {
        setDiaryDateTimeInput(initialValue = allDayValue())

        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNodeWithText(START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(ALL_DAY_END_DATE_TEXT).assertExists()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-002 초기 종일 아님 기간이 제공되면 스위치가 켜진 상태로 날짜와 시간이 표시된다`() {
        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNodeWithText(START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(START_TIME_TEXT).assertExists()
        composeRule.onNodeWithText(END_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(END_TIME_TEXT).assertExists()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-003 스위치를 처음 켜면 오늘부터 오늘까지의 종일 기간이 표시된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toDefaultDisplayText()

        setDiaryDateTimeInput()

        composeRule.onNode(hasRole(Role.Switch)).performTouchInput {
            click(Offset(x = width * 0.95F, y = height / 2F))
        }

        composeRule.onNode(hasRole(Role.Checkbox)).assertIsOn()
        composeRule.onAllNodesWithText(today).assertCountEquals(2)
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-004 스위치를 껐다가 다시 켜면 직전 선택값이 복원된다`() {
        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNode(hasRole(Role.Switch)).performClick()

        composeRule.onNodeWithText(START_DATE_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(START_TIME_TEXT).assertDoesNotExist()

        composeRule.onNode(hasRole(Role.Switch)).performClick()

        composeRule.onNode(hasRole(Role.Checkbox)).assertIsOff()
        composeRule.onNodeWithText(START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(START_TIME_TEXT).assertExists()
        composeRule.onNodeWithText(END_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(END_TIME_TEXT).assertExists()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-035 초기 기간이 바뀌면 바뀐 초기 기간으로 처음부터 시작한다`() {
        var initialValue: DiaryDateTimeInputValue by mutableStateOf(dateTimeValue())
        composeRule.setContent {
            DiaryTheme {
                DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = initialValue))
            }
        }

        composeRule.onNode(hasRole(Role.Switch)).performClick()
        composeRule.onNode(hasRole(Role.Switch)).assertIsOff()

        initialValue = allDayValue()

        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNode(hasRole(Role.Checkbox)).assertIsOn()
        composeRule.onNodeWithText(START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(ALL_DAY_END_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(START_TIME_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-031 종일을 해제하면 두 날짜가 유지되고 시각이 모두 기본 시각으로 표시된다`() {
        val defaultTimeText = defaultTimeText()

        setDiaryDateTimeInput(initialValue = allDayValue())

        composeRule.onNode(hasRole(Role.Checkbox)).performClick()

        composeRule.onNodeWithText(START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(ALL_DAY_END_DATE_TEXT).assertExists()
        composeRule.onAllNodesWithText(defaultTimeText).assertCountEquals(2)
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-006 종일을 선택하면 시작과 종료에 날짜만 표시된다`() {
        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNode(hasRole(Role.Checkbox)).performClick()

        composeRule.onNodeWithText(START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(END_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(START_TIME_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(END_TIME_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-032 종일로 전환한 뒤 다시 해제하면 시간이 기본 시각에서 다시 시작한다`() {
        val defaultTimeText = defaultTimeText()

        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNode(hasRole(Role.Checkbox)).performClick()
        composeRule.onNode(hasRole(Role.Checkbox)).performClick()

        composeRule.onNodeWithText(START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(END_DATE_TEXT).assertExists()
        composeRule.onAllNodesWithText(defaultTimeText).assertCountEquals(2)
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-018 화면이 재생성되어도 선택 상태가 유지된다`() {
        val defaultTimeText = defaultTimeText()
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = allDayValue()))
            }
        }

        composeRule.onNode(hasRole(Role.Checkbox)).performClick()
        composeRule.onAllNodesWithText(defaultTimeText).assertCountEquals(2)

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNode(hasRole(Role.Switch)).assertIsOn()
        composeRule.onNode(hasRole(Role.Checkbox)).assertIsOff()
        composeRule.onNodeWithText(START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(ALL_DAY_END_DATE_TEXT).assertExists()
        composeRule.onAllNodesWithText(defaultTimeText).assertCountEquals(2)
    }

    private fun setDiaryDateTimeInput(initialValue: DiaryDateTimeInputValue? = null) {
        composeRule.setContent {
            DiaryTheme {
                DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = initialValue))
            }
        }
    }
}
