package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.ALL_DAY_END_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.ALL_DAY_END_DAY_CELL_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_CANCEL
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_CONFIRM
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.EARLIER_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.EARLIER_DAY_CELL_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.END_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.PICKED_START_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.PICK_DAY_CELL_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_DAY_CELL_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.allDayValue
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryDateTimeInputDatePickerDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `날짜를 누르면 현재 날짜가 선택된 날짜 선택 다이얼로그가 표시된다`() {
        setDiaryDateTimeInput(initialValue = allDayValue())

        composeRule.onNodeWithText(START_DATE_TEXT).performClick()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNode(hasText(text = START_DAY_CELL_TEXT, substring = true)).assert(isSelected())

        composeRule.onNodeWithText(DEFAULT_CANCEL).performClick()
        composeRule.onNodeWithText(ALL_DAY_END_DATE_TEXT).performClick()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNode(hasText(text = ALL_DAY_END_DAY_CELL_TEXT, substring = true)).assert(isSelected())
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-009 날짜 선택 다이얼로그에서 확인하면 고른 날짜가 반영된다`() {
        setDiaryDateTimeInput(initialValue = allDayValue())

        composeRule.onNodeWithText(START_DATE_TEXT).performClick()
        composeRule.onNode(hasText(text = PICK_DAY_CELL_TEXT, substring = true)).performClick()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()

        composeRule.onNodeWithText(PICKED_START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(ALL_DAY_END_DATE_TEXT).assertExists()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-010 날짜 선택 다이얼로그를 취소하면 기존 값이 유지된다`() {
        setDiaryDateTimeInput(initialValue = allDayValue())

        composeRule.onNodeWithText(START_DATE_TEXT).performClick()
        composeRule.onNode(hasText(text = PICK_DAY_CELL_TEXT, substring = true)).performClick()
        composeRule.onNodeWithText(DEFAULT_CANCEL).performClick()

        composeRule.onNodeWithText(START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(PICKED_START_DATE_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-014 시작을 종료보다 뒤로 선택하면 종료가 시작에 맞춰진다`() {
        setDiaryDateTimeInput(
            initialValue =
                DiaryDateTimeInputValue.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 20)),
        )

        composeRule.onNodeWithText(START_DATE_TEXT).performClick()
        composeRule.onNode(hasText(text = PICK_DAY_CELL_TEXT, substring = true)).performClick()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()

        composeRule.onAllNodesWithText(PICKED_START_DATE_TEXT).assertCountEquals(2)
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-015 종료를 시작보다 앞으로 선택하면 시작이 종료에 맞춰진다`() {
        setDiaryDateTimeInput(
            initialValue =
                DiaryDateTimeInputValue.AllDay(dateRange = LocalDate(year = 2026, month = 7, day = 19)..LocalDate(year = 2026, month = 7, day = 20)),
        )

        composeRule.onNodeWithText(END_DATE_TEXT).performClick()
        composeRule.onNode(hasText(text = EARLIER_DAY_CELL_TEXT, substring = true)).performClick()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()

        composeRule.onAllNodesWithText(EARLIER_DATE_TEXT).assertCountEquals(2)
    }

    private fun setDiaryDateTimeInput(initialValue: DiaryDateTimeInputValue? = null) {
        composeRule.setContent {
            DiaryTheme {
                DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = initialValue))
            }
        }
    }
}
