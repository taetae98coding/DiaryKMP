package io.github.taetae98coding.diary.feature.holiday.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.domain.holiday.usecase.GetGoldenHolidayUseCase
import io.github.taetae98coding.diary.feature.holiday.api.HolidayHomeNavKey
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_ANNUAL_LEAVE_LABEL
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_INCREASE_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.DEFAULT_NEXT_OPTION_DESCRIPTION
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.YEAR
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHoliday
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.goldenHolidayGroup
import io.github.taetae98coding.diary.feature.holiday.ui.home.HolidayHomeTestFixture.holiday
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// MemoAdd 화면에서 돌아왔을 때 보던 상태가 남는지는 전환 이력과 entry별 저장 상태가 함께 정한다.
// HolidayEntry와 같은 구성으로 HolidayHome을 두고, MemoAdd 화면은 이 모듈의 의존이 아니므로 본문만 대역으로 둔다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HolidayHomeNavigationReturnTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val backStack = NavBackStack<ScreenNavKey>(HolidayHomeNavKey)
    private val targetYear = mutableStateOf<Int?>(null)

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-148a5464-9c76-4536-b1c6-877f8740429e MemoAdd 화면에서 돌아오면 보던 상태를 유지한 채 선택된 날짜가 없다`() {
        setHolidayHomeNavDisplay()
        composeRule.onNodeWithContentDescription(DEFAULT_INCREASE_DESCRIPTION).performClick()
        scrollToNextYear()
        composeRule.onNodeWithContentDescription(DEFAULT_NEXT_OPTION_DESCRIPTION).performClick()
        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()
        composeRule.performLongPress(composeRule.dayCenter(day = FIRST_SELECTED_DAY))
        composeRule.performUp()
        composeRule.onNodeWithText(MEMO_ADD_CONTENT).assertIsDisplayed()
        backStack.last() shouldBe memoAddNavKey(day = FIRST_SELECTED_DAY)

        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(HolidayHomeNavKey)
        composeRule.onNodeWithText((YEAR + 1).toString()).assertExists()
        composeRule.onNode(annualLeaveCount(count = 1)).assertExists()
        composeRule.onNodeWithText(SECOND_OPTION_POSITION).assertExists()
        // 남은 선택이 없으면 새로 길게 누른 날짜 하루만 선택되어 완료된다.
        composeRule.performLongPress(composeRule.dayCenter(day = SECOND_SELECTED_DAY))
        composeRule.performUp()
        backStack.last() shouldBe memoAddNavKey(day = SECOND_SELECTED_DAY)
    }

    private fun setHolidayHomeNavDisplay() {
        val module =
            holidayHomeYearViewModelModule(
                fetchHolidayUseCase = successfulFetchHolidayUseCase(),
                getGoldenHolidayUseCase = nextYearTwoOptionGetGoldenHolidayUseCase(),
            )

        composeRule.setContent {
            val viewModelStoreOwner =
                remember {
                    object : ViewModelStoreOwner {
                        override val viewModelStore: ViewModelStore = ViewModelStore()
                    }
                }

            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
                KoinApplication(configuration = koinConfiguration { modules(module) }) {
                    DiaryTheme {
                        NavDisplay(
                            backStack = backStack,
                            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
                            entryProvider =
                                entryProvider {
                                    entry<HolidayHomeNavKey> {
                                        val state = rememberHolidayHomeScaffoldState(initialYear = YEAR)

                                        ScrollToYearEffect(state = state, targetYear = targetYear)
                                        HolidayHomeScreen(
                                            navigateUp = { backStack.removeLastOrNull() },
                                            navigateToMemoAdd = { dateRange ->
                                                backStack.add(
                                                    MemoAddNavKey(
                                                        initialDateRange =
                                                            MemoAddNavKey.InitialDateRange(
                                                                start = dateRange.start,
                                                                endInclusive = dateRange.endInclusive,
                                                            ),
                                                    ),
                                                )
                                            },
                                            state = state,
                                        )
                                    }
                                    entry<MemoAddNavKey> { Text(text = MEMO_ADD_CONTENT) }
                                },
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    // 돌아온 뒤 다시 2027년으로 옮겨 주지 않도록, 옮긴 다음 이동 요청을 지운다.
    private fun scrollToNextYear() {
        composeRule.runOnIdle { targetYear.value = YEAR + 1 }
        composeRule.waitForIdle()
        composeRule.runOnIdle { targetYear.value = null }
        composeRule.waitForIdle()
    }

    // 2027년 2월 16일부터 18일까지의 공휴일에 연차를 앞에 붙인 안과 뒤에 붙인 안을 담는다.
    // 두 번째 안만 2월 21일부터 27일까지의 주를 표시한다.
    private fun nextYearTwoOptionGetGoldenHolidayUseCase(): GetGoldenHolidayUseCase =
        mockk<GetGoldenHolidayUseCase>().also { useCase ->
            every { useCase(parameter = any()) } answers {
                val groupList =
                    if (firstArg<GetGoldenHolidayUseCase.Parameter>().year == YEAR + 1) {
                        val holidayList = listOf(holiday(name = HOLIDAY_NAME, start = nextYearFebruary(day = 16), endInclusive = nextYearFebruary(day = 18)))

                        listOf(
                            goldenHolidayGroup(
                                optionList =
                                    listOf(
                                        goldenHoliday(holidayList = holidayList, start = nextYearFebruary(day = 13), endInclusive = nextYearFebruary(day = 18)),
                                        goldenHoliday(holidayList = holidayList, start = nextYearFebruary(day = 16), endInclusive = nextYearFebruary(day = 21)),
                                    ),
                            ),
                        )
                    } else {
                        emptyList()
                    }

                flowOf(Result.success(groupList))
            }
        }

    private fun memoAddNavKey(day: Int): MemoAddNavKey =
        MemoAddNavKey(
            initialDateRange = MemoAddNavKey.InitialDateRange(start = nextYearFebruary(day = day), endInclusive = nextYearFebruary(day = day)),
        )

    private fun nextYearFebruary(day: Int): LocalDate = LocalDate(year = YEAR + 1, month = Month.FEBRUARY, day = day)

    // 연차 개수는 연차 라벨과 같은 줄에 있는 숫자다. 주별 날짜 숫자와 구분한다.
    private fun annualLeaveCount(count: Int): SemanticsMatcher = hasText(count.toString()) and hasAnySibling(hasText(DEFAULT_ANNUAL_LEAVE_LABEL))

    private companion object {
        const val HOLIDAY_NAME = "설날"
        const val SECOND_OPTION_POSITION = "2 / 2"
        const val FIRST_SELECTED_DAY = 24
        const val SECOND_SELECTED_DAY = 25
        const val MEMO_ADD_CONTENT = "MemoAddContent"
    }
}
