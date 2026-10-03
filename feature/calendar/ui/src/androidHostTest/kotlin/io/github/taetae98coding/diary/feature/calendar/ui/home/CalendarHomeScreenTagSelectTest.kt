package io.github.taetae98coding.diary.feature.calendar.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.calendar.rememberCalendarState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterEvent
import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherReport
import io.github.taetae98coding.diary.core.model.weather.CalendarWeatherTemperature
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarFilterUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.GetCalendarMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.SelectCalendarFilterTagUseCase
import io.github.taetae98coding.diary.domain.tag.usecase.PageTagUseCase
import io.github.taetae98coding.diary.feature.calendar.ui.home.filter.CalendarHomeFilterBottomSheetContent
import io.github.taetae98coding.diary.feature.calendar.ui.home.filter.CalendarHomeFilterViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.home.filter.tagPagingDataOf
import io.github.taetae98coding.diary.feature.calendar.ui.home.memo.CalendarHomeMemoViewModel
import io.github.taetae98coding.diary.feature.calendar.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Instant
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class CalendarHomeScreenTagSelectTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val workTag: Tag = tag(title = TAG_TITLE)
    private val selectedTagListFlow = MutableStateFlow(emptyList<Tag>())

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-081 태그를 선택해도 생일 표시는 달라지지 않는다`() {
        setCalendarHomeScreenWithFilter(
            birthdayList =
                listOf(
                    CalendarContactBirthday(contactId = Uuid.random(), name = BIRTHDAY_NAME, date = july(day = 8)),
                ),
        )
        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertIsDisplayed()

        selectWorkTag()

        composeRule.onNodeWithText(BIRTHDAY_TEXT).assertIsDisplayed()
    }

    @Test
    fun `TC-CALENDAR-HOME-FEATURE-109 태그를 선택해도 공휴일 이름과 날씨 표시는 달라지지 않는다`() {
        setCalendarHomeScreenWithFilter(
            holidayList = listOf(Holiday(name = HOLIDAY_NAME, isHoliday = true, dateRange = july(day = 17)..july(day = 17))),
            weatherReport =
                CalendarWeatherReport(
                    weatherList =
                        listOf(
                            calendarWeather(
                                date = july(day = 15),
                                temperature = CalendarWeatherTemperature.Current(value = 24.3),
                                descriptionList = listOf(SUNNY_DESCRIPTION),
                            ),
                        ),
                ),
        )
        assertHolidayAndWeatherDisplayed()

        selectWorkTag()

        assertHolidayAndWeatherDisplayed()
    }

    private fun selectWorkTag() {
        composeRule.onNodeWithText(TAG_TITLE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(TAG_TITLE).assertIsSelected()
        composeRule
            .onNodeWithContentDescription(FILTER_BUTTON_DESCRIPTION)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.StateDescription) shouldBe FILTER_APPLIED_STATE_DESCRIPTION
    }

    private fun assertHolidayAndWeatherDisplayed() {
        composeRule.onNodeWithText(HOLIDAY_NAME).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(SUNNY_DESCRIPTION, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText(CURRENT_TEMPERATURE_TEXT).assertIsDisplayed()
    }

    private fun setCalendarHomeScreenWithFilter(
        birthdayList: List<CalendarContactBirthday> = emptyList(),
        holidayList: List<Holiday> = emptyList(),
        weatherReport: CalendarWeatherReport = CalendarWeatherReport(),
    ) {
        val getCalendarFilterUseCase = getCalendarFilterUseCase()
        val filterViewModel = filterViewModel(getCalendarFilterUseCase = getCalendarFilterUseCase)
        val memoViewModel =
            CalendarHomeMemoViewModel(
                getCalendarFilterUseCase = getCalendarFilterUseCase,
                getCalendarMemoUseCase =
                    mockk<GetCalendarMemoUseCase>().also { useCase ->
                        every { useCase(parameter = any()) } returns flowOf(Result.success(emptyList()))
                    },
                moveMemoUseCase = mockk(),
            )

        composeRule.setContent {
            val filterUiState by filterViewModel.uiState.collectAsStateWithLifecycle()
            val state =
                rememberCalendarHomeScaffoldState(
                    calendarState = rememberCalendarState(initialYearMonth = JULY_2026),
                )

            DiaryTheme {
                Column {
                    CalendarHomeFilterBottomSheetContent(
                        tagPagingItems = filterViewModel.tagPagingData.collectAsLazyPagingItems(),
                        uiStateProvider = { filterUiState },
                        onEvent = { event ->
                            if (event is TagFilterEvent.Select) filterViewModel.selectTag(id = event.id)
                        },
                    )
                    CalendarHomeScreen(
                        navigateToMemoDetail = {},
                        navigateToMemoAdd = {},
                        navigateToContactDetail = {},
                        navigateToFilter = {},
                        navigateToTimetable = {},
                        state = state,
                        permissionManager = rememberPermissionManager(),
                        holidayViewModel = holidayViewModel(holidayListFlow = MutableStateFlow(holidayList)),
                        memoViewModel = memoViewModel,
                        birthdayViewModel = birthdayViewModel(birthdayListFlow = MutableStateFlow(birthdayList)),
                        weatherViewModel = weatherViewModel(weatherReportFlow = MutableStateFlow(weatherReport)),
                        syncViewModel = syncViewModel(),
                        modifier = Modifier.weight(1F),
                    )
                }
            }
        }
    }

    private fun getCalendarFilterUseCase(): GetCalendarFilterUseCase =
        mockk<GetCalendarFilterUseCase>().also { useCase ->
            every { useCase(parameter = Unit) } returns selectedTagListFlow.map { tagList -> Result.success(tagList) }
        }

    private fun filterViewModel(getCalendarFilterUseCase: GetCalendarFilterUseCase): CalendarHomeFilterViewModel {
        val selectCalendarFilterTagUseCase =
            mockk<SelectCalendarFilterTagUseCase>().also { useCase ->
                coEvery { useCase(parameter = workTag.id) } answers {
                    selectedTagListFlow.value += workTag
                    Result.success(Unit)
                }
            }

        return CalendarHomeFilterViewModel(
            pageTagUseCase =
                mockk<PageTagUseCase>().also { useCase ->
                    every { useCase(parameter = any()) } returns flowOf(Result.success(tagPagingDataOf(listOf(workTag))))
                },
            getCalendarFilterUseCase = getCalendarFilterUseCase,
            selectCalendarFilterTagUseCase = selectCalendarFilterTagUseCase,
            unselectCalendarFilterTagUseCase = mockk(),
            unselectAllCalendarFilterTagUseCase = mockk(),
        )
    }

    private fun tag(title: String): Tag =
        fixtureMonkey
            .giveMeKotlinBuilder<Tag>()
            .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(emoji = "", title = title))
            .setExp(Tag::isFinished, false)
            .setExp(Tag::isDeleted, false)
            .setExp(Tag::updatedAt, fixtureMonkey.giveMeOne<Instant>())
            .setExp(Tag::createdAt, fixtureMonkey.giveMeOne<Instant>())
            .sample()

    private fun july(day: Int): LocalDate = LocalDate(year = 2026, month = Month.JULY, day = day)

    companion object {
        private const val TAG_TITLE = "업무"
        private const val BIRTHDAY_NAME = "홍길동"
        private const val BIRTHDAY_TEXT = "🎂 홍길동"
        private const val HOLIDAY_NAME = "제헌절"
        private const val SUNNY_DESCRIPTION = "맑음"
        private const val CURRENT_TEMPERATURE_TEXT = "24.3°"
        private const val FILTER_BUTTON_DESCRIPTION = "Filter by tag"
        private const val FILTER_APPLIED_STATE_DESCRIPTION = "Tag filter applied"
        private val JULY_2026 = YearMonth(year = 2026, month = Month.JULY)
    }
}
