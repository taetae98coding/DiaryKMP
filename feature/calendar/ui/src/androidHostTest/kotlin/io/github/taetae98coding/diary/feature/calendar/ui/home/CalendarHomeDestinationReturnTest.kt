package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.home.CalendarHomeTestFixture.englishTitle
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
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

// 공통 내비게이션이 목적지를 옮길 때처럼 전환 이력에 다른 목적지를 쌓았다가 캘린더만 남기고, 캘린더 화면이 보던 달을 이어서 보여 주는지 본다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeDestinationReturnTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val backStack = NavBackStack<ScreenNavKey>(CalendarHomeNavKey)

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-016 캘린더는 다른 목적지에 다녀와도 보던 달을 그대로 보여 준다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val nextYearMonth =
            today.yearMonth.firstDay
                .plus(1, DateTimeUnit.MONTH)
                .yearMonth
        setCalendarNavDisplay()
        composeRule.onRoot().performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(englishTitle(nextYearMonth)).assertIsDisplayed()

        composeRule.runOnIdle { backStack.add(DestinationTestTopLevelNavKey) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(TOP_LEVEL_CONTENT).assertIsDisplayed()
        composeRule.runOnIdle {
            backStack.clear()
            backStack.add(CalendarHomeNavKey)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(englishTitle(nextYearMonth)).assertIsDisplayed()
    }

    private fun setCalendarNavDisplay() {
        val holidayViewModel = mockk<CalendarHomeHolidayViewModel>()
        every { holidayViewModel.fetch(any()) } returns Unit
        every { holidayViewModel.holidayList } returns MutableStateFlow(emptyList())
        every { holidayViewModel.isFetching } returns MutableStateFlow(false)

        val memoViewModel = mockk<CalendarHomeMemoViewModel>()
        every { memoViewModel.fetch(any()) } returns Unit
        every { memoViewModel.memoList } returns MutableStateFlow(emptyList())
        every { memoViewModel.filterUiState } returns MutableStateFlow(CalendarHomeScaffoldFilterUiState())

        val birthdayViewModel = birthdayViewModel()
        val weatherViewModel = weatherViewModel()
        val syncViewModel = syncViewModel()

        composeRule.setContent {
            DiaryTheme {
                NavDisplay(
                    backStack = backStack,
                    entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                    entryProvider =
                        entryProvider {
                            // CalendarEntry가 캘린더 홈을 조립하는 것과 같은 구성으로 둔다.
                            entry<CalendarHomeNavKey> {
                                val state = rememberCalendarHomeScaffoldState()

                                ScrollToTodayOnReselectEffect(
                                    reselectEvent = emptyFlow(),
                                    state = state,
                                )
                                CalendarHomeScreen(
                                    navigateToMemoDetail = {},
                                    navigateToMemoAdd = {},
                                    navigateToContactDetail = {},
                                    navigateToFilter = {},
                                    navigateToTimetable = {},
                                    state = state,
                                    permissionManager = rememberPermissionManager(),
                                    holidayViewModel = holidayViewModel,
                                    memoViewModel = memoViewModel,
                                    birthdayViewModel = birthdayViewModel,
                                    weatherViewModel = weatherViewModel,
                                    syncViewModel = syncViewModel,
                                )
                            }
                            entry<DestinationTestTopLevelNavKey> { Text(text = TOP_LEVEL_CONTENT) }
                        },
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val TOP_LEVEL_CONTENT = "TopLevelContent"
    }
}

// 메모나 태그처럼 캘린더가 아닌 주요 목적지를 대신한다. 다른 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object DestinationTestTopLevelNavKey : ScreenNavKey {
    override val screenName: String
        get() = "DestinationTestTopLevel"
}
