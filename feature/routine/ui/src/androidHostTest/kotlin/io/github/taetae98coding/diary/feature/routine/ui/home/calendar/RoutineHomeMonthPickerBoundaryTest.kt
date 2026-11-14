package io.github.taetae98coding.diary.feature.routine.ui.home.calendar

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.DEFAULT_CONFIRM
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.DEFAULT_LIST_VIEW_MODE_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.DEFAULT_NEXT_MONTH_DESCRIPTION
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.JULY_15_2026
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.JULY_2026
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.englishDayCellText
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarTestFixture.englishTitle
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.holiday.RoutineHomeHolidayUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.viewmode.RoutineHomeViewMode
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeMonthPickerBoundaryTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var calendarViewModel = RoutineHomeCalendarTestFixture.calendarViewModel(uiState = MutableStateFlow(RoutineHomeCalendarUiState()))
    private var holidayViewModel = RoutineHomeCalendarTestFixture.holidayViewModel(uiState = MutableStateFlow(RoutineHomeHolidayUiState()))

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-e6767167-926c-48bd-adee-8c39f9d8e15b 달 선택이 열린 채 화면이 재생성되어도 달 선택은 열린 채로 남고 달은 이동하지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { RoutineHomeTestScreen() }
        openMonthPickerAndPickAugustDate()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertMonthPickerOpenOnJuly()
    }

    @Test
    fun `TC-e6767167-926c-48bd-adee-8c39f9d8e15b 달 선택이 열린 채 다른 앱에 다녀와도 달 선택은 열린 채로 남고 달은 이동하지 않는다`() {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) { RoutineHomeTestScreen() }
        }
        openMonthPickerAndPickAugustDate()

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }
        composeRule.waitForIdle()

        assertMonthPickerOpenOnJuly()
    }

    @Test
    fun `TC-e6767167-926c-48bd-adee-8c39f9d8e15b 달 선택이 열린 채 시스템이 앱을 정리했다가 다시 만들어도 달 선택은 열린 채로 남고 달은 이동하지 않는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { RoutineHomeTestScreen() }
        openMonthPickerAndPickAugustDate()

        // 시스템이 정리한 앱을 다시 만들면 ViewModel은 새로 만들어지고 저장해 둔 화면 상태만 복원된다.
        calendarViewModel = RoutineHomeCalendarTestFixture.calendarViewModel(uiState = MutableStateFlow(RoutineHomeCalendarUiState()))
        holidayViewModel = RoutineHomeCalendarTestFixture.holidayViewModel(uiState = MutableStateFlow(RoutineHomeHolidayUiState()))
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertMonthPickerOpenOnJuly()
    }

    @Test
    fun `TC-8a984839-ffdf-4f18-874d-014359d49591 달 선택이 열린 채 다른 주요 목적지에 다녀오면 목록 보기로 시작하고 달 선택은 닫혀 있다`() {
        val backStack = NavBackStack<ScreenNavKey>(RoutineHomeNavKey)
        composeRule.setContent {
            NavDisplay(
                backStack = backStack,
                entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                entryProvider =
                    entryProvider {
                        entry<RoutineHomeNavKey> { RoutineHomeTestScreen(initialViewMode = RoutineHomeViewMode.LIST) }
                        entry<RoutineHomeMonthPickerTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                    },
            )
        }
        showCalendarAndOpenMonthPicker()

        // 공통 내비게이션은 주요 목적지를 옮길 때 떠난 목적지를 전환 이력에서 빼고 새 목적지만 남긴다.
        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(RoutineHomeMonthPickerTopLevelNavKey)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(TOP_LEVEL_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(RoutineHomeNavKey)
        }
        composeRule.waitForIdle()

        assertListShownAndMonthPickerClosedAfterShowingCalendar()
    }

    @Test
    fun `TC-8a984839-ffdf-4f18-874d-014359d49591 달 선택이 열린 채 앱을 다시 실행하면 목록 보기로 시작하고 달 선택은 닫혀 있다`() {
        var launchCount by mutableIntStateOf(0)
        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없으므로, 새 키로 저장 상태 없이 화면을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) { RoutineHomeTestScreen(initialViewMode = RoutineHomeViewMode.LIST) }
        }
        showCalendarAndOpenMonthPicker()

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        assertListShownAndMonthPickerClosedAfterShowingCalendar()
    }

    private fun openMonthPickerAndPickAugustDate() {
        val august = YearMonth(year = 2026, month = Month.AUGUST)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(englishTitle(JULY_2026)).performClick()
        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_MONTH_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasText(text = englishDayCellText(august, day = 15), substring = true)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
    }

    private fun showCalendarAndOpenMonthPicker() {
        composeRule.waitForIdle()
        composeRule.routineHomeCalendarToggleViewMode(description = DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION)
        composeRule.onNodeWithText(englishTitle(JULY_2026)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
    }

    private fun assertMonthPickerOpenOnJuly() {
        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertExists()
        composeRule.onNode(hasText(englishTitle(JULY_2026)) and hasContentDescription(DROP_UP_DESCRIPTION)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_LIST_VIEW_MODE_DESCRIPTION).assertExists()
    }

    private fun assertListShownAndMonthPickerClosedAfterShowingCalendar() {
        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)

        composeRule.routineHomeCalendarToggleViewMode(description = DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_CONFIRM).assertDoesNotExist()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.onNode(hasText(englishTitle(JULY_2026)) and hasContentDescription(DROP_DOWN_DESCRIPTION)).assertIsDisplayed()
    }

    @Composable
    private fun RoutineHomeTestScreen(initialViewMode: RoutineHomeViewMode = RoutineHomeViewMode.CALENDAR) {
        RoutineHomeCalendarTestScreen(
            today = JULY_15_2026,
            calendarViewModel = calendarViewModel,
            holidayViewModel = holidayViewModel,
            initialViewMode = initialViewMode,
        )
    }

    private companion object {
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
        const val DROP_DOWN_DESCRIPTION = "Show date picker"
        const val DROP_UP_DESCRIPTION = "Hide date picker"
    }
}

// 메모나 태그처럼 루틴이 아닌 주요 목적지를 대신한다. 다른 기능의 화면 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object RoutineHomeMonthPickerTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "RoutineHomeMonthPickerTopLevel"
}
