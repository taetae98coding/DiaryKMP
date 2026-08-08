package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ANNUAL_LEAVE_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_DECREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_INCREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_ANNUAL_LEAVE_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_DECREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.KOREAN_INCREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayHomeAnnualLeaveStepperTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-004 기본 환경에서 연차 라벨과 기본 연차 개수를 표시한다`() {
        setHolidayHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_ANNUAL_LEAVE_LABEL).assertExists()
        composeRule.onNodeWithText("0").assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-HOLIDAY-HOME-FEATURE-004 한국어 환경에서 연차 라벨과 기본 연차 개수를 표시한다`() {
        setHolidayHomeScaffold()

        composeRule.onNodeWithText(KOREAN_ANNUAL_LEAVE_LABEL).assertExists()
        composeRule.onNodeWithText("0").assertExists()
    }

    @Test
    fun `기본 환경에서 연차 줄이기와 늘리기 동작에 접근성 이름을 제공한다`() {
        setHolidayHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_DECREASE_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_INCREASE_DESCRIPTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 연차 줄이기와 늘리기 동작에 접근성 이름을 제공한다`() {
        setHolidayHomeScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_DECREASE_DESCRIPTION).assertExists()
        composeRule.onNodeWithContentDescription(KOREAN_INCREASE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-006 늘리기 동작을 선택하면 연차 개수가 하나 는다`() {
        setHolidayHomeScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_INCREASE_DESCRIPTION).performClick()

        composeRule.onNodeWithText("1").assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-007 줄이기 동작을 선택하면 연차 개수가 하나 준다`() {
        setHolidayHomeScaffold()

        repeat(2) { composeRule.onNodeWithContentDescription(DEFAULT_INCREASE_DESCRIPTION).performClick() }
        composeRule.onNodeWithContentDescription(DEFAULT_DECREASE_DESCRIPTION).performClick()

        composeRule.onNodeWithText("1").assertExists()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-008 연차 개수가 가장 작은 값이면 줄이기 동작을 선택할 수 없다`() {
        setHolidayHomeScaffold()

        composeRule.onNodeWithText("0").assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DECREASE_DESCRIPTION).assertIsNotEnabled()
    }

    @Test
    fun `TC-HOLIDAY-HOME-FEATURE-019 연차 개수에는 상한이 없다`() {
        setHolidayHomeScaffold()

        repeat(CLICK_COUNT) { composeRule.onNodeWithContentDescription(DEFAULT_INCREASE_DESCRIPTION).performClick() }

        composeRule.onNodeWithText(CLICK_COUNT.toString()).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_INCREASE_DESCRIPTION).assertIsEnabled()
    }

    private fun setHolidayHomeScaffold() {
        composeRule.setContent {
            DiaryTheme {
                HolidayHomeScaffold(
                    onEvent = {},
                    state = rememberHolidayHomeScaffoldState(initialYear = YEAR),
                    yearContent = { _ -> },
                )
            }
        }
    }

    private companion object {
        // 예전 최대값 다섯을 넘겨 상한이 없음을 확인한다.
        private const val CLICK_COUNT = 6
    }
}
