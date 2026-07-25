package io.github.taetae98coding.diary.compose.core.input

import android.provider.Settings
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_CANCEL
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_CONFIRM
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.END_HOUR_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.END_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.PICKED_AM_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.PICKED_EARLIER_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.PICKED_PM_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.PICK_EARLIER_HOUR_DESCRIPTION
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.PICK_HOUR_DESCRIPTION
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_HOUR_12_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_HOUR_24_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_PERIOD_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.dateTimeValue
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.datetime.LocalDateTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryDateTimeInputTimePickerDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUpTimeFormat() {
        setDeviceTimeFormat(is24Hour = false)
    }

    @Test
    fun `시간을 누르면 현재 시간이 선택된 시간 선택 다이얼로그가 표시된다`() {
        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNodeWithText(START_TIME_TEXT).performClick()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onAllNodesWithText(START_HOUR_12_TEXT).assertAny(isSelected())

        composeRule.onNodeWithText(DEFAULT_CANCEL).performClick()
        composeRule.onNodeWithText(END_TIME_TEXT).performClick()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onAllNodesWithText(END_HOUR_TEXT).assertAny(isSelected())
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-012 시간 선택 다이얼로그에서 확인하면 고른 시간이 반영된다`() {
        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNodeWithText(START_TIME_TEXT).performClick()
        composeRule.onNodeWithContentDescription(PICK_HOUR_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()

        composeRule.onNodeWithText(PICKED_PM_TIME_TEXT).assertExists()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-013 시간 선택 다이얼로그를 취소하면 기존 값이 유지된다`() {
        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNodeWithText(START_TIME_TEXT).performClick()
        composeRule.onNodeWithContentDescription(PICK_HOUR_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_CANCEL).performClick()

        composeRule.onNodeWithText(START_TIME_TEXT).assertExists()
        composeRule.onNodeWithText(PICKED_PM_TIME_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-014 종일 아님에서 시작 시간을 종료보다 뒤로 선택하면 종료가 시작에 맞춰진다`() {
        setDiaryDateTimeInput(
            initialValue =
                DiaryDateTimeInputValue.DateTime(
                    start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 8, minute = 30),
                    endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 9, minute = 0),
                ),
        )

        composeRule.onNodeWithText("8:30 AM").performClick()
        composeRule.onNodeWithContentDescription(PICK_HOUR_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()

        composeRule.onAllNodesWithText(PICKED_AM_TIME_TEXT).assertCountEquals(2)
    }

    @Test
    fun `TC-DIARY-DATE-TIME-INPUT-FEATURE-015 종일 아님에서 종료 시간을 시작보다 앞으로 선택하면 시작이 종료에 맞춰진다`() {
        setDiaryDateTimeInput(
            initialValue =
                DiaryDateTimeInputValue.DateTime(
                    start = LocalDateTime(year = 2026, month = 7, day = 20, hour = 8, minute = 30),
                    endInclusive = LocalDateTime(year = 2026, month = 7, day = 20, hour = 9, minute = 0),
                ),
        )

        composeRule.onNodeWithText(END_TIME_TEXT).performClick()
        composeRule.onNodeWithContentDescription(PICK_EARLIER_HOUR_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()

        composeRule.onAllNodesWithText(PICKED_EARLIER_TIME_TEXT).assertCountEquals(2)
    }

    @Test
    fun `12시간제 기기에서는 오전·오후를 고르는 시계를 표시한다`() {
        setDeviceTimeFormat(is24Hour = false)
        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNodeWithText(START_TIME_TEXT).performClick()

        composeRule.onAllNodesWithText(START_HOUR_12_TEXT).assertAny(isSelected())
        composeRule.onAllNodesWithText(START_PERIOD_TEXT).assertAny(isSelected())
    }

    @Test
    fun `24시간제 기기에서는 오전·오후 없는 시계를 표시한다`() {
        setDeviceTimeFormat(is24Hour = true)
        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNodeWithText(START_TIME_TEXT).performClick()

        composeRule.onAllNodesWithText(START_HOUR_24_TEXT).assertAny(isSelected())
        composeRule.onAllNodesWithText(START_PERIOD_TEXT).assertCountEquals(0)
    }

    private fun setDeviceTimeFormat(is24Hour: Boolean) {
        Settings.System.putString(
            RuntimeEnvironment.getApplication().contentResolver,
            Settings.System.TIME_12_24,
            if (is24Hour) "24" else "12",
        )
    }

    private fun setDiaryDateTimeInput(initialValue: DiaryDateTimeInputValue? = null) {
        composeRule.setContent {
            DiaryTheme {
                DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = initialValue))
            }
        }
    }
}
