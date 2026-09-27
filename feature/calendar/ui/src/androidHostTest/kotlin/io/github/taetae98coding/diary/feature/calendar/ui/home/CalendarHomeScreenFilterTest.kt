package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.feature.calendar.ui.home.holiday.CalendarHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CalendarHomeScreenFilterTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `필터 버튼을 누르면 필터 화면으로 이동한다`() {
        var navigateToFilterCount = 0
        setCalendarHomeScreen(navigateToFilter = { navigateToFilterCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION).performClick()

        navigateToFilterCount shouldBe 1
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-086 선택한 태그가 있으면 필터 버튼이 적용 상태를 알린다`() {
        setCalendarHomeScreen(
            filterUiState = CalendarHomeScaffoldFilterUiState(isApplied = true),
        )

        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION)
            .assertExists()
        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.StateDescription) shouldBe DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-086 선택한 태그가 없으면 필터 버튼이 적용 상태를 알리지 않는다`() {
        setCalendarHomeScreen(
            filterUiState = CalendarHomeScaffoldFilterUiState(isApplied = false),
        )

        composeRule
            .onNodeWithContentDescription(DEFAULT_FILTER_BUTTON_DESCRIPTION)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.StateDescription) shouldBe null
    }

    private fun setCalendarHomeScreen(
        navigateToFilter: () -> Unit = {},
        filterUiState: CalendarHomeScaffoldFilterUiState = CalendarHomeScaffoldFilterUiState(),
    ) {
        val holidayViewModel = mockk<CalendarHomeHolidayViewModel>()
        every { holidayViewModel.fetch(any()) } returns Unit
        every { holidayViewModel.holidayList } returns MutableStateFlow(emptyList())
        every { holidayViewModel.isFetching } returns MutableStateFlow(false)

        val memoViewModel = mockk<CalendarHomeMemoViewModel>()
        every { memoViewModel.fetch(any()) } returns Unit
        every { memoViewModel.memoList } returns MutableStateFlow(emptyList())
        every { memoViewModel.filterUiState } returns MutableStateFlow(filterUiState)

        composeRule.setContent {
            DiaryTheme {
                CalendarHomeScreen(
                    navigateToMemoDetail = {},
                    navigateToMemoAdd = {},
                    navigateToContactDetail = {},
                    birthdayViewModel = birthdayViewModel(),
                    navigateToFilter = navigateToFilter,
                    navigateToTimetable = {},
                    holidayViewModel = holidayViewModel,
                    memoViewModel = memoViewModel,
                    weatherViewModel = weatherViewModel(),
                    syncViewModel = syncViewModel(),
                    state = rememberCalendarHomeScaffoldState(),
                    permissionManager = rememberPermissionManager(),
                )
            }
        }
    }

    companion object {
        private const val DEFAULT_FILTER_BUTTON_DESCRIPTION = "Filter by tag"
        private const val DEFAULT_FILTER_APPLIED_STATE_DESCRIPTION = "Tag filter applied"
    }
}
