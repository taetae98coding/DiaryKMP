package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_ALL_DAY
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_CANCEL
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_CONFIRM
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_END
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_LABEL
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.DEFAULT_START
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.KOREAN_ALL_DAY
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.KOREAN_CANCEL
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.KOREAN_CONFIRM
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.KOREAN_END
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.KOREAN_END_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.KOREAN_END_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.KOREAN_LABEL
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.KOREAN_START
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.KOREAN_START_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.KOREAN_START_TIME_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.START_DATE_TEXT
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.allDayValue
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputTestFixture.dateTimeValue
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryDateTimeInputLocalizationTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 컴포넌트 문구를 표시한다`() {
        setDiaryDateTimeInput()

        composeRule.onNodeWithText(KOREAN_LABEL).assertExists()
        composeRule.onNodeWithText(KOREAN_ALL_DAY).assertExists()
        composeRule.onNodeWithText(KOREAN_START).assertExists()
        composeRule.onNodeWithText(KOREAN_END).assertExists()
    }

    @Test
    fun `기본 환경에서 컴포넌트 문구를 표시한다`() {
        setDiaryDateTimeInput()

        composeRule.onNodeWithText(DEFAULT_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_ALL_DAY).assertExists()
        composeRule.onNodeWithText(DEFAULT_START).assertExists()
        composeRule.onNodeWithText(DEFAULT_END).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 다이얼로그 버튼 문구를 표시한다`() {
        setDiaryDateTimeInput()

        composeRule.onNodeWithText(KOREAN_START_DATE_TEXT).performClick()

        composeRule.onNodeWithText(KOREAN_CONFIRM).assertExists()
        composeRule.onNodeWithText(KOREAN_CANCEL).assertExists()
    }

    @Test
    fun `기본 환경에서 다이얼로그 버튼 문구를 표시한다`() {
        setDiaryDateTimeInput()

        composeRule.onNodeWithText(START_DATE_TEXT).performClick()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNodeWithText(DEFAULT_CANCEL).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 컴포넌트의 날짜를 한국어 표기로 표시한다`() {
        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNodeWithText(KOREAN_START_DATE_TEXT).assertExists()
        composeRule.onNodeWithText(KOREAN_END_DATE_TEXT).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 컴포넌트의 시간을 한국어 표기로 표시한다`() {
        setDiaryDateTimeInput(initialValue = dateTimeValue())

        composeRule.onNodeWithText(KOREAN_START_TIME_TEXT).assertExists()
        composeRule.onNodeWithText(KOREAN_END_TIME_TEXT).assertExists()
    }

    private fun setDiaryDateTimeInput(initialValue: DiaryDateTimeInputValue = allDayValue()) {
        composeRule.setContent {
            DiaryTheme {
                DiaryDateTimeInput(state = rememberDiaryDateTimeInputState(initialValue = initialValue))
            }
        }
    }
}
