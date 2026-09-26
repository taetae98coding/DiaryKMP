package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.DEFAULT_CANCEL
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.DEFAULT_CONFIRM
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.DROP_DOWN_DESCRIPTION
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.DROP_UP_DESCRIPTION
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.KOREAN_CANCEL
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.KOREAN_CONFIRM
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.NEXT_MONTH_DESCRIPTION
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.dayCellText
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.englishTitle
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.koreanTitle
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.datetime.yearMonth
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScaffoldDatePickerDialogTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `다이얼로그가 닫혀 있으면 제목 오른쪽에 아래쪽 화살표 아이콘이 표시된다`() {
        setCalendarHomeScaffold()

        composeRule.onNodeWithContentDescription(DROP_DOWN_DESCRIPTION, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DROP_UP_DESCRIPTION, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-005 제목을 누르면 현재 표시 중인 달의 1일이 선택된 날짜 선택 다이얼로그가 표시된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        setCalendarHomeScaffold()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).performClick()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNode(hasText(text = dayCellText(today.yearMonth, day = 1), substring = true)).assert(isSelected())
    }

    @Test
    fun `제목 클릭 영역은 제목과 화살표 주위에 안쪽 여백을 포함한다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        setCalendarHomeScaffold()

        val title = englishTitle(today.yearMonth)
        val clickBounds =
            composeRule
                .onNode(hasClickAction() and hasText(title))
                .getUnclippedBoundsInRoot()
        val titleBounds =
            composeRule
                .onNodeWithText(title, useUnmergedTree = true)
                .getUnclippedBoundsInRoot()
        val arrowBounds =
            composeRule
                .onNodeWithContentDescription(DROP_DOWN_DESCRIPTION, useUnmergedTree = true)
                .getUnclippedBoundsInRoot()

        titleBounds.left - clickBounds.left shouldBe 8.dp
        clickBounds.right - arrowBounds.right shouldBe 8.dp
        minOf(titleBounds.top, arrowBounds.top) - clickBounds.top shouldBe 4.dp
        clickBounds.bottom - maxOf(titleBounds.bottom, arrowBounds.bottom) shouldBe 4.dp
    }

    @Test
    fun `다이얼로그가 열려 있으면 제목 오른쪽에 위쪽 화살표 아이콘이 표시된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        setCalendarHomeScaffold()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).performClick()

        composeRule.onNodeWithContentDescription(DROP_UP_DESCRIPTION, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithContentDescription(DROP_DOWN_DESCRIPTION, useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-007 날짜를 고르고 확인하면 캘린더가 고른 날짜가 속한 달로 이동한다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val nextYearMonth =
            today.yearMonth.firstDay
                .plus(1, DateTimeUnit.MONTH)
                .yearMonth

        setCalendarHomeScaffold()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).performClick()
        composeRule.onNodeWithContentDescription(NEXT_MONTH_DESCRIPTION).performClick()
        composeRule.onNode(hasText(text = dayCellText(nextYearMonth, day = 15), substring = true)).performClick()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
        composeRule.onNodeWithText(englishTitle(nextYearMonth)).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-008 다이얼로그를 취소하면 보던 달이 유지된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val nextYearMonth =
            today.yearMonth.firstDay
                .plus(1, DateTimeUnit.MONTH)
                .yearMonth

        setCalendarHomeScaffold()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).performClick()
        composeRule.onNodeWithContentDescription(NEXT_MONTH_DESCRIPTION).performClick()
        composeRule.onNode(hasText(text = dayCellText(nextYearMonth, day = 15), substring = true)).performClick()
        composeRule.onNodeWithText(DEFAULT_CANCEL).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CANCEL).assertDoesNotExist()
        composeRule.onNodeWithText(englishTitle(today.yearMonth)).assertIsDisplayed()
    }

    @Test
    fun `날짜 선택 다이얼로그의 확인과 취소 문구가 영어로 표시된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        setCalendarHomeScaffold()

        composeRule.onNodeWithText(englishTitle(today.yearMonth)).performClick()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNodeWithText(DEFAULT_CANCEL).assertExists()
    }

    @Test
    @Config(sdk = [36], qualifiers = "ko")
    fun `날짜 선택 다이얼로그의 확인과 취소 문구가 한국어로 표시된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        setCalendarHomeScaffold()

        composeRule.onNodeWithText(koreanTitle(today.yearMonth)).performClick()

        composeRule.onNodeWithText(KOREAN_CONFIRM).assertExists()
        composeRule.onNodeWithText(KOREAN_CANCEL).assertExists()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-066 달 선택 중 화면이 재생성되어도 달 선택이 열린 채로 유지된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                CalendarHomeScaffold(onEvent = {}, onCalendarEvent = {})
            }
        }
        composeRule.onNodeWithText(englishTitle(today.yearMonth)).performClick()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNodeWithContentDescription(DROP_UP_DESCRIPTION, useUnmergedTree = true).assertExists()
        // 열려 있는 다이얼로그 헤더도 같은 년월 문구를 표시하므로, 화살표를 함께 가진 제목으로 좁혀 찾는다.
        composeRule
            .onNode(hasText(englishTitle(today.yearMonth)) and hasContentDescription(DROP_UP_DESCRIPTION))
            .assertExists()
        composeRule.onNode(hasText(text = dayCellText(today.yearMonth, day = 1), substring = true)).assert(isSelected())
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-102 달 선택 중 시스템이 앱을 정리했다가 다시 만들어도 달 선택이 열린 채로 다시 보인다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val restorationTester = StateRestorationTester(composeRule)

        restorationTester.setContent {
            DiaryTheme {
                CalendarHomeScaffold(onEvent = {}, onCalendarEvent = {})
            }
        }
        composeRule.onNodeWithText(englishTitle(today.yearMonth)).performClick()

        // 시스템이 정리한 앱을 다시 만들 때도 화면 재생성과 같이 저장해 둔 화면 상태에서 복원한다.
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule
            .onNode(hasText(englishTitle(today.yearMonth)) and hasContentDescription(DROP_UP_DESCRIPTION))
            .assertExists()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-090 달 선택 중 다른 앱에 다녀와도 달 선택이 열린 채로 유지된다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)

        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                DiaryTheme {
                    CalendarHomeScaffold(onEvent = {}, onCalendarEvent = {})
                }
            }
        }
        composeRule.onNodeWithText(englishTitle(today.yearMonth)).performClick()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule
            .onNode(hasText(englishTitle(today.yearMonth)) and hasContentDescription(DROP_UP_DESCRIPTION))
            .assertExists()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-091 달 선택 중 앱을 다시 실행하면 달 선택이 닫힌 상태로 시작한다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        var launchCount by mutableIntStateOf(0)

        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없으므로, 새 키로 저장 상태 없이 화면을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) {
                DiaryTheme {
                    CalendarHomeScaffold(onEvent = {}, onCalendarEvent = {})
                }
            }
        }
        composeRule.onNodeWithText(englishTitle(today.yearMonth)).performClick()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DROP_DOWN_DESCRIPTION, useUnmergedTree = true).assertExists()
        composeRule.onNodeWithText(englishTitle(today.yearMonth)).assertIsDisplayed()
    }

    private fun setCalendarHomeScaffold() {
        composeRule.setContent {
            DiaryTheme {
                CalendarHomeScaffold(onEvent = {}, onCalendarEvent = {})
            }
        }
    }
}
