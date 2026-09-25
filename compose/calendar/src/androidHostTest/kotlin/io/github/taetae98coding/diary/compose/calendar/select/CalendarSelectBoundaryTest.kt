package io.github.taetae98coding.diary.compose.calendar.select

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.calendar.Calendar
import io.github.taetae98coding.diary.compose.calendar.CalendarState
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDateRange
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarSelectBoundaryTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-CALENDAR-SELECT-DOMAIN-007 선택 중 화면이 재생성되면 선택이 유지되지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var calendarState: CalendarState
        restorationTester.setContent {
            DiaryTheme {
                calendarState = rememberCalendarState(initialYearMonth = JULY_2026)
                Calendar(state = calendarState, onSelect = {}) {}
            }
        }

        composeRule.performLongPress(composeRule.dayCenter(day = 14))
        composeRule.performMoveTo(composeRule.dayCenter(day = 17))
        composeRule.runOnIdle { calendarState.selectState.dateRange shouldBe july(day = 14)..july(day = 17) }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle { calendarState.selectState.dateRange.shouldBeNull() }
    }

    @Test
    fun `TC-CALENDAR-SELECT-DOMAIN-009 선택 중 화면이 재생성되면 선택이 취소되고 선택 완료가 전달되지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val selectedList = mutableListOf<LocalDateRange>()
        restorationTester.setContent {
            DiaryTheme {
                Calendar(state = rememberCalendarState(initialYearMonth = JULY_2026), onSelect = { selectedList += it }) {}
            }
        }

        composeRule.performLongPress(composeRule.dayCenter(day = 14))
        composeRule.performMoveTo(composeRule.dayCenter(day = 17))

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        selectedList shouldBe emptyList()
    }

    @Test
    fun `TC-CALENDAR-SELECT-DOMAIN-010 선택 중 앱이 백그라운드로 이동하면 그 시점의 기간으로 선택이 완료된다`() {
        val registry = SaveableStateRegistry(restoredValues = null) { true }
        val selectedList = mutableListOf<LocalDateRange>()
        composeRule.setContent {
            CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
                DiaryTheme {
                    Calendar(state = rememberCalendarState(initialYearMonth = JULY_2026), onSelect = { selectedList += it }) {}
                }
            }
        }

        composeRule.performLongPress(composeRule.dayCenter(day = 14))
        composeRule.performMoveTo(composeRule.dayCenter(day = 17))
        composeRule.runOnIdle { registry.performSave() }
        composeRule.performCancel()

        selectedList shouldBe listOf(july(day = 14)..july(day = 17))
    }

    @Test
    fun `TC-CALENDAR-SELECT-DOMAIN-011 선택 중 화면을 벗어나면 그 시점의 기간으로 선택이 완료된다`() {
        var isShown by mutableStateOf(true)
        val selectedList = mutableListOf<LocalDateRange>()
        composeRule.setContent {
            DiaryTheme {
                if (isShown) {
                    Calendar(state = rememberCalendarState(initialYearMonth = JULY_2026), onSelect = { selectedList += it }) {}
                }
            }
        }

        composeRule.performLongPress(composeRule.dayCenter(day = 14))
        composeRule.performMoveTo(composeRule.dayCenter(day = 17))
        composeRule.runOnIdle { isShown = false }
        composeRule.waitForIdle()

        selectedList shouldBe listOf(july(day = 14)..july(day = 17))
    }
}
