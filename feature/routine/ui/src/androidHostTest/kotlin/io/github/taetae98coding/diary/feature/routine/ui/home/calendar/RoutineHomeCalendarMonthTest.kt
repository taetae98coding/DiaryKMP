package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.DAYS_PER_CALENDAR
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.DEFAULT_CANCEL
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.DEFAULT_CONFIRM
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.DEFAULT_NEXT_MONTH_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.JULY_15_2026
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.JULY_2026
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.KOREAN_CALENDAR_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.KOREAN_TODAY_BUTTON_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.englishDayCellText
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.englishTitle
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.koreanTitle
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.plus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineHomeCalendarMonthTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var primaryDateColor = Color.Unspecified

    @Test
    fun `TC-3606b4f1-86ce-42e1-9bb8-bdd048d00272 달력 보기로 처음 바꾸면 오늘이 속한 달의 달력이 표시된다`() {
        setRoutineHomeScreen(initialViewMode = RoutineHomeViewMode.LIST)
        composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertDoesNotExist()

        composeRule.routineHomeCalendarToggleViewMode(description = KOREAN_CALENDAR_VIEW_MODE_DESCRIPTION)

        composeRule.routineHomeCalendarDateTexts() shouldBe List(DAYS_PER_CALENDAR) { index -> JUNE_28_2026.plus(index, DateTimeUnit.DAY).day.toString() }
        composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertIsDisplayed()
    }

    @Test
    fun `TC-a00e2e81-189e-4443-a564-51d1226aa3fd 이전 달로 이동하면 달력과 표시 중인 달 안내가 이전 달로 바뀐다`() {
        setRoutineHomeScreen()

        composeRule.routineHomeCalendarMoveToPreviousMonth()

        composeRule.onNodeWithText(koreanTitle(YearMonth(year = 2026, month = Month.JUNE))).assertIsDisplayed()
        composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertDoesNotExist()
        // 2026년 6월은 1일이 월요일이어서 5월 31일 일요일부터 표시한다.
        composeRule.routineHomeCalendarDateTexts() shouldBe
            List(DAYS_PER_CALENDAR) { index -> LocalDate(year = 2026, month = Month.MAY, day = 31).plus(index, DateTimeUnit.DAY).day.toString() }
    }

    @Test
    fun `TC-a00e2e81-189e-4443-a564-51d1226aa3fd 다음 달로 이동하면 달력과 표시 중인 달 안내가 다음 달로 바뀐다`() {
        setRoutineHomeScreen()

        composeRule.routineHomeCalendarMoveToNextMonth()

        composeRule.onNodeWithText(koreanTitle(YearMonth(year = 2026, month = Month.AUGUST))).assertIsDisplayed()
        composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertDoesNotExist()
        // 2026년 8월은 1일이 토요일이어서 7월 26일 일요일부터 표시한다.
        composeRule.routineHomeCalendarDateTexts() shouldBe
            List(DAYS_PER_CALENDAR) { index -> LocalDate(year = 2026, month = Month.JULY, day = 26).plus(index, DateTimeUnit.DAY).day.toString() }
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-5d80f18d-bcc7-43a8-963c-5b12fd736b38 달 선택을 시작하면 표시 중인 달의 1일이 선택되어 있다`() {
        setRoutineHomeScreen()

        composeRule.onNodeWithText(englishTitle(JULY_2026)).performClick()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNode(hasText(text = englishDayCellText(JULY_2026, day = 1), substring = true)).assert(isSelected())
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-5a123a65-3ab1-4f89-99ef-331664866a51 달 선택에서 날짜를 고르고 확인하면 그 달로 이동한다`() {
        setRoutineHomeScreen()
        pickNovemberDate()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
        composeRule.onNodeWithText(englishTitle(NOVEMBER_2026)).assertIsDisplayed()
        composeRule.routineHomeCalendarDateTexts() shouldBe
            List(DAYS_PER_CALENDAR) { index -> LocalDate(year = 2026, month = Month.NOVEMBER, day = 1).plus(index, DateTimeUnit.DAY).day.toString() }
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `TC-5a123a65-3ab1-4f89-99ef-331664866a51 달 선택에서 날짜를 고르고 취소하면 보던 달을 유지한다`() {
        setRoutineHomeScreen()
        pickNovemberDate()

        composeRule.onNodeWithText(DEFAULT_CANCEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
        composeRule.onNodeWithText(englishTitle(JULY_2026)).assertIsDisplayed()
        composeRule.routineHomeCalendarDateTexts() shouldBe List(DAYS_PER_CALENDAR) { index -> JUNE_28_2026.plus(index, DateTimeUnit.DAY).day.toString() }
    }

    @Test
    fun `TC-99dfead1-4a27-48bb-9d4c-a74c2f078f1b 다른 달을 보다가 오늘이 속한 달로 이동을 실행하면 오늘이 속한 달을 본다`() {
        setRoutineHomeScreen(initialYearMonth = NOVEMBER_2026)
        composeRule.onNodeWithText(koreanTitle(NOVEMBER_2026)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(KOREAN_TODAY_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertIsDisplayed()
        composeRule.routineHomeCalendarDateTexts() shouldBe List(DAYS_PER_CALENDAR) { index -> JUNE_28_2026.plus(index, DateTimeUnit.DAY).day.toString() }
    }

    @Test
    fun `TC-99dfead1-4a27-48bb-9d4c-a74c2f078f1b 오늘이 속한 달을 보다가 오늘이 속한 달로 이동을 실행해도 그 달을 본다`() {
        setRoutineHomeScreen()

        composeRule.onNodeWithContentDescription(KOREAN_TODAY_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertIsDisplayed()
        composeRule.routineHomeCalendarDateTexts() shouldBe List(DAYS_PER_CALENDAR) { index -> JUNE_28_2026.plus(index, DateTimeUnit.DAY).day.toString() }
    }

    @Test
    fun `TC-0caf334b-08b8-4529-974e-4cf8943bc44f 오늘이 속한 달에서는 오늘 하나만 주요 날짜로 강조된다`() {
        setRoutineHomeScreen()

        composeRule.onNodeWithText(koreanTitle(JULY_2026)).assertIsDisplayed()
        composeRule.routineHomeCalendarColoredDateCount(day = JULY_15_2026.day, color = primaryDateColor) shouldBe 1
        composeRule.routineHomeCalendarColoredDateTotalCount(color = primaryDateColor) shouldBe 1
    }

    @Test
    fun `TC-0caf334b-08b8-4529-974e-4cf8943bc44f 오늘이 보이지 않는 달에서는 주요 날짜로 강조되는 날짜가 없다`() {
        val september = YearMonth(year = 2026, month = Month.SEPTEMBER)
        setRoutineHomeScreen(initialYearMonth = september)

        composeRule.onNodeWithText(koreanTitle(september)).assertIsDisplayed()
        composeRule.routineHomeCalendarColoredDateTotalCount(color = primaryDateColor) shouldBe 0
    }

    @Test
    fun `TC-39d24732-cb8a-494c-8393-5c433c2f44b6 이동 범위의 가장 이전 달에서 이전 달로 이동해도 그 달을 유지한다`() {
        val firstYearMonth = YearMonth(year = 1, month = Month.JANUARY)
        setRoutineHomeScreen(initialYearMonth = firstYearMonth)
        composeRule.onNodeWithText(koreanTitle(firstYearMonth)).assertIsDisplayed()
        val dateTextList = composeRule.routineHomeCalendarDateTexts()

        composeRule.routineHomeCalendarMoveToPreviousMonth()

        composeRule.onNodeWithText(koreanTitle(firstYearMonth)).assertIsDisplayed()
        composeRule.routineHomeCalendarDateTexts() shouldBe dateTextList
    }

    private fun pickNovemberDate() {
        composeRule.onNodeWithText(englishTitle(JULY_2026)).performClick()
        repeat(NOVEMBER_2026.month.ordinal - JULY_2026.month.ordinal) {
            composeRule.onNodeWithContentDescription(DEFAULT_NEXT_MONTH_DESCRIPTION).performClick()
            composeRule.waitForIdle()
        }
        composeRule.onNode(hasText(text = englishDayCellText(NOVEMBER_2026, day = 15), substring = true)).performClick()
        composeRule.waitForIdle()
    }

    private fun setRoutineHomeScreen(
        initialYearMonth: YearMonth = JULY_2026,
        initialViewMode: RoutineHomeViewMode = RoutineHomeViewMode.CALENDAR,
    ) {
        val calendarViewModel = RoutineHomeCalendarTestFixture.calendarViewModel(uiState = MutableStateFlow(RoutineHomeCalendarUiState()))
        val holidayViewModel = RoutineHomeCalendarTestFixture.holidayViewModel(uiState = MutableStateFlow(RoutineHomeHolidayUiState()))

        composeRule.setContent {
            RoutineHomeCalendarColorCapture { _, primaryDateColor -> this.primaryDateColor = primaryDateColor }
            RoutineHomeCalendarTestScreen(
                today = JULY_15_2026,
                calendarViewModel = calendarViewModel,
                holidayViewModel = holidayViewModel,
                initialYearMonth = initialYearMonth,
                initialViewMode = initialViewMode,
            )
        }
        composeRule.waitForIdle()
    }

    private companion object {
        val JUNE_28_2026 = LocalDate(year = 2026, month = Month.JUNE, day = 28)
        val NOVEMBER_2026 = YearMonth(year = 2026, month = Month.NOVEMBER)
    }
}
